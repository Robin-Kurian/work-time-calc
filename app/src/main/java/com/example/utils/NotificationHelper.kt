package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        private const val PUNCH_CHANNEL_ID = "work_time_tracker_punch_alerts_v3"
        private const val PUNCH_CHANNEL_NAME = "Punch Alerts"
        private const val TIMER_CHANNEL_ID = "work_time_tracker_timer_alerts_v3"
        private const val TIMER_CHANNEL_NAME = "Timer Alerts"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // 1. General Punch channel
                val punchChannel = NotificationChannel(
                    PUNCH_CHANNEL_ID,
                    PUNCH_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for punch in, punch out, and target reached events"
                }
                notificationManager.createNotificationChannel(punchChannel)

                // 2. Loud Timer channel using system default alarm sound
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build()

                val timerChannel = NotificationChannel(
                    TIMER_CHANNEL_ID,
                    TIMER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Loud alarm alerts when the focus or break timer finishes"
                    setSound(alarmUri, audioAttributes)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                }
                notificationManager.createNotificationChannel(timerChannel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendNotification(title: String, body: String, isTimer: Boolean = false) {
        try {
            // Intent to open the app when notification is clicked
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )

            val channelId = if (isTimer) TIMER_CHANNEL_ID else PUNCH_CHANNEL_ID
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(if (isTimer) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (isTimer) {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(alarmUri)
                builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
                builder.setCategory(NotificationCompat.CATEGORY_ALARM)

                // Vibrate device directly for 1 second as robust foreground feedback
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(1000)
                    }
                }
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

