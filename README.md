# Lume - Android Application

Lume is a financial intelligence application built with modern Android technologies. This project uses **Jetpack Compose** for the UI and follows the **MVVM (Model-View-ViewModel)** architecture.

## 🛠 Project Configuration

- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Language**: Kotlin 2.0+
- **Build System**: Gradle (Kotlin DSL)

## 🏗 Architecture

The application follows the recommended **App Architecture Guide**:

- **UI Layer (`ui/`)**:
  - **Screens**: composable functions representing each screen (`Dashboard`, `ShareReceiver`).
  - **Components**: Reusable UI elements (`FinanceBottomBar`).
  - **Navigation**: Single Activity architecture using `Navigation Compose`.
  - **Theme**: Custom Material 3 theme with forced Dark Mode.
- **ViewModel Layer (`viewmodel/`)**:
  - Manages UI state and business logic.
  - Exposes state via `StateFlow`.
  - Handles coroutines and background operations (e.g., OCR processing).
- **Data Layer (`data/`)**:
  - Data models and repositories (currently `OcrModels`).

## 📦 Key Libraries

- **Jetpack Compose**: Modern toolkit for building native UI.
- **Material 3**: Design system components.
- **ML Kit Text Recognition**: On-device OCR for scanning receipts.
- **Coil**: Image loading library for Android backed by Kotlin Coroutines.
- **Navigation Compose**: Navigation component for Jetpack Compose.

## 🚀 Getting Started

1.  Clone the repository.
2.  Open in Android Studio (Koala or newer recommended).
3.  Sync Gradle project.
4.  Run on an emulator or device (API 24+).
