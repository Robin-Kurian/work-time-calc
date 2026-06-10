package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.delay

data class WifiConnectionState(
    val onWifi: Boolean,
    val ssid: String?
)

object WifiConnectionHelper {

    fun readState(context: Context): WifiConnectionState {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var onWifi = false
        var resolvedSsid: String? = null

        for (network in connectivityManager.allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue
            if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) continue

            onWifi = true
            val ssid = cleanSsid(extractSsid(context, capabilities))
            if (ssid != null) {
                return WifiConnectionState(onWifi = true, ssid = ssid)
            }
        }

        if (!onWifi) {
            val activeNetwork = connectivityManager.activeNetwork
            val activeCapabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
            if (activeCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                onWifi = true
                resolvedSsid = cleanSsid(extractSsid(context, activeCapabilities))
            }
        }

        return WifiConnectionState(onWifi = onWifi, ssid = resolvedSsid)
    }

    suspend fun readStateWithRetries(
        context: Context,
        maxAttempts: Int = 5,
        initialDelayMs: Long = 1500,
        retryDelayMs: Long = 2000,
        conservative: Boolean = false
    ): WifiConnectionState {
        var lastState = WifiConnectionState(onWifi = false, ssid = null)

        for (attempt in 1..maxAttempts) {
            val state = readState(context)
            lastState = state

            if (state.onWifi && state.ssid != null) {
                return state
            }

            // During auto-roam the default network can briefly look disconnected.
            // When punched in, keep retrying instead of trusting the first miss.
            if (!conservative && !state.onWifi) {
                return state
            }

            if (attempt < maxAttempts) {
                delay(if (attempt == 1) initialDelayMs else retryDelayMs)
            }
        }

        return lastState
    }

    private fun extractSsid(context: Context, capabilities: NetworkCapabilities): String? {
        val wifiInfo = capabilities.transportInfo as? WifiInfo
        var ssid = wifiInfo?.ssid

        if (ssid == null || ssid == "<unknown ssid>" || ssid == "0x") {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            ssid = wifiManager.connectionInfo?.ssid
        }

        return ssid
    }

    fun cleanSsid(ssid: String?): String? {
        val clean = ssid?.replace("\"", "")?.trim()
        if (clean.isNullOrEmpty() || clean == "<unknown ssid>" || clean == "0x") return null
        return clean
    }
}
