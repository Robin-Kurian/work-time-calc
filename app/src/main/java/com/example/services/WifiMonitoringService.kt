package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.PreferencesHelper
import com.example.utils.AutoPunchManager
import com.example.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WifiMonitoringService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var connectCheckJob: Job? = null
    private var disconnectCheckJob: Job? = null
    private var periodicCheckJob: Job? = null

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var prefs: PreferencesHelper

    companion object {
        private const val CHANNEL_ID = "wifi_monitoring_service_channel"
        private const val NOTIFICATION_ID = 9999
        private const val DISCONNECT_DEBOUNCE_MS = 3000L
        private const val CONNECT_DEBOUNCE_MS = 1500L
        private const val PERIODIC_CHECK_MS = 60_000L
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scheduleConnectCheck()
        }

        override fun onLost(network: Network) {
            scheduleDisconnectCheck()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return
            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) {
                scheduleDisconnectCheck()
                return
            }
            scheduleConnectCheck()
        }
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        prefs = PreferencesHelper(this)

        if (!PermissionUtils.hasLocationPermission(this)) {
            stopSelf()
            return
        }

        createNotificationChannel()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        runWifiCheck(delayMs = 0)
        startPeriodicWifiChecks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!PermissionUtils.hasLocationPermission(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val notification = createNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        connectCheckJob?.cancel()
        disconnectCheckJob?.cancel()
        periodicCheckJob?.cancel()
        serviceJob.cancel()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleConnectCheck() {
        disconnectCheckJob?.cancel()
        connectCheckJob?.cancel()
        connectCheckJob = serviceScope.launch {
            delay(CONNECT_DEBOUNCE_MS)
            AutoPunchManager.checkWifiConnection(applicationContext)
        }
    }

    private fun scheduleDisconnectCheck() {
        connectCheckJob?.cancel()
        disconnectCheckJob?.cancel()
        disconnectCheckJob = serviceScope.launch {
            delay(DISCONNECT_DEBOUNCE_MS)
            AutoPunchManager.checkWifiConnection(applicationContext)
        }
    }

    private fun runWifiCheck(delayMs: Long) {
        disconnectCheckJob?.cancel()
        disconnectCheckJob = serviceScope.launch {
            if (delayMs > 0) delay(delayMs)
            AutoPunchManager.checkWifiConnection(applicationContext)
        }
    }

    private fun startPeriodicWifiChecks() {
        periodicCheckJob?.cancel()
        periodicCheckJob = serviceScope.launch {
            while (true) {
                delay(PERIODIC_CHECK_MS)
                AutoPunchManager.checkWifiConnection(applicationContext)
            }
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Work Time Auto-Tracker")
            .setContentText("Monitoring Wi-Fi in the background...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Auto-Tracker Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for Wi-Fi auto-tracking service"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
