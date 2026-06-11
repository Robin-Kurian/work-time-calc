package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MainActivity
import com.example.utils.NotificationHelper

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_STOP_ALARM) return

        NotificationHelper.stopAlarmGlobally(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_STOP_ALARM, true)
        }
        context.startActivity(openIntent)
    }

    companion object {
        const val ACTION_STOP_ALARM = "com.example.action.STOP_ALARM"
        const val EXTRA_STOP_ALARM = "extra_stop_alarm"
    }
}
