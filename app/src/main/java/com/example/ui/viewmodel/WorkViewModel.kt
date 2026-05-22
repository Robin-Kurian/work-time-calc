package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesHelper
import com.example.data.Session
import com.example.data.Task
import com.example.data.TaskDao
import com.example.utils.NotificationHelper
import com.example.utils.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionDao = db.sessionDao()
    private val taskDao = db.taskDao()
    private val prefs = PreferencesHelper(application)
    private val notificationHelper = NotificationHelper(application)

    // Pomodoro States
    private val _pomodoroMode = MutableStateFlow(PomodoroMode.WORK)
    val pomodoroMode = _pomodoroMode.asStateFlow()

    private val _pomodoroRemainingSeconds = MutableStateFlow(PomodoroMode.WORK.durationMinutes * 60)
    val pomodoroRemainingSeconds = _pomodoroRemainingSeconds.asStateFlow()

    private val _pomodoroTotalSeconds = MutableStateFlow(PomodoroMode.WORK.durationMinutes * 60)
    val pomodoroTotalSeconds = _pomodoroTotalSeconds.asStateFlow()

    private val _pomodoroIsRunning = MutableStateFlow(false)
    val pomodoroIsRunning = _pomodoroIsRunning.asStateFlow()

    private var pomodoroJob: Job? = null

    // Task States
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

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

    private val _isAutoTrackingEnabled = MutableStateFlow(prefs.isAutoTrackingEnabled)
    val isAutoTrackingEnabled = _isAutoTrackingEnabled.asStateFlow()

    private val _isLocked = MutableStateFlow(prefs.isLocked)
    val isLocked = _isLocked.asStateFlow()

    // Real-time ticking relative elapsed timer since last Punch In
    private val _liveActiveElapsedSeconds = MutableStateFlow(0L)
    val liveActiveElapsedSeconds = _liveActiveElapsedSeconds.asStateFlow()

    // Auto-refresh control jobs
    private var timerJob: Job? = null
    private var wifiPollJob: Job? = null
    private var sessionLoadJob: Job? = null
    private var isPunching = false

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkWifiConnectionInstant()
        }

        override fun onLost(network: Network) {
            checkWifiConnectionInstant()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            checkWifiConnectionInstant()
        }
    }

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
        loadTasks()
        
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        val amWork = Math.max(0, lunchOutMin - (arrivalMin + TimeUtils.BUFFER_IN))
        val pmWork = Math.max(0, leaveMin - TimeUtils.BUFFER_OUT - lunchInMin)

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

    fun toggleAutoTracking() {
        val newState = !_isAutoTrackingEnabled.value
        _isAutoTrackingEnabled.value = newState
        prefs.isAutoTrackingEnabled = newState
    }

    fun toggleLock() {
        val newState = !_isLocked.value
        _isLocked.value = newState
        prefs.isLocked = newState
        checkWifiConnectionInstant()
    }

    fun setConnectedAsWork() {
        val current = _currentSsid.value
        if (!current.isNullOrEmpty()) {
            updateWorkSsid(current)
        }
    }

    // Active tracking
    fun checkMidnightAndLoadSessions() {
        sessionLoadJob?.cancel()
        sessionLoadJob = viewModelScope.launch {
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
            val context = getApplication<Application>()
            val ssid = getActiveWifiSsid(context)
            val isCurrentlyOnWifi = isActuallyOnWifi(context)
            
            val currentS = _sessions.value
            val isIn = currentS.isNotEmpty() && currentS.first().outTime == null
            val targetSsid = workSsid.value

            // UI Label Fix
            if (ssid == null && isCurrentlyOnWifi && isIn) {
                _currentSsid.value = targetSsid
            } else {
                _currentSsid.value = ssid
            }
            
            _lastCheckedTime.value = SimpleDateFormat("h:mm a", Locale.US).format(Date())

            // Auto punch tracking: Skip if LOCKED or DISABLED
            if (_isAutoTrackingEnabled.value && !_isLocked.value) {
                if (targetSsid.isNotEmpty()) {
                    if (ssid == targetSsid && !isIn) {
                        punch()
                    } else if (isIn) {
                        val isDifferentIdentifiableWifi = !ssid.isNullOrEmpty() && ssid != targetSsid && ssid != "<unknown ssid>"
                        val hasLeftWifiEntirely = !isCurrentlyOnWifi
                        
                        if (isDifferentIdentifiableWifi || hasLeftWifiEntirely) {
                            punch()
                        }
                    }
                }
            }
        }
    }

    private fun startWifiPolling() {
        wifiPollJob?.cancel()
        wifiPollJob = viewModelScope.launch {
            while (true) {
                checkWifiConnectionInstant()
                delay(60000) // Fallback poll every 60 seconds
            }
        }
    }

    private fun startLiveTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var lastCheckedDay = getTodayString()
            while (true) {
                val today = getTodayString()
                if (today != lastCheckedDay) {
                    lastCheckedDay = today
                    checkMidnightAndLoadSessions()
                }
                
                val currentSessions = _sessions.value
                if (currentSessions.isNotEmpty() && currentSessions.first().outTime == null) {
                    val inTimeMs = currentSessions.first().inTime
                    val elapsedSeconds = (System.currentTimeMillis() - inTimeMs) / 1000
                    _liveActiveElapsedSeconds.value = Math.max(0L, elapsedSeconds)

                    // Auto-trigger target reached alarm when reaching 7h 50m today while punched in
                    val workedMinutesToday = calculateWorkedMinutes(currentSessions)
                    if (workedMinutesToday >= TimeUtils.REQUIRED_MINUTES) {
                        val todayStr = getTodayString()
                        if (prefs.alarmedDay != todayStr) {
                            prefs.alarmedDay = todayStr
                            notificationHelper.sendNotification(
                                "🎉 Target Reached! Time to leave!",
                                "You've worked ${TimeUtils.fmtDur(workedMinutesToday)} today",
                                isTimer = true
                            )
                        }
                    }
                } else {
                    _liveActiveElapsedSeconds.value = 0L
                }
                delay(1000)
            }
        }
    }

    fun punch() {
        if (isPunching) return
        isPunching = true
        viewModelScope.launch {
            try {
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
                        val alreadyAlarmed = prefs.alarmedDay == todayStr
                        if (!alreadyAlarmed) {
                            prefs.alarmedDay = todayStr
                        }
                        notificationHelper.sendNotification(
                            "🎉 Time to leave!",
                            "You've worked ${TimeUtils.fmtDur(workedMinutesToday)} today",
                            isTimer = !alreadyAlarmed
                        )
                    }
                }
                // Reload
                checkMidnightAndLoadSessions()
            } finally {
                delay(500)
                isPunching = false
            }
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
            checkMidnightAndLoadSessions()
        }
    }

    fun updateSession(session: Session) {
        viewModelScope.launch {
            sessionDao.updateSession(session)
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
        sessionLoadJob?.cancel()
        pomodoroJob?.cancel()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    // Pomodoro logic
    fun startPomodoro() {
        if (_pomodoroIsRunning.value) return
        _pomodoroIsRunning.value = true
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (_pomodoroIsRunning.value && _pomodoroRemainingSeconds.value > 0) {
                delay(1000)
                if (_pomodoroIsRunning.value) {
                    val nextSeconds = _pomodoroRemainingSeconds.value - 1
                    _pomodoroRemainingSeconds.value = nextSeconds
                    if (nextSeconds <= 0) {
                        _pomodoroIsRunning.value = false
                        val modeName = _pomodoroMode.value.displayName
                        notificationHelper.sendNotification(
                            "⏱ Pomodoro Timer Finished",
                            "Your $modeName session is complete.",
                            isTimer = true
                        )
                        break
                    }
                }
            }
        }
    }

    fun pausePomodoro() {
        _pomodoroIsRunning.value = false
        pomodoroJob?.cancel()
    }

    fun resetPomodoro() {
        pausePomodoro()
        _pomodoroRemainingSeconds.value = _pomodoroMode.value.durationMinutes * 60
    }

    fun setPomodoroMode(mode: PomodoroMode) {
        pausePomodoro()
        _pomodoroMode.value = mode
        _pomodoroTotalSeconds.value = mode.durationMinutes * 60
        _pomodoroRemainingSeconds.value = mode.durationMinutes * 60
    }

    fun adjustPomodoroTime(amountSeconds: Int) {
        val current = _pomodoroRemainingSeconds.value
        val next = Math.max(0, current + amountSeconds)
        _pomodoroRemainingSeconds.value = next
        if (next > _pomodoroTotalSeconds.value) {
            _pomodoroTotalSeconds.value = next
        }
    }

    // Tasks logic
    fun loadTasks() {
        viewModelScope.launch {
            taskDao.getTasksFlow().collect {
                _tasks.value = it
            }
        }
    }

    fun addTask(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(Task(text = text.trim()))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}

data class ManualCalcState(
    val arrivalMin: Int,
    val lunchDur: Int,
    val leaveMin: Int,
    val amWork: Int,
    val pmWork: Int
)

enum class PomodoroMode(val displayName: String, val durationMinutes: Int) {
    WORK("Work", 25),
    BREAK_5("Break (5m)", 5),
    BREAK_10("Break (10m)", 10),
    BREAK_15("Break (15m)", 15)
}
