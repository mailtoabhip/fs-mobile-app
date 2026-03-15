# Technology Stack

## Build System
- **Build Tool**: Gradle 8.5.0
- **Android Gradle Plugin**: 8.5.0
- **Kotlin Version**: 1.9.10

## SDK Configuration
- **Compile SDK**: 35
- **Target SDK**: 35
- **Min SDK**: 24
- **Java Version**: 11

## Core Technologies
- **Language**: Kotlin with Java interop
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Dagger 2 (v2.22.1)
- **Reactive Programming**: RxJava 2 (v2.1.7) with RxAndroid (v2.1.0)
- **Networking**: Retrofit 2 (v2.3.0) with OkHttp (v3.9.0)
- **Database**: Room (v2.6.1) with RxJava support
- **Coroutines**: Kotlin Coroutines (v1.4.1)

## UI & Components
- **UI Framework**: AndroidX with Material Design (v1.11.0)
- **Data Binding**: Android Data Binding enabled
- **Image Loading**: Glide (v4.11.0)
- **Splash Screen**: AndroidX Core SplashScreen (v1.0.1)
- **Fragments**: Fragment KTX (v1.8.9)

## Firebase & Analytics
- **Firebase**: Core, Crashlytics, Analytics, Performance, Messaging, Dynamic Links
- **Analytics**: MoEngage SDK (v12.10.02)
- **In-App Messaging**: Firebase In-App Messaging Display

## Background Processing
- **Work Manager**: AndroidX Work Runtime KTX (v2.9.0)
- **Location Services**: Google Play Services Location (v21.3.0)

## Development Tools
- **Debugging**: Chucker (v3.5.2) for network inspection (debug builds only)
- **Proguard**: Enabled for release builds with resource shrinking

## Build Variants
- **Flavors**: development, uat, production
- **Build Types**: debug, release, releaseDebuggable
- **Features by Flavor**:
  - development/uat: Firebase disabled, performance logging enabled, resource configs limited
  - production: Firebase enabled, full resources, signed release builds

## Common Commands

### Build Commands
```bash
# Build debug APK for development flavor
./gradlew assembleDevelopmentDebug

# Build release APK for production flavor
./gradlew assembleProductionRelease

# Build all variants
./gradlew assembleRelease
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run tests for specific flavor
./gradlew testDevelopmentDebugUnitTest
```

### Cleaning & Installation
```bash
# Clean build artifacts
./gradlew clean

# Install debug build on connected device
./gradlew installDevelopmentDebug

# Uninstall app
adb uninstall com.delhivery.axle
```

### Debugging & Analysis
```bash
# Generate dependency report
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Lint checks
./gradlew lint
```

## APK Naming Convention
APKs are automatically named with the format:
```
Axle_{flavor}_{buildType}_{version}_{date}_{time}.apk
```
Example: `Axle_production_release_2.133.13_150326_1430.apk`

## Security Features
- Root detection enabled in release builds
- ProGuard obfuscation for release builds
- Signed releases with keystore
- Firebase Crashlytics for production monitoring
