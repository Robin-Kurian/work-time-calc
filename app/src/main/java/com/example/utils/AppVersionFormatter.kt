package com.example.utils

import com.example.BuildConfig

fun formattedAppVersionLabel(): String {
    val versionName = BuildConfig.VERSION_NAME.trim()
    return if (versionName.isNotEmpty()) "v$versionName" else "v?"
}
