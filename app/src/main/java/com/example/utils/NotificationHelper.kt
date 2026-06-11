package com.example.utils

import android.app.KeyguardManager
import android.app.Notification
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
import com.example.receivers.AlarmStopReceiver

class NotificationHelper(private val context: Context) {

    companion object {
        private const val PUNCH_CHANNEL_ID = "work_time_tracker_punch_alerts_v3"
        private const val PUNCH_CHANNEL_NAME = "Punch Alerts"
        private const val TIMER_CHANNEL_ID = "work_time_tracker_timer_alerts_v4"
        private const val TIMER_CHANNEL_NAME = "Timer Alerts"
        const val ALARM_NOTIFICATION_ID = 9001

        @Volatile
        private var activeRingtone: android.media.Ringtone? = null

        @Volatile
        private var activeVibrator: Vibrator? = null

        fun isDeviceLocked(context: Context): Boolean {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardLocked
        }

        fun stopAlarmGlobally(context: Context) {
            stopRingtoneAndVibration()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(ALARM_NOTIFICATION_ID)
        }

        private fun stopRingtoneAndVibration() {
            try {
                activeRingtone?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                }
                activeRingtone = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                activeVibrator?.cancel()
                activeVibrator = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val punchChannel = NotificationChannel(
                    PUNCH_CHANNEL_ID,
                    PUNCH_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for punch in, punch out, and target reached events"
                }
                notificationManager.createNotificationChannel(punchChannel)

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
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(timerChannel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAlarmSoundAndVibration() {
        stopRingtoneAndVibration()
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.isLooping = true
                }
                ringtone.play()
                activeRingtone = ringtone
            }

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 1000, 500, 1000, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, 0)
                }
                activeVibrator = vibrator
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSoundAndVibration() {
        stopRingtoneAndVibration()
    }

    fun dismissAlarmNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
    }

    fun showAlarmNotification(title: String, body: String, silent: Boolean) {
        try {
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val contentPendingIntent = PendingIntent.getActivity(context, 2, openIntent, pendingIntentFlags)
            val fullScreenPendingIntent = PendingIntent.getActivity(context, 3, openIntent, pendingIntentFlags)

            val stopIntent = Intent(context, AlarmStopReceiver::class.java).apply {
                action = AlarmStopReceiver.ACTION_STOP_ALARM
            }
            val stopPendingIntent = PendingIntent.getBroadcast(context, 4, stopIntent, pendingIntentFlags)

            val builder = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(contentPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(
                    android.R.drawable.ic_media_pause,
                    "Stop Alarm",
                    stopPendingIntent
                )

            if (silent) {
                builder.setSilent(true)
            } else {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(alarmUri)
                builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(ALARM_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendNotification(title: String, body: String, isTimer: Boolean = false, silent: Boolean = false) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
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
                builder.setCategory(NotificationCompat.CATEGORY_ALARM)
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                if (silent) {
                    builder.setSilent(true)
                } else {
                    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    builder.setSound(alarmUri)
                    builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
                }
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
