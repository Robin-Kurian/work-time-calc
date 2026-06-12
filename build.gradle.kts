// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// This project lives on an external non-APFS volume where macOS writes AppleDouble (._*)
// sidecar files during builds, breaking Android resource parsing and KSP file copying.
// Keep all build output on the internal drive instead.
subprojects {
    val buildKey = rootProject.name.lowercase().replace(Regex("[^a-z0-9]+"), "-")
    val modulePath = path.removePrefix(":").ifEmpty { "root" }
    layout.buildDirectory.set(
        File(System.getProperty("user.home"), ".gradle/local-builds/$buildKey/$modulePath")
    )
}
