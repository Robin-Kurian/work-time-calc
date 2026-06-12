plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

// macOS creates AppleDouble (._*) files on non-APFS volumes (e.g. exFAT external drives),
// which break Android resource parsing and KSP incremental output copying.
tasks.register("removeAppleDoubleFiles") {
    doLast {
        listOf(layout.buildDirectory.get().asFile, project.projectDir).forEach { root ->
            if (!root.exists()) return@forEach
            root.walkTopDown()
                .filter { it.isFile && it.name.startsWith("._") }
                .forEach { it.delete() }
        }
    }
}

tasks.matching { it.name.startsWith("ksp") }.configureEach {
    dependsOn("removeAppleDoubleFiles")
}
tasks.matching { it.name.contains("package") && it.name.contains("Resources") }.configureEach {
    finalizedBy("removeAppleDoubleFiles")
}
tasks.matching { it.name.contains("parse") && it.name.contains("Resources") }.configureEach {
    dependsOn("removeAppleDoubleFiles")
}
tasks.matching { it.name.contains("process") && it.name.contains("Resources") }.configureEach {
    dependsOn("removeAppleDoubleFiles")
}

android {
    namespace = "com.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "3.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Material design icons (extended)
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // Excel XLSX Writer
    implementation("org.dhatim:fastexcel:0.20.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
