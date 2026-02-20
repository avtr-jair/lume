plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lume"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lume"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
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
}