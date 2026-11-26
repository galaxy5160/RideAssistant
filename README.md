# RideAssistant

Offline-first Android driver intelligence & real-time ride offer assistant built with Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, and Android AccessibilityService.

## Key Features
- **100% Offline & Private**: No `INTERNET` permission, no cloud APIs, no external AI, no telemetry.
- **Event-Driven Accessibility Capture**: Detects ride offers on Ola, Uber, Rapido, Porter, and inDrive without continuous polling.
- **Instant Decision Pipeline**: Extracts `TextTree`, parses fare/pickup/ride distance, evaluates rules, calculates profit & score (`GOOD` / `CHECK` / `LOW` / `UNKNOWN`).
- **Home Distance & Home Priority**: Calculates straight-line Haversine distance from ride destination to locally saved HOME coordinates.
- **Floating Bubble & Alerts**: Optional `WindowManager` overlay and high-priority local notifications.
- **Offline History & Analytics**: Room database persistence, earnings summary, and real-time diagnostics.

## Build Instructions
1. Open `/data/user/0/ai.antigem/files/home/.gemini/antigravity-cli/scratch/RideAssistant` in Android Studio (or build via Gradle/GitHub Actions with Android SDK 35 & JDK 17).
2. Run unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. Build Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
