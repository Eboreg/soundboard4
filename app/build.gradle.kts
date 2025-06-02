import java.io.FileInputStream
import java.util.Properties

val currentVersionCode = 2
val currentVersionName = "0.1.1"
val keystoreProperties = Properties()

try {
    keystoreProperties.load(FileInputStream(rootProject.file("keystore.properties")))
} catch (_: Throwable) {
    keystoreProperties["storeFile"] = "/dev/null"
    keystoreProperties["storePassword"] = ""
    keystoreProperties["keyAlias"] = ""
    keystoreProperties["keyPassword"] = ""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "us.huseli.soundboard4"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    defaultConfig {
        applicationId = "us.huseli.soundboard4"
        minSdk = 26
        targetSdk = 35
        versionCode = currentVersionCode
        versionName = currentVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.preference.ktx)

    // Theme etc:
    implementation(libs.retain.theme)

    // Splashscreen:
    implementation(libs.androidx.core.splashscreen)

    // Room:
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Hilt:
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Media3:
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)

    // ffmpeg:
    implementation(files("ffmpeg-kit.aar"))
    implementation(libs.smart.exception.java)

    // Material:
    implementation(libs.androidx.material3)
    implementation(libs.material.icons.extended)

    // Color picker:
    implementation(libs.compose.colorpicker)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
