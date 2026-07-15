---
name: run-the-app
description: Build and run the app on Android, JVM Desktop, Web, or iOS from source. Use when getting the KMP starter kit running for the first time, or when the user asks to build, launch, or run the app on a device/emulator.
---

# Run the app

All gradle commands run from `MobileApp/`.

## Prerequisites

- **JDK 17+** (first run also downloads the JetBrains JDK + Compose — expect a slow initial build).
- **Android SDK** installed (Android Studio bundles it).
- **`sdk.dir`** set in `MobileApp/local.properties`:
  ```properties
  sdk.dir=/Users/you/Library/Android/sdk
  ```
  Without this, any Android task fails immediately with "SDK location not found".
- **JetBrains Kotlin Multiplatform plugin** in Android Studio (`Settings → Plugins → Marketplace →` search "Kotlin Multiplatform", install, restart). This is what surfaces the non-Android run targets — without it Android Studio shows only the **Android** target.
- iOS also needs Xcode (macOS only).

## Run per platform

**Android** (emulator running or device connected):
```bash
./gradlew :androidApp:assembleDebug
# APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk
# then install: ./gradlew :androidApp:installDebug
# SHA1 (for Firebase later): ./gradlew :androidApp:signingReport
```

**JVM Desktop** (fastest way to see the app — no emulator/device):
```bash
./gradlew :desktopApp:run
```

**Web (Wasm/JS)** — dev server, opens in browser:
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

**iOS** (macOS only) — open the Xcode project and run from Xcode:
```bash
open iosApp/iosApp.xcodeproj
```
Do NOT run iOS builds for routine validation — they are slow. Only when the change is iOS-specific.

## First-run failures

- `SDK location not found` → `sdk.dir` missing/wrong in `MobileApp/local.properties`.
- `Unsupported class file major version` / toolchain errors → JDK below 17; check `java -version`.
- Android task with no device → start an emulator (or connect a device) before `installDebug`.
- Slowest builds are the first one (dependency + JDK download) and iOS — this is expected.

Desktop (`:desktopApp:run`) is the quickest sanity check that the app builds and shows the Home screen.

## Next

Once it runs, you're at step A of the **`getting-started`** guide — continue there: rebrand with
**`refactor-package`**, then build your features with **`build-features`**. If you don't have a product
defined yet (`AiGuidelines/project/prd.md` is still blank), start with the **`new-app`** skill.
