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
    private val checkMutex = Mutex()
    private val punchMutex = Mutex()

    suspend fun checkWifiConnection(context: Context) = checkMutex.withLock {
        val prefs = PreferencesHelper(context)
        if (!prefs.isAutoTrackingEnabled || prefs.isLocked) return

        val targetSsid = WifiConnectionHelper.cleanSsid(prefs.workSsid) ?: return

        val db = AppDatabase.getDatabase(context)
        val todayStr = getTodayString()
        val currentS = db.sessionDao().getSessionsForDaySync(todayStr)
        val isIn = currentS.isNotEmpty() && currentS.first().outTime == null

        updateDisplayState(context, prefs, targetSsid, isIn)

        if (isIn) {
            if (WifiConnectionHelper.confirmLeftWorkNetwork(context, targetSsid)) {
                punchOut(context)
            }
        } else if (WifiConnectionHelper.confirmAtWorkNetwork(context, targetSsid)) {
            punchIn(context)
        }
    }

    private fun updateDisplayState(
        context: Context,
        prefs: PreferencesHelper,
        targetSsid: String,
        isIn: Boolean
    ) {
        val display = WifiConnectionHelper.captureDisplayState(context)
        val detectedSsid = when {
            display.ssid != null -> display.ssid
            display.onWifi && isIn -> targetSsid
            else -> null
        }

        prefs.currentSsid = detectedSsid
        prefs.lastCheckedTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("EEE MMM dd yyyy", Locale.US).format(Date())
    }

    private suspend fun punchIn(context: Context) = punchMutex.withLock {
        try {
            val db = AppDatabase.getDatabase(context)
            val sessionDao = db.sessionDao()
            val notificationHelper = NotificationHelper(context)
            val now = System.currentTimeMillis()
            val todayStr = getTodayString()
            val currentSessions = sessionDao.getSessionsForDaySync(todayStr)
            val hasActive = currentSessions.isNotEmpty() && currentSessions.first().outTime == null

            if (hasActive) return

            val newSession = Session(inTime = now, outTime = null, dayString = todayStr)
            sessionDao.insertSession(newSession)
            notificationHelper.sendNotification("🟢 Punched In", "Started at ${TimeUtils.fmtTimestamp(now)}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun punchOut(context: Context) = punchMutex.withLock {
        try {
            val db = AppDatabase.getDatabase(context)
            val sessionDao = db.sessionDao()
            val prefs = PreferencesHelper(context)
            val notificationHelper = NotificationHelper(context)
            val now = System.currentTimeMillis()
            val todayStr = getTodayString()
            val currentSessions = sessionDao.getSessionsForDaySync(todayStr)
            val hasActive = currentSessions.isNotEmpty() && currentSessions.first().outTime == null

            if (!hasActive) return

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
                if (prefs.isAlarmEnabled && !alreadyAlarmed) {
                    notificationHelper.showAlarmNotification(
                        title = "🎉 Time to leave!",
                        body = "You've worked ${TimeUtils.fmtDurSeconds(workedMs / 1000)} today",
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
