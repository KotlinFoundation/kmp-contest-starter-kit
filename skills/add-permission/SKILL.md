---
name: add-permission
description: Request a runtime permission (notifications, camera, gallery out of the box; location, microphone or any other after adding its Calf module) via the app-level AppPermissionState API. Use when a screen needs to ask the user for a device permission. For full push/FCM setup (not just the notification permission prompt), use enable-notifications instead.
---

# Add a runtime permission

Permissions go through the app-level `AppPermissionState` wrapper (backed by Calf) — real dialogs on
Android/iOS, granted no-op on desktop/web. **Never use Calf types directly in screens.**

Helpers live in `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/permissions/AppPermissionState.kt`.

## Ready-made helpers

| Helper | iOS `Info.plist` key |
|---|---|
| `rememberNotificationPermissionState()` | — |
| `rememberCameraPermissionState()` | `NSCameraUsageDescription` |
| `rememberGalleryPermissionState()` | `NSPhotoLibraryUsageDescription` |

Those three are the permissions the kit ships. **Any other permission needs its Calf module added
first** — the kit deliberately does not depend on the umbrella `calf-permissions` artifact:

```kotlin
// MobileApp/gradle/libs.versions.toml
calf-permissions-location = { module = "com.mohamedrejeb.calf:calf-permissions-location", version.ref = "calf" }

// MobileApp/shared/build.gradle.kts, commonMain
implementation(libs.calf.permissions.location)
```

Then call the generic helper and add the iOS key:

```kotlin
rememberAppPermissionState(Permission.FineLocation)   // + NSLocationWhenInUseUsageDescription
```

> **Never switch to the umbrella `calf-permissions` module.** It links every permission API, so
> App Store review rejects the build with **ITMS-90683** demanding a purpose string for location,
> bluetooth, contacts and the rest — permissions the app never uses. One module per permission you
> actually request.

## Use it in a screen

Each helper returns `AppPermissionState { isGranted, shouldShowRationale, request(), openSettings() }`:

```kotlin
@Composable
fun CameraScreen() {
    val camera = rememberCameraPermissionState { granted -> /* result after request() */ }
    when {
        camera.isGranted -> CameraPreview()
        camera.shouldShowRationale -> RationaleCard(onAllow = { camera.openSettings() })
        else -> Button(onClick = { camera.request() }) { Text("Enable camera") }
    }
}
```

## Ask on screen entry

For permissions best requested as the screen appears (e.g. notifications on Home):

```kotlin
RequestPermissionOnEntry(rememberNotificationPermissionState())
```
It skips if already granted, or if the user previously denied (never re-prompt unasked — show a
rationale UI instead).

## iOS / Android setup

- **iOS**: add the matching `NS*UsageDescription` key to `iosApp/iosApp/Info.plist`. Camera/gallery/
  location/microphone all require one — a **missing key hard-crashes** the app when the permission opens.
  Notifications need none.
- **Android**: add the `<uses-permission>` entry to `androidApp/src/main/AndroidManifest.xml` if the
  permission requires one.

Validate with the `run-quality-gates` skill.
