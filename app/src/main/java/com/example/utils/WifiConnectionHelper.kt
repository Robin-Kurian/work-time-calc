package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.delay

/** Strict live snapshot used for punch decisions — never uses stale SSID cache. */
data class WifiSnapshot(
    val linkUp: Boolean,
    val ssid: String?
)

/** Looser state for UI display only. */
data class WifiConnectionState(
    val onWifi: Boolean,
    val ssid: String?
)

object WifiConnectionHelper {

    fun captureSnapshot(context: Context): WifiSnapshot {
        if (!PermissionUtils.hasLocationPermission(context)) {
            return WifiSnapshot(linkUp = false, ssid = null)
        }
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return WifiSnapshot(linkUp = false, ssid = null)

        if (!isSupplicantConnected(wifiManager)) {
            return WifiSnapshot(linkUp = false, ssid = null)
        }

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return WifiSnapshot(linkUp = false, ssid = null)

        var hasLiveWifiTransport = false
        var ssidFromCapabilities: String? = null

        val activeNetwork = runCatching { connectivityManager.activeNetwork }.getOrNull()
        val activeCapabilities = activeNetwork?.let { network ->
            runCatching { connectivityManager.getNetworkCapabilities(network) }.getOrNull()
        }
        if (activeCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true &&
            activeCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
        ) {
            hasLiveWifiTransport = true
            ssidFromCapabilities = cleanSsid(extractSsidFromCapabilities(activeCapabilities))
        }

        if (!hasLiveWifiTransport || ssidFromCapabilities == null) {
            val allNetworks = runCatching { connectivityManager.allNetworks }.getOrNull().orEmpty()
            for (network in allNetworks) {
                val capabilities = runCatching { connectivityManager.getNetworkCapabilities(network) }.getOrNull()
                    ?: continue
                if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) continue
                if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) continue
                hasLiveWifiTransport = true
                val candidate = cleanSsid(extractSsidFromCapabilities(capabilities))
                if (candidate != null) {
                    ssidFromCapabilities = candidate
                    break
                }
            }
        }

        if (!hasLiveWifiTransport) {
            return WifiSnapshot(linkUp = false, ssid = null)
        }

        val ssid = runCatching { cleanSsid(wifiManager.connectionInfo?.ssid) }.getOrNull()
            ?: ssidFromCapabilities
        return WifiSnapshot(linkUp = true, ssid = ssid)
    }

    fun captureDisplayState(context: Context): WifiConnectionState {
        val snapshot = captureSnapshot(context)
        if (!snapshot.linkUp) {
            return WifiConnectionState(onWifi = false, ssid = null)
        }
        if (snapshot.ssid != null) {
            return WifiConnectionState(onWifi = true, ssid = snapshot.ssid)
        }

        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val displaySsid = runCatching { cleanSsid(wifiManager?.connectionInfo?.ssid) }.getOrNull()
        return WifiConnectionState(onWifi = true, ssid = displaySsid)
    }

    /**
     * Treat known office SSID variants (for example "ISPG_Staff" and "ISPG_Guest")
     * as equivalent for auto-tracking decisions.
     */
    fun isWorkNetworkMatch(currentSsid: String?, targetSsid: String?): Boolean {
        if (currentSsid == null || targetSsid == null) return false
        return isLikelySameWifiFamily(currentSsid, targetSsid)
    }

    private fun isLikelySameWifiFamily(currentSsid: String, targetSsid: String): Boolean {
        if (currentSsid == targetSsid) return true
        val currentFamily = extractFamilyPrefix(currentSsid) ?: return false
        val targetFamily = extractFamilyPrefix(targetSsid) ?: return false
        return currentFamily.equals(targetFamily, ignoreCase = true)
    }

    /**
     * Confirms the device is on the work SSID with two consecutive live reads.
     * Used only when punched OUT to avoid stale-cache false punch-ins.
     */
    suspend fun confirmAtWorkNetwork(context: Context, targetSsid: String): Boolean {
        var consecutiveMatches = 0

        repeat(3) { attempt ->
            val snap = captureSnapshot(context)
            when {
                !snap.linkUp -> return false
                isWorkNetworkMatch(snap.ssid, targetSsid) -> {
                    consecutiveMatches++
                    if (consecutiveMatches >= 2) return true
                }
                snap.ssid != null -> return false
                else -> consecutiveMatches = 0
            }

            if (attempt < 2) delay(1000)
        }

        return false
    }

    /**
     * Confirms the device left the work network.
     * Tolerates brief roam gaps while punched IN.
     */
    suspend fun confirmLeftWorkNetwork(context: Context, targetSsid: String): Boolean {
        var consecutiveAway = 0

        repeat(5) { attempt ->
            val snap = captureSnapshot(context)
            when {
                !snap.linkUp -> {
                    consecutiveAway++
                    if (consecutiveAway >= 2) return true
                }
                snap.ssid != null && !isWorkNetworkMatch(snap.ssid, targetSsid) -> return true
                isWorkNetworkMatch(snap.ssid, targetSsid) -> {
                    consecutiveAway = 0
                    return false
                }
                else -> consecutiveAway = 0
            }

            if (attempt < 4) {
                delay(if (attempt == 0) 1500 else 2000)
            }
        }

        return consecutiveAway >= 2
    }

    private fun isSupplicantConnected(wifiManager: WifiManager): Boolean {
        val info = runCatching { wifiManager.connectionInfo }.getOrNull() ?: return false
        return info.networkId != -1 && info.supplicantState == SupplicantState.COMPLETED
    }

    private fun extractSsidFromCapabilities(capabilities: NetworkCapabilities): String? {
        val wifiInfo = capabilities.transportInfo as? WifiInfo
        return wifiInfo?.ssid
    }

    fun cleanSsid(ssid: String?): String? {
        val clean = ssid?.replace("\"", "")?.trim()
        if (clean.isNullOrEmpty() || clean == "<unknown ssid>" || clean == "0x") return null
        return clean
    }

    private fun extractFamilyPrefix(ssid: String): String? {
        val firstToken = ssid.trim().split('_', '-', ' ').firstOrNull().orEmpty().trim()
        return firstToken.takeIf { it.length >= 3 }
    }
}
