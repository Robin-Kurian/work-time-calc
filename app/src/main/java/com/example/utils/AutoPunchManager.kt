package com.example.utils

import android.content.Context
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

        val db = AppDatabase.getDatabase(context)
        val todayStr = getTodayString()
        val currentS = db.sessionDao().getSessionsForDaySync(todayStr)
        val isIn = currentS.isNotEmpty() && currentS.first().outTime == null

        val wifiState = WifiConnectionHelper.readStateWithRetries(
            context = context,
            conservative = isIn
        )
        val ssid = wifiState.ssid
        val isCurrentlyOnWifi = wifiState.onWifi

        // Store detected SSID
        val detectedSsid = if (ssid == null && isCurrentlyOnWifi && isIn) {
            targetSsid
        } else {
            ssid
        }

        prefs.currentSsid = detectedSsid
        prefs.lastCheckedTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())

        if (detectedSsid == targetSsid && !isIn) {
            punch(context)
        } else if (isIn) {
            val isDifferentIdentifiableWifi =
                !detectedSsid.isNullOrEmpty() && detectedSsid != targetSsid
            val hasLeftWifiEntirely = !isCurrentlyOnWifi

            if (isDifferentIdentifiableWifi || hasLeftWifiEntirely) {
                punch(context)
            }
        }
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("EEE MMM dd yyyy", Locale.US).format(Date())
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

                val sessionDurSec = (now - activeSession.inTime) / 1000
                val workedMs = calculateWorkedMs(currentSessions.map {
                    if (it.id == activeSession.id) updated else it
                })
                val targetMs = prefs.targetWorkMinutes * 60_000L
                val isDone = targetMs > 0 && workedMs >= targetMs

                notificationHelper.sendNotification(
                    "🔴 Punched Out",
                    "Session: ${TimeUtils.fmtDurSeconds(sessionDurSec)} · Total: ${TimeUtils.fmtDurSeconds(workedMs / 1000)}"
                )

                if (isDone) {
                    val alreadyAlarmed = prefs.alarmedDay == todayStr
                    if (!alreadyAlarmed) {
                        prefs.alarmedDay = todayStr
                    }
                    notificationHelper.sendNotification(
                        "🎉 Time to leave!",
                        "You've worked ${TimeUtils.fmtDurSeconds(workedMs / 1000)} today",
                        isTimer = !alreadyAlarmed,
                        silent = com.example.MainActivity.isForeground
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateWorkedMs(sessionList: List<Session>, nowMs: Long = System.currentTimeMillis()): Long {
        return sessionList.sumOf { s -> (s.outTime ?: nowMs) - s.inTime }
    }
}
