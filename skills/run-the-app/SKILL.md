---
name: run-the-app
description: Build and run the app on Android, JVM Desktop, Web, or iOS from source. Use when getting the KMP starter kit running for the first time, or when the user asks to build, launch, or run the app on a device/emulator.
---

# Run the app

All gradle commands run from `MobileApp/`.

## Prerequisites

- **JDK 17+** (first run also downloads the JetBrains JDK + Compose — expect a slow initial build).
- **Android SDK** installed (Android Studio bundles it).
- **`sdk.dir`** set in `MobileApp/local.properties`. That file is gitignored, so a fresh clone has none —
  copy the committed template and set the one value everyone needs:
  ```bash
  cp MobileApp/local.properties.example MobileApp/local.properties   # then edit sdk.dir
  ```
  ```properties
  sdk.dir=/Users/you/Library/Android/sdk
  ```
  Without this, any Android task fails immediately with "SDK location not found". Every other key in the
  template is optional and phase-tagged (`[P1]`…`[P5]`) — leave them blank for now; see
  `configure-environment`.
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

## Run vs. verify — don't loop

`run-the-app` is for a **human** to see the app. When you (the agent) just need to confirm a change is
sound, do **not** launch it:

- **Compiling is the check.** `:androidApp:assembleDebug` (or `:desktopApp:run` for a one-off visual) is
  enough. Do **not** auto-install and launch via `adb` and poll for the process to "confirm it works" —
  the launcher Activity is `.AppActivity` (Application class `.AndroidApp`), but detecting it via adb is
  fragile and is a classic retry-loop trap. For behaviour/appearance use the **`verify-ui`** skill.
- **`run` tasks never exit** (`:desktopApp:run`, `:webApp:wasmJsBrowserDevelopmentRun`, `installDebug`+launch).
  A task that hasn't returned is **running, not hung** — start it once (background if you need the shell)
  and stop; do not kill and re-run.
- **Never run `check` / `build` / `clean build`** to validate — they aggregate every target (incl. iOS) and
  a single iOS cache failure fails the whole thing, which loops. See `run-quality-gates` for the scoped gates.
- A failed/slow command is **not** a retry signal: read the error, fix or STOP. First build is slow
  (JBR + Compose download) — expected.

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
