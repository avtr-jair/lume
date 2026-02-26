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
        // --- Machine Learning ---
    // ML Kit Text Recognition for OCR features (Scanning receipts)
    implementation(libs.text.recognition)

    // --- Android Core & Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose ---
    // BOM (Bill of Materials) to manage Compose versions automatically
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Material Design 3 components
    
    // --- Legacy / Compatibility ---
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Third Party & Extensions ---
    // Coil: Image loading for Compose (AsyncImage)
    implementation(libs.coil.compose)
    // Navigation: Jetpack Navigation for Compose
    implementation(libs.androidx.navigation.compose)
    // Extended Icons: Additional Material Icons (Outlined, Filled, etc.)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // --- Coroutines ---
    // Android specific coroutines (Dispatchers.Main)
    implementation(libs.kotlinx.coroutines.android)
    // Play Services integration for Coroutines (Tasks API used by ML Kit)
    implementation(libs.kotlinx.coroutines.play.services)

    // --- Networking & Serialization ---
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlin.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
}