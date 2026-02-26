import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.lume"
    compileSdk = 35

    // 🔥 SIMULANDO EL MISMO PROBLEMA QUE TU PROYECTO REAL
    val secretProperties = Properties()

    secretProperties.setProperty(
        "SIGNING_KEYSTORE_PASSWORD",
        System.getenv("SIGNING_KEYSTORE_PASSWORD")
    )

    secretProperties.setProperty(
        "SIGNING_KEY_ALIAS",
        System.getenv("SIGNING_KEY_ALIAS")
    )

    secretProperties.setProperty(
        "SIGNING_KEY_PASSWORD",
        System.getenv("SIGNING_KEY_PASSWORD")
    )

    signingConfigs {
        create("releaseConfig") {
            storeFile = file("../dummy.jks")
            storePassword = secretProperties["SIGNING_KEYSTORE_PASSWORD"] as String
            keyAlias = secretProperties["SIGNING_KEY_ALIAS"] as String
            keyPassword = secretProperties["SIGNING_KEY_PASSWORD"] as String
        }
    }

    defaultConfig {
        applicationId = "com.example.lume"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:9100/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("releaseConfig")

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
}

dependencies {
    implementation(libs.androidx.core.ktx)
}