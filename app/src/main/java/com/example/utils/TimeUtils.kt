package com.example.utils

import com.example.data.Session
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

enum class OutsideBreakType {
    LUNCH,
    MORNING_TEA,
    EVENING_TEA,
    WARNING,
    DEDUCTED;

    fun excelTitle(): String = when (this) {
        LUNCH -> "🍔 Lunch Break"
        MORNING_TEA -> "☕ Morning Tea Break"
        EVENING_TEA -> "☕ Evening Tea Break"
        WARNING -> "⚠️ Outside (Warning)"
        DEDUCTED -> "Outside (Deducted)"
    }

    fun breakLabel(duration: String): String = when (this) {
        LUNCH -> "Lunch break for $duration"
        MORNING_TEA -> "Tea break for $duration"
        EVENING_TEA -> "Tea break for $duration"
        WARNING -> "Outside for $duration"
        DEDUCTED -> "Outside for $duration"
    }

    fun breakPrefixEmoji(): String? = when (this) {
        LUNCH -> "🍔"
        MORNING_TEA -> "☕"
        EVENING_TEA -> "☕"
        WARNING -> "⚠️"
        DEDUCTED -> null
    }

    fun excelStatus(): String = when (this) {
        LUNCH -> "Lunch"
        MORNING_TEA -> "Morning Tea"
        EVENING_TEA -> "Evening Tea"
        WARNING -> "Warning"
        DEDUCTED -> "Deducted"
    }
}

object TimeUtils {

    // REQUIRED_MINUTES = 470 (7h 50m)
    const val REQUIRED_MINUTES = 470
    const val BUFFER_IN = 5
    const val BUFFER_OUT = 5
    private const val MORNING_TEA_START_MIN = 8 * 60     // 8:00 am
    private const val MORNING_TEA_END_MIN = 11 * 60      // 11:00 am inclusive
    private const val LUNCH_WINDOW_START_MIN = 12 * 60   // 12:00 pm
    private const val LUNCH_WINDOW_END_MIN = 15 * 60     // 3:00 pm exclusive
    private const val EVENING_TEA_START_MIN = 15 * 60    // 3:00 pm
    private const val EVENING_TEA_END_MIN = 18 * 60      // 6:00 pm inclusive
    private const val TEA_MIN_OUTSIDE_SEC = 100          // >= 1m 40s
    private const val LUNCH_MIN_OUTSIDE_SEC = 19 * 60    // > 19 minutes
    private const val WARNING_MIN_OUTSIDE_SEC = 10 * 60  // > 10 minutes

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

    // seconds → e.g. "45s", "1m 20s", "1h 5m 20s"
    fun fmtDurSeconds(totalSeconds: Long): String {
        val rounded = Math.max(0L, totalSeconds)
        if (rounded < 60) {
            return "${rounded}s"
        }
        val h = rounded / 3600
        val m = (rounded % 3600) / 60
        val s = rounded % 60

        return buildString {
            if (h > 0) {
                append("${h}h")
            }
            if (m > 0) {
                if (isNotEmpty()) append(" ")
                append("${m}m")
            }
            if (s > 0) {
                if (isNotEmpty()) append(" ")
                append("${s}s")
            }
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

    private fun outMinutesFromMidnight(outTimeMs: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = outTimeMs
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    fun classifyOutsideBreak(outTimeMs: Long, outsideSeconds: Long): OutsideBreakType {
        val outMin = outMinutesFromMidnight(outTimeMs)

        if (outsideSeconds > LUNCH_MIN_OUTSIDE_SEC &&
            outMin in LUNCH_WINDOW_START_MIN until LUNCH_WINDOW_END_MIN
        ) {
            return OutsideBreakType.LUNCH
        }

        if (outsideSeconds >= TEA_MIN_OUTSIDE_SEC &&
            outMin in MORNING_TEA_START_MIN..MORNING_TEA_END_MIN
        ) {
            return OutsideBreakType.MORNING_TEA
        }

        if (outsideSeconds >= TEA_MIN_OUTSIDE_SEC &&
            outMin in EVENING_TEA_START_MIN..EVENING_TEA_END_MIN
        ) {
            return OutsideBreakType.EVENING_TEA
        }

        if (outsideSeconds > WARNING_MIN_OUTSIDE_SEC) {
            return OutsideBreakType.WARNING
        }

        return OutsideBreakType.DEDUCTED
    }

    fun isLunchBreak(outTimeMs: Long, outsideSeconds: Long): Boolean =
        classifyOutsideBreak(outTimeMs, outsideSeconds) == OutsideBreakType.LUNCH

    fun formatOutsideBreak(outTimeMs: Long, outsideSeconds: Long): String {
        val type = classifyOutsideBreak(outTimeMs, outsideSeconds)
        val duration = fmtDurSeconds(outsideSeconds)
        val label = type.breakLabel(duration)
        val emoji = type.breakPrefixEmoji()
        return if (emoji != null) "$emoji $label" else label
    }

    /** Sum of all outside gaps between consecutive sessions (oldest → newest). */
    fun totalOutsideSeconds(sessions: List<Session>): Long {
        if (sessions.size < 2) return 0L
        var total = 0L
        val chronological = sessions.reversed()
        chronological.forEachIndexed { index, session ->
            if (index < chronological.size - 1) {
                val outTime = session.outTime ?: return@forEachIndexed
                val outsideSec = (chronological[index + 1].inTime - outTime) / 1000
                if (outsideSec > 0) total += outsideSec
            }
        }
        return total
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

