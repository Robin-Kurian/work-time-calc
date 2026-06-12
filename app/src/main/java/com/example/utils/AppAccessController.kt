package com.example.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

enum class AccessBlockReason {
    APP_DISABLED,
    MINIMUM_VERSION_REQUIRED,
    VERSION_BLOCKED,
    CANNOT_VERIFY
}

sealed interface AppAccessDecision {
    data object Allow : AppAccessDecision

    data class Block(
        val reason: AccessBlockReason,
        val message: String,
        val detail: String? = null,
        val updateUrl: String?,
        val canRetry: Boolean
    ) : AppAccessDecision
}

data class RemoteAccessConfig(
    val enabled: Boolean = true,
    val killSwitch: Boolean = false,
    val minVersionCode: Int = 1,
    val blockedVersionCodes: Set<Int> = emptySet(),
    val forceUpdateUrl: String? = null,
    val message: String = "",
    val graceHoursIfOffline: Int = 24
) {
    fun toJsonString(): String {
        val payload = JSONObject()
        payload.put("enabled", enabled)
        payload.put("killSwitch", killSwitch)
        payload.put("minVersionCode", minVersionCode)
        payload.put("blockedVersionCodes", blockedVersionCodes.toList())
        payload.put("forceUpdateUrl", forceUpdateUrl ?: "")
        payload.put("message", message)
        payload.put("graceHoursIfOffline", graceHoursIfOffline)
        return payload.toString()
    }
}

