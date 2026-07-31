---
name: enable-notifications
description: Enable push (FCM) and local notifications in the KMP app via KMPNotifier 2.0, set up iOS APNs, request the notification runtime permission, and send a test push. Use when the developer wants push notifications, local notifications, re-engagement pings, or FCM. Firebase must already be set up (integrations phase).
---

# Enable notifications (push + local)

Notifications use **KMPNotifier 2.0**, split into two modules already on the classpath:
`kmpnotifier-local` (local notifications) + `kmpnotifier-push-firebase` (FCM push). Push rides on the
Firebase project from the `integrations` phase — no new project.

## 1. Listeners (already wired)

`AppInitializer.initializeNotification()`
(`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/root/AppInitializer.kt`) registers:
- `KMPNotifier.addListener { onNotificationClicked(data) }` — user tapped a notification.
- `KMPNotifier.addPushListener { onNewToken(token); onPushNotificationWithPayloadData(...) }` — the
  device FCM token (log it) and incoming pushes.

To grab the device token for a test send, it's logged as `Firebase onNewToken: <token>` — read it
from the logs after launch. Send local notifications with the `KMPNotifier` local API when you need
an on-device reminder (no server round-trip). Deep-link taps by branching on the payload in
`onNotificationClicked`.

## 2. Runtime permission (Android 13+ / iOS)

Ask through the app-level permission wrapper, not KMPNotifier or Calf directly. Use
`rememberNotificationPermissionState()` and prime it benefit-first (see the `add-permission` skill).
The demo `HomeScreen` uses `RequestPermissionOnEntry(rememberNotificationPermissionState { ... })`.
Notifications need **no** iOS `Info.plist` usage-description key.

## 3. iOS APNs setup (User Action)

Push on iOS requires an APNs auth key uploaded to Firebase:
1. **Apple Developer portal** (https://developer.apple.com/account/) → **Certificates, Identifiers &
   Profiles → Keys → +**. Name it, check **Apple Push Notifications service (APNs)**, Continue →
   Register, and **download the `.p8`** (one-time download — keep it safe; note the Key ID + Team ID).
2. **Firebase Console → Project settings → Cloud Messaging → Apple app configuration** → upload the
   `.p8` with its Key ID and Team ID.
3. **Xcode** → app target → **Signing & Capabilities**: add **Push Notifications** and **Background
   Modes → Remote notifications**; confirm the correct **Team** is selected (must match the APNs
   key's team). iOS deployment target must be **16.0+**.

Android needs no APNs — `google-services.json` already carries the FCM config.

## 4. Send a test push (User Action)

Firebase Console → **Messaging (Cloud Messaging) → Send a message** → compose a notification → **Send
test message** → paste the device FCM token from the logs. Confirm it arrives on a real device (push
does not deliver to simulators/emulators reliably — use a physical device for iOS).

## Done
A test push is received on-device and the notification permission prompt appears on first entry. This
is the `enable-notifications` step of the `growth` phase. Advanced usage:
https://github.com/mirzemehdi/KMPNotifier#usage.
