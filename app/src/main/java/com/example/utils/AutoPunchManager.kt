package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.example.data.AppDatabase
import com.example.data.PreferencesHelper
import com.example.data.Session
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object AutoPunchManager {
    private val punchMutex = Mutex()

    suspend fun checkWifiConnection(context: Context) {
        val prefs = PreferencesHelper(context)
        if (!prefs.isAutoTrackingEnabled || prefs.isLocked) return

        val targetSsid = prefs.workSsid
        if (targetSsid.isEmpty()) return

        var ssid: String? = null
        var isCurrentlyOnWifi = false
        val maxRetries = 5

        for (attempt in 1..maxRetries) {
            ssid = getActiveWifiSsid(context)
            isCurrentlyOnWifi = isActuallyOnWifi(context)

            // If we are connected and SSID resolved, we are good to go
            if (isCurrentlyOnWifi && !ssid.isNullOrEmpty() && ssid != "<unknown ssid>" && ssid != "0x") {
                break
            }

            // If wifi is off/disconnected completely, no need to retry
            if (!isCurrentlyOnWifi) {
                break
            }

            // Connection is establishing; wait 2s before retry
            kotlinx.coroutines.delay(2000)
        }

        // Store detected SSID
        val detectedSsid = if (ssid == null && isCurrentlyOnWifi) {
            val db = AppDatabase.getDatabase(context)
            val currentS = db.sessionDao().getSessionsForDaySync(getTodayString())
            val isIn = currentS.isNotEmpty() && currentS.first().outTime == null
            if (isIn) targetSsid else null
        } else {
            ssid
        }

        prefs.currentSsid = detectedSsid
        prefs.lastCheckedTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())

        val db = AppDatabase.getDatabase(context)
        val todayStr = getTodayString()
        val currentS = db.sessionDao().getSessionsForDaySync(todayStr)
        val isIn = currentS.isNotEmpty() && currentS.first().outTime == null

        if (detectedSsid == targetSsid && !isIn) {
            punch(context)
        } else if (isIn) {
            val isDifferentIdentifiableWifi = !detectedSsid.isNullOrEmpty() && detectedSsid != targetSsid && detectedSsid != "<unknown ssid>"
            val hasLeftWifiEntirely = !isCurrentlyOnWifi

            if (isDifferentIdentifiableWifi || hasLeftWifiEntirely) {
                punch(context)
            }
        }
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("EEE MMM dd yyyy", Locale.US).format(Date())
    }

    private fun isActuallyOnWifi(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            false
        }
    }

    private fun getActiveWifiSsid(context: Context): String? {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiInfo = capabilities.transportInfo as? WifiInfo
                var ssid = wifiInfo?.ssid

                if (ssid == null || ssid == "<unknown ssid>" || ssid == "0x") {
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    ssid = wifiManager.connectionInfo?.ssid
                }

                val clean = ssid?.replace("\"", "")
                if (!clean.isNullOrEmpty() && clean != "<unknown ssid>" && clean != "0x") {
                    return clean
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private suspend fun punch(context: Context) = punchMutex.withLock {
        try {
            val db = AppDatabase.getDatabase(context)
            val sessionDao = db.sessionDao()
            val prefs = PreferencesHelper(context)
            val notificationHelper = NotificationHelper(context)
            val now = System.currentTimeMillis()
            val todayStr = getTodayString()
            val currentSessions = sessionDao.getSessionsForDaySync(todayStr)
            val hasActive = currentSessions.isNotEmpty() && currentSessions.first().outTime == null

            if (!hasActive) {
                // Punch IN
                val newSession = Session(inTime = now, outTime = null, dayString = todayStr)
                sessionDao.insertSession(newSession)
                notificationHelper.sendNotification("🟢 Punched In", "Started at ${TimeUtils.fmtTimestamp(now)}")
            } else {
                // Punch OUT
                val activeSession = currentSessions.first()
                val updated = activeSession.copy(outTime = now)
                sessionDao.updateSession(updated)

                val sessionDurMin = ((now - activeSession.inTime) / 60000).toInt()
                val workedMinutesToday = calculateWorkedMinutes(currentSessions.map {
                    if (it.id == activeSession.id) updated else it
                })

                notificationHelper.sendNotification(
                    "🔴 Punched Out",
                    "Session: ${TimeUtils.fmtDur(sessionDurMin)} · Total: ${TimeUtils.fmtDur(workedMinutesToday)}"
                )

                if (workedMinutesToday >= prefs.targetWorkMinutes) {
                    val alreadyAlarmed = prefs.alarmedDay == todayStr
                    if (!alreadyAlarmed) {
                        prefs.alarmedDay = todayStr
                    }
                    notificationHelper.sendNotification(
                        "🎉 Time to leave!",
                        "You've worked ${TimeUtils.fmtDur(workedMinutesToday)} today",
                        isTimer = !alreadyAlarmed,
                        silent = com.example.MainActivity.isForeground
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateWorkedMinutes(sessionList: List<Session>): Int {
        val now = System.currentTimeMillis()
        var totalMs = 0L
        for (s in sessionList) {
            val outTime = s.outTime ?: now
            totalMs += (outTime - s.inTime)
        }
        return (totalMs / 60000).toInt()
    }
}