class AppAccessController(
    private val context: Context,
    private val configUrl: String = DEFAULT_CONFIG_URL
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun evaluateAccess(
        allowCachedFallback: Boolean = true,
        maxFetchAttempts: Int = 1
    ): AppAccessDecision = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        when (val fetchResult = fetchRemoteConfigWithRetries(maxFetchAttempts)) {
            is FetchResult.Success -> {
                cacheConfig(fetchResult.config, now)
                evaluateConfig(fetchResult.config)
            }
            is FetchResult.Failure -> {
                if (allowCachedFallback) {
                    evaluateWithCacheOrBlock(now)
                } else {
                    cannotVerifyDecision(fetchResult.reason)
                }
            }
        }
    }

    private fun evaluateWithCacheOrBlock(now: Long): AppAccessDecision {
        val cachedConfig = loadCachedConfig() ?: return cannotVerifyDecision()
        val lastSuccessAt = prefs.getLong(KEY_LAST_SUCCESS_AT, 0L)
        val graceMs = TimeUnit.HOURS.toMillis(cachedConfig.graceHoursIfOffline.coerceAtLeast(0).toLong())
        val withinGrace = lastSuccessAt > 0L && now - lastSuccessAt <= graceMs

        return if (withinGrace) {
            evaluateConfig(cachedConfig)
        } else {
            cannotVerifyDecision()
        }
    }

    private fun evaluateConfig(config: RemoteAccessConfig): AppAccessDecision {
        val currentVersionCode = currentVersionCode()

        if (!config.enabled || config.killSwitch) {
            return AppAccessDecision.Block(
                reason = AccessBlockReason.APP_DISABLED,
                message = config.message.ifBlank {
                    "This app is currently disabled."
                },
                detail = null,
                updateUrl = null,
                canRetry = true
            )
        }

        val isBelowMinVersion = currentVersionCode < config.minVersionCode
        if (isBelowMinVersion) {
            return AppAccessDecision.Block(
                reason = AccessBlockReason.MINIMUM_VERSION_REQUIRED,
                message = config.message.ifBlank {
                    "Please update the app to continue."
                },
                detail = "Installed version code: $currentVersionCode. Minimum required: ${config.minVersionCode}.",
                updateUrl = config.forceUpdateUrl,
                canRetry = true
            )
        }

        val isExplicitlyBlocked = config.blockedVersionCodes.contains(currentVersionCode)
        if (isExplicitlyBlocked) {
            return AppAccessDecision.Block(
                reason = AccessBlockReason.VERSION_BLOCKED,
                message = config.message.ifBlank {
                    "This app version is blocked. Please update to continue."
                },
                detail = "Installed version code: $currentVersionCode is in blockedVersionCodes.",
                updateUrl = config.forceUpdateUrl,
                canRetry = true
            )
        }

        return AppAccessDecision.Allow
    }

    private fun cannotVerifyDecision(detail: String? = null): AppAccessDecision = AppAccessDecision.Block(
        reason = AccessBlockReason.CANNOT_VERIFY,
        message = "Unable to verify access right now. Connect to internet and try again.",
        detail = detail,
        updateUrl = null,
        canRetry = true
    )

    private suspend fun fetchRemoteConfigWithRetries(maxAttempts: Int): FetchResult {
        val attempts = maxAttempts.coerceAtLeast(1)
        var lastFailure: FetchResult.Failure = FetchResult.Failure("Unknown error")

        repeat(attempts) { attemptIndex ->
            when (val result = fetchRemoteConfig()) {
                is FetchResult.Success -> return result
                is FetchResult.Failure -> lastFailure = result
            }

            if (attemptIndex < attempts - 1) {
                delay(RETRY_DELAY_MS)
            }
        }

        return lastFailure
    }

    private fun cacheConfig(config: RemoteAccessConfig, now: Long) {
        prefs.edit()
            .putString(KEY_CONFIG_JSON, config.toJsonString())
            .putLong(KEY_LAST_SUCCESS_AT, now)
            .apply()
    }

    private fun loadCachedConfig(): RemoteAccessConfig? {
        val raw = prefs.getString(KEY_CONFIG_JSON, null) ?: return null
        return parseConfig(raw)
    }

    private fun fetchRemoteConfig(): FetchResult {
        return try {
            val freshConfigUrl = freshConfigUrl()
            val body = fetchJsonText(freshConfigUrl) ?: return FetchResult.Failure("Network error")
            val config = parseRemoteConfigResponse(body) ?: return FetchResult.Failure("Invalid config")
            FetchResult.Success(config)
        } catch (error: Exception) {
            FetchResult.Failure(error.message ?: "Unknown error")
        }
    }

    private fun freshConfigUrl(): String {
        val separator = if (configUrl.contains("?")) "&" else "?"
        return "$configUrl${separator}_ts=${System.currentTimeMillis()}"
    }

    private fun fetchJsonText(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0")
            setRequestProperty("Pragma", "no-cache")
            setRequestProperty("User-Agent", USER_AGENT)
        }

        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                null
            } else {
                connection.inputStream.bufferedReader().use { it.readText() }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRemoteConfigResponse(raw: String): RemoteAccessConfig? {
        parseConfig(raw)?.let { return it }
        return parseConfigFromGistApiResponse(raw)
    }

    private fun parseConfigFromGistApiResponse(raw: String): RemoteAccessConfig? {
        return runCatching {
            val root = JSONObject(raw)
            val files = root.optJSONObject("files") ?: return null

            val configuredFile = files.optJSONObject(DEFAULT_CONFIG_FILENAME)
            val configuredContent = extractConfigContent(configuredFile)
            if (!configuredContent.isNullOrBlank()) {
                return parseConfig(configuredContent)
            }

            val keys = files.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val fileObject = files.optJSONObject(key)
                val content = extractConfigContent(fileObject)
                if (!content.isNullOrBlank()) {
                    return parseConfig(content)
                }
            }

            null
        }.getOrNull()
    }

    private fun extractConfigContent(fileObject: JSONObject?): String? {
        if (fileObject == null) return null

        val embeddedContent = fileObject.optString("content", "").trim()
        if (embeddedContent.isNotBlank()) {
            return embeddedContent
        }

        val rawUrl = fileObject.optString("raw_url", "").trim()
        if (rawUrl.isBlank()) {
            return null
        }

        return fetchJsonText(rawUrl)
    }

    private fun parseConfig(raw: String): RemoteAccessConfig? {
        return runCatching {
            val json = JSONObject(raw)
            val blocked = buildSet {
                val blockedArray = json.optJSONArray("blockedVersionCodes")
                if (blockedArray != null) {
                    for (index in 0 until blockedArray.length()) {
                        val version = blockedArray.optInt(index, Int.MIN_VALUE)
                        if (version != Int.MIN_VALUE) add(version)
                    }
                }
            }

            val updateUrl = json.optString("forceUpdateUrl", "").trim().ifBlank { null }
            RemoteAccessConfig(
                enabled = json.optBoolean("enabled", true),
                killSwitch = json.optBoolean("killSwitch", false),
                minVersionCode = json.optInt("minVersionCode", 1),
                blockedVersionCodes = blocked,
                forceUpdateUrl = updateUrl,
                message = json.optString("message", ""),
                graceHoursIfOffline = json.optInt("graceHoursIfOffline", DEFAULT_GRACE_HOURS)
                    .coerceIn(0, MAX_GRACE_HOURS)
            )
        }.getOrNull()
    }

    private fun currentVersionCode(): Int {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }
    }

    private sealed interface FetchResult {
        data class Success(val config: RemoteAccessConfig) : FetchResult
        data class Failure(val reason: String) : FetchResult
    }

    companion object {
        private const val PREFS_NAME = "wtc_remote_access"
        private const val KEY_CONFIG_JSON = "config_json"
        private const val KEY_LAST_SUCCESS_AT = "last_success_at"
        private const val NETWORK_TIMEOUT_MS = 3_000
        private const val RETRY_DELAY_MS = 700L
        private const val DEFAULT_GRACE_HOURS = 24
        private const val MAX_GRACE_HOURS = 7 * 24
        private const val DEFAULT_CONFIG_FILENAME = "app-config.json"
        private const val USER_AGENT = "WorkTimeCalc/3.1.0"
        private const val DEFAULT_CONFIG_URL =
            "https://gist.githubusercontent.com/Robin-Kurian/4f139d16ee7b8eb3f47af6d26664eed3/raw/app-config.json"
    }
}
