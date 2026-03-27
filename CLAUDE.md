# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Youngssoo is a Kotlin Multiplatform (KMP) educational game app targeting Android and iOS. The app features math games, vocabulary games, and a reward system.

## Build Commands

```bash
# Android
./gradlew assembleDebug                    # Build Android debug APK
./gradlew assembleRelease                  # Build Android release APK
./gradlew installDebug                     # Install on connected Android device

# iOS (from iosApp directory)
cd iosApp && pod install                   # Install CocoaPods dependencies
# Open iosApp/iosApp.xcworkspace in Xcode to build/run

# Common
./gradlew build                            # Build all targets
./gradlew clean                            # Clean build artifacts
```

## Architecture

### Module Structure
- `/composeApp` - Shared Kotlin Multiplatform code with Compose UI
  - `commonMain` - Platform-agnostic code (UI, ViewModels, domain logic)
  - `androidMain` - Android-specific implementations
  - `iosMain` - iOS-specific implementations
- `/iosApp` - iOS app entry point and Swift bridging code

### Key Architectural Patterns

**Dependency Injection (Koin)**
- `di/Modules.kt` - Shared DI module definitions
- `di/Modules.android.kt` / `di/Modules.ios.kt` - Platform-specific modules
- Platform modules are `expect`/`actual` declarations

**Database (Room)**
- `core/data/local/AppDatabase.kt` - Room database definition
- Platform-specific database builders in respective `Modules.*.kt` files
- Schema files stored in `composeApp/schemas/`

**Navigation**
- State-based navigation in `App.kt` using `currentGame` state
- Routes defined in `Route.kt` as sealed interfaces

**iOS-Kotlin Interop**
- Swift bridge files in `iosApp/iosApp/Bridge/` for native SDK integration
- C interop definitions in `composeApp/src/iosMain/c_interop/SwiftProvider.def`

### Package Structure
```
com.bium.youngssoo/
├── core/
│   ├── data/           # HTTP client, database
│   ├── domain/         # Result types, error handling
│   ├── presentation/   # Shared UI components, theme, colors
│   └── logging/
├── game/
│   ├── math/           # Math game feature
│   └── vocab/          # Vocabulary game feature
├── home/presentation/  # Home screen
├── reward/             # Reward system
└── di/                 # Dependency injection
```

## Key Dependencies

- **UI**: Jetbrains Compose Multiplatform 1.7.0
- **DI**: Koin 4.0.0
- **Networking**: Ktor 3.0.0
- **Database**: Room 2.7.0-alpha11 (multiplatform)
- **Image Loading**: Coil 3.0
- **Social Login**: Naver SDK, Kakao SDK (Android), Apple/Kakao/Naver bridges (iOS)
- **Firebase**: Messaging, Auth (Android)

## Configuration

- Android signing config reads from `local.properties` (KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)
- Keystore file: `composeApp/bium_key.jks`
- Firebase config: `composeApp/src/google-services.json`
