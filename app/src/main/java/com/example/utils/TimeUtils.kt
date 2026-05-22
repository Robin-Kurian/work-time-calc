package com.example.utils

import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object TimeUtils {

    // REQUIRED_MINUTES = 470 (7h 50m)
    const val REQUIRED_MINUTES = 470
    const val BUFFER_IN = 5
    const val BUFFER_OUT = 5

    // "HH:MM" → total minutes
    fun toMin(t: String): Int {
        val parts = t.split(":")
        if (parts.size < 2) return 0
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }

    // total minutes → "h:mmam/pm"
    // MUST wrap values >= 1440 using % 1440 to handle past-midnight times
    fun toTime(m: Int): String {
        val wrapped = ((m % 1440) + 1440) % 1440
        val h = wrapped / 60
        val mm = wrapped % 60
        val h12 = if (h > 12) h - 12 else if (h == 0) 12 else h
        val ap = if (h >= 12) "pm" else "am"
        return String.format(Locale.US, "%d:%02d%s", h12, mm, ap)
    }

    // minutes → "Xh Ym" or "Ym"
    fun fmtDur(m: Int): String {
        val rounded = Math.max(0, m)
        return if (rounded < 60) {
            "${rounded}m"
        } else {
            val h = rounded / 60
            val mins = rounded % 60
            if (mins == 0) "${h}h" else "${h}h ${mins}m"
        }
    }

    // timestamp ms → "h:mmam/pm"
    fun fmtTimestamp(ms: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = ms
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        return toTime(h * 60 + m)
    }

    // current time as "HH:MM"
    fun nowHHMM(): String {
        val calendar = Calendar.getInstance()
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    // current time in minutes from midnight
    fun nowMinutes(): Int {
        val calendar = Calendar.getInstance()
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        return h * 60 + m
    }

    // elapsed seconds → "m:ss" or "h:mm:ss"
    fun fmtElapsed(seconds: Long): String {
        val hh = seconds / 3600
        val mm = (seconds % 3600) / 60
        val ss = seconds % 60
        return if (hh > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hh, mm, ss)
        } else {
            String.format(Locale.US, "%d:%02d", mm, ss)
        }
    }

    // Update the hour and minute of a given timestamp, keeping the date the same
    fun updateTimeOfTimestamp(originalTimestamp: Long, selectedHour: Int, selectedMinute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = originalTimestamp
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

