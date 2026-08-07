---
name: add-permission
description: Request a runtime permission (camera, gallery, location, microphone, notifications, or any other) via the app-level AppPermissionState API. Use when a screen needs to ask the user for a device permission. For full push/FCM setup (not just the notification permission prompt), use enable-notifications instead.
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
| `rememberLocationPermissionState()` | `NSLocationWhenInUseUsageDescription` |
| `rememberMicrophonePermissionState()` | `NSMicrophoneUsageDescription` |

Any other permission: `rememberAppPermissionState(Permission.Bluetooth)`.

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
