# KMPStarterKit — Kotlin + Compose Multiplatform Starter Kit (android, ios, web, desktop)

Compose Multiplatform mobile application targeting Android, iOS, Web (WASM), and JVM Desktop.

## Module Structure

```
├── shared/            # Shared KMP library (commonMain, androidMain, iosMain, webMain, jvmMain)
├── androidApp/        # Android Application entry point
├── desktopApp/        # JVM Desktop entry point
├── webApp/            # Wasm/JS browser entry point
├── designsystem/      # Reusable UI components library
├── libs/
│   └── subscription/  # Subscriptions (API + RevenueCat/Adapty implementations)
├── build-logic/       # Convention plugins for KMP library modules
├── iosApp/            # Xcode project wrapper
├── distribution/      # Release assets (keystore, what's new)
├── scripts/           # Build/release helper scripts
└── fastlane/          # iOS release automation
```

## Prerequisites

- JDK 17 or higher (required by AGP 9)
- Android Studio Narwhal Feature Drop or newer (for AGP 9 support)
- Android SDK path configured in `local.properties` (`sdk.dir=/path/to/sdk`)
- For iOS: Xcode installed
- Optional: Check your system with [KDoctor](https://github.com/Kotlin/kdoctor)

## Before Running

Add `local.properties` file to this directory with:
```properties
sdk.dir=/path/to/android/sdk
GOOGLE_WEB_CLIENT_ID=test
```

**Firebase config.** The committed [androidApp/google-services.json](androidApp/google-services.json) and
[iosApp/iosApp/GoogleService-Info.plist](iosApp/iosApp/GoogleService-Info.plist) are placeholder templates —
the app builds with them, but Firebase features (auth, push, analytics) need real values. Download both
files for your own Firebase project from the [Firebase console](https://console.firebase.google.com/)
(Project settings → Your apps) and replace the placeholders. Never commit real Firebase config to a public
fork — keep secrets in `local.properties` (already gitignored), which CI also uses.

## Android

To run the application on android device/emulator:
- Open this directory in Android Studio and run the imported `androidApp` run configuration

To build the application bundle:
- Run `./gradlew :androidApp:assembleDebug`
- Find `.apk` file in `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

Useful commands:
```bash
# Debug APK
./gradlew :androidApp:assembleDebug

# Unit tests (shared module)
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest

# Android UI tests (device required)
./gradlew :androidApp:connectedDebugAndroidTest

# SHA1 for Firebase
./gradlew :androidApp:signingReport
```

## iOS

To run the application on iPhone device/simulator:
- Open `iosApp/iosApp.xcproject` in Xcode and run standard configuration
- Or use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) for Android Studio

```bash
# iOS simulator unit tests
./gradlew :shared:iosSimulatorArm64Test
```

## JVM Desktop

Run the full Compose Desktop app:
```bash
./gradlew :desktopApp:run
```

To preview design system components, run `designsystem/src/jvmMain/kotlin/Main.kt` from IDE.

## Web (Wasm/JS)

Dev server (live reload):
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Production bundle:
```bash
./gradlew :webApp:wasmJsBrowserDistribution
# Output: webApp/build/dist/wasmJs/productionExecutable/
```

## Screen Generation

Generate boilerplate for a new screen and wire it into navigation + DI in one step (run from `MobileApp/`):
```bash
./scripts/generate_screen.sh YourScreenName
```
The script creates `*Screen.kt`, `*UiState.kt`, `*ViewModel.kt`, registers the route, inserts the `entry<>` block in `AppNavigation.kt`, and adds `viewModelOf(::YourScreenNameViewModel)` to `root/Di.kt`.

## Refactor Package / App ID

Rename the app's package / `applicationId` / iOS bundle ID + display name across the whole project (run from `MobileApp/`):
```bash
./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp                    # full refactor
./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp --skip-package-rename   # IDs + name only
```
`--app-id` / `--app-name` are required. `--skip-package-rename` (default off) keeps Kotlin packages. Edits files in place (commit/back up first; it prompts unless `-y`). See [Scripts docs](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/scripts) for all options.

## Quality Gates

PRs are gated by `.github/workflows/pr_checks.yml` (at the repo root, **not** under `MobileApp/`). Run the same checks locally before pushing:

```bash
# Format / lint (auto-fix locally)
./gradlew spotlessApply
./gradlew spotlessCheck

# Unit + Compose UI tests (commonTest + jvmTest + androidHostTest)
./gradlew :shared:jvmTest :shared:testAndroidHostTest
```

Optional (local only, not part of PR checks): Roborazzi screenshot comparison. Record baselines with `./gradlew :shared:recordRoborazziAndroidHostTest`, compare later runs with `./gradlew :shared:verifyRoborazziAndroidHostTest`. Goldens are not committed.

`@Preview`s under `com.kotlinfoundation.koko.*` are scanned automatically. Use `androidx.compose.ui.tooling.preview.Preview` (multiplatform-aware as of CMP 1.10) — the deprecated `org.jetbrains.compose.ui.tooling.preview.Preview` is **not** discovered.

## Store Screenshot Generation

Render every `@Preview @StoreScreenshot` composable at App Store / Play Store pixel sizes — the screen as it renders, no marketing chrome added (no Fastlane / ImageMagick):

```bash
./scripts/generate_store_screenshots.sh
```

Output lands at `distribution/store_screenshots/<locale>/<device>/*.png` — pure screen captures at storefront pixel dimensions, upload-ready. Add a new screenshot by writing a `@Preview @StoreScreenshot @Composable` function next to the screen it previews (typically inside `HomeScreen.kt`, `GalleryScreen.kt`, etc.):

```kotlin
import androidx.compose.ui.tooling.preview.Preview
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.util.StoreDevice
import com.kotlinfoundation.koko.util.StoreScreenshot

@Preview
@StoreScreenshot(device = StoreDevice.IPHONE_6_5, locale = "en", tag = "01-home")
@Composable
private fun HomeStoreScreenshot_iPhone_en() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(creditBalance = 12), onUiEvent = {})
    }
}
```

Storefront previews are excluded from regression screenshot tests and only run when the script (or `-PgenerateStoreScreenshots=true`) is set.
