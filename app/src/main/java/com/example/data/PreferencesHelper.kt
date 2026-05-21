package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wtc_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ARRIVAL = "wtc_arr"
        private const val KEY_LUNCH_OUT = "wtc_lo"
        private const val KEY_LUNCH_IN = "wtc_li"
        private const val KEY_WORK_SSID = "work_ssid"
        private const val KEY_TRACKED_DAY = "s_day"
        private const val KEY_AUTO_TRACKING = "auto_tracking_enabled"
    }

    var arrivalTime: String
        get() = prefs.getString(KEY_ARRIVAL, "09:00") ?: "09:00"
        set(value) = prefs.edit().putString(KEY_ARRIVAL, value).apply()

    var lunchOutTime: String
        get() = prefs.getString(KEY_LUNCH_OUT, "12:00") ?: "12:00"
        set(value) = prefs.edit().putString(KEY_LUNCH_OUT, value).apply()

    var lunchInTime: String
        get() = prefs.getString(KEY_LUNCH_IN, "13:00") ?: "13:00"
        set(value) = prefs.edit().putString(KEY_LUNCH_IN, value).apply()

    var workSsid: String
        get() = prefs.getString(KEY_WORK_SSID, "ISPG_Staff") ?: "ISPG_Staff"
        set(value) = prefs.edit().putString(KEY_WORK_SSID, value).apply()

    var trackedDay: String
        get() = prefs.getString(KEY_TRACKED_DAY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TRACKED_DAY, value).apply()

    var isAutoTrackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_TRACKING, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_TRACKING, value).apply()
}
