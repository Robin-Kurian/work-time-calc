package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesHelper
import com.example.data.Session
import com.example.utils.NotificationHelper
import com.example.utils.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionDao = db.sessionDao()
    private val prefs = PreferencesHelper(application)
    private val notificationHelper = NotificationHelper(application)

    // Manual screen states
    val arrivalTime = MutableStateFlow(prefs.arrivalTime)
    val lunchOutTime = MutableStateFlow(prefs.lunchOutTime)
    val lunchInTime = MutableStateFlow(prefs.lunchInTime)

    // Session tracking states
    val workSsid = MutableStateFlow(prefs.workSsid)
    private val _currentSsid = MutableStateFlow<String?>(null)
    val currentSsid = _currentSsid.asStateFlow()

    private val _lastCheckedTime = MutableStateFlow("")
    val lastCheckedTime = _lastCheckedTime.asStateFlow()

    // Real-time ticking relative elapsed timer since last Punch In
    private val _liveActiveElapsedSeconds = MutableStateFlow(0L)
    val liveActiveElapsedSeconds = _liveActiveElapsedSeconds.asStateFlow()

    // Auto-refresh control jobs
    private var timerJob: Job? = null
    private var wifiPollJob: Job? = null

    // Get today's unique date string
    fun getTodayString(): String {
        return SimpleDateFormat("EEE MMM dd yyyy", Locale.US).format(Date())
    }

    // Reactive list of today's sessions
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions = _sessions.asStateFlow()

    init {
        checkMidnightAndLoadSessions()
        startWifiPolling()
        startLiveTimer()
    }

    // Manual calculation helper properties as combiners
    val manualCalculationState: StateFlow<ManualCalcState> = combine(
        arrivalTime, lunchOutTime, lunchInTime
    ) { arr, lo, li ->
        val arrivalMin = TimeUtils.toMin(arr)
        val lunchOutMin = TimeUtils.toMin(lo)
        val lunchInMin = TimeUtils.toMin(li)

        val lunchDur = Math.max(0, lunchInMin - lunchOutMin)
        val leaveMin = arrivalMin + TimeUtils.REQUIRED_MINUTES + lunchDur
        val amWork = lunchOutMin - (arrivalMin + TimeUtils.BUFFER_IN)
        val pmWork = leaveMin - TimeUtils.BUFFER_OUT - lunchInMin

        ManualCalcState(
            arrivalMin = arrivalMin,
            lunchDur = lunchDur,
            leaveMin = leaveMin,
            amWork = amWork,
            pmWork = pmWork
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManualCalcState(0, 0, 0, 0, 0)
    )

    fun updateArrivalTime(time: String) {
        arrivalTime.value = time
        prefs.arrivalTime = time
    }

    fun updateLunchOutTime(time: String) {
        lunchOutTime.value = time
        prefs.lunchOutTime = time
    }

    fun updateLunchInTime(time: String) {
        lunchInTime.value = time
        prefs.lunchInTime = time
    }

    fun updateWorkSsid(ssid: String) {
        workSsid.value = ssid
        prefs.workSsid = ssid
        checkWifiConnectionInstant()
    }

    // Active tracking
    fun checkMidnightAndLoadSessions() {
        viewModelScope.launch {
            val todayStr = getTodayString()
            val savedDay = prefs.trackedDay
            if (savedDay != todayStr) {
                prefs.trackedDay = todayStr
                // Automatically reset/wipe sessions for the new day
                sessionDao.clearAll()
            }
            // Reactive observation
            sessionDao.getSessionsForDay(todayStr).collect {
                _sessions.value = it
            }
        }
    }

    // Trigger Wi-Fi connection check instantly
    fun checkWifiConnectionInstant() {
        viewModelScope.launch {
            val ssid = getActiveWifiSsid(getApplication())
            _currentSsid.value = ssid
            _lastCheckedTime.value = SimpleDateFormat("h:mm a", Locale.US).format(Date())

            // Auto punch tracking if Work SSID matches
            val targetSsid = workSsid.value
            if (targetSsid.isNotEmpty()) {
                val currentS = _sessions.value
                val isIn = currentS.isNotEmpty() && currentS.first().outTime == null
                
                if (ssid == targetSsid && !isIn) {
                    // Auto punch IN
                    punch()
                } else if (ssid != targetSsid && isIn) {
                    // Auto punch OUT
                    punch()
                }
            }
        }
    }

    private fun startWifiPolling() {
        wifiPollJob?.cancel()
        wifiPollJob = viewModelScope.launch {
            while (true) {
                checkWifiConnectionInstant()
                delay(30000) // Poll every 30 seconds
            }
        }
    }

    private fun startLiveTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentSessions = _sessions.value
                if (currentSessions.isNotEmpty() && currentSessions.first().outTime == null) {
                    val inTimeMs = currentSessions.first().inTime
                    val elapsedSeconds = (System.currentTimeMillis() - inTimeMs) / 1000
                    _liveActiveElapsedSeconds.value = Math.max(0L, elapsedSeconds)
                } else {
                    _liveActiveElapsedSeconds.value = 0L
                }
                delay(1000)
            }
        }
    }

    fun punch() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val todayStr = getTodayString()
            val currentSessions = _sessions.value
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

                if (workedMinutesToday >= TimeUtils.REQUIRED_MINUTES) {
                    notificationHelper.sendNotification(
                        "🎉 Time to leave!",
                        "You've worked ${TimeUtils.fmtDur(workedMinutesToday)} today"
                    )
                }
            }
            // Reload just in case
            checkMidnightAndLoadSessions()
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            sessionDao.clearAll()
            checkMidnightAndLoadSessions()
        }
    }

    fun calculateWorkedMinutes(sessionList: List<Session>): Int {
        val now = System.currentTimeMillis()
        var totalMs = 0L
        for (s in sessionList) {
            val outTime = s.outTime ?: now
            totalMs += (outTime - s.inTime)
        }
        return (totalMs / 60000).toInt()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        wifiPollJob?.cancel()
    }

    private fun getActiveWifiSsid(context: Context): String? {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wifiManager.connectionInfo
                if (info != null) {
                    val ssid = info.ssid
                    if (ssid != null && ssid != "<unknown ssid>" && ssid != "0x") {
                        return ssid.replace("\"", "")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

data class ManualCalcState(
    val arrivalMin: Int,
    val lunchDur: Int,
    val leaveMin: Int,
    val amWork: Int,
    val pmWork: Int
)
