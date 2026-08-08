---
name: setup-analytics
description: Enable Firebase Analytics, Crashlytics, and Remote Config feature flags in the KMP app — log events, add a flag, toggle it at runtime via Remote Config, and verify events in DebugView. Use when the developer wants analytics, crash reporting, or remote feature flags. Firebase must already be set up (integrations phase).
---

# Set up analytics, Crashlytics & feature flags

All three ride on the **same Firebase project** wired in the `integrations` phase (Analytics,
Crashlytics, and Remote Config are Firebase features — no new project). Enable each in the console,
then use the app's existing abstractions.

## 1. Enable in the Firebase Console

Open https://console.firebase.google.com/ → your project:
- **Analytics** — the Analytics dashboard (enabled with the project); confirm the Android/iOS apps
  report.
- **Crashlytics** — **Run & monitor → Crashlytics → Enable Crashlytics** (a first crash/report must
  arrive before the dashboard populates).
- **Remote Config** — **Run & monitor → Remote Config**; you'll add parameters in step 3.

## 2. Log analytics events

Events go through the `Analytics` interface — Firebase Analytics on mobile, no-op elsewhere:
`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/analytics/Analytics.kt`.

- `analytics.logEvent(event, params)` — custom event.
- `analytics.logScreenView(screenName)` — the common screen-view event.

Define new event/param names as constants in the `Analytics` companion (e.g. `EVENT_SCREEN_VIEW`,
`EVENT_CLICKED_GENERATE`) rather than raw strings. Inject `Analytics` via Koin and log at your funnel
points (screen entry, key conversions). Analytics is gated by the `IS_ANALYTICS_ENABLED` flag in
`AppInitializer.initializeAnalytics()`.

## 3. Feature flags via Remote Config

Flags are read through `FeatureFlagManager`
(`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/data/source/featureflag/FeatureFlagManager.kt`),
backed by Firebase Remote Config on Android/iOS and a no-op elsewhere. `AppInitializer` calls
`syncsFlagsAsync()` on startup; values fall back to `DEFAULT_VALUES` until fresh ones fetch.

**Add a flag** in `FeatureFlagManager`:
```kotlin
object Keys {
    const val IS_ADS_ENABLED = "is_ads_enabled"
    const val IS_ANALYTICS_ENABLED = "is_analytics_enabled"
    const val SHOW_REMOTE_PAYWALL = "show_remote_paywall"
    const val MY_NEW_FLAG = "my_new_flag" // <- add
}
val DEFAULT_VALUES: Map<String, Comparable<Nothing>> = mapOf(
    Keys.IS_ADS_ENABLED to false,
    Keys.IS_ANALYTICS_ENABLED to true,
    Keys.SHOW_REMOTE_PAYWALL to false,
    Keys.MY_NEW_FLAG to false, // <- default until Remote Config fetches
)
```

**Read it:** `featureFlagManager.getBoolean(FeatureFlagManager.Keys.MY_NEW_FLAG)` (also
`getString` / `getLong` / `getDouble`).

**Toggle it at runtime:** in the Firebase Console → **Remote Config**, **Add parameter** with the
same key string (`my_new_flag`), set the value, and **Publish changes**. The next `syncsFlagsAsync()`
(app start) picks it up — no app release needed. Publishing lets you enable/disable a feature or roll
it out to a subset of users without shipping.

## 4. Verify in DebugView

Firebase **DebugView** shows events in near-real-time from a debug-enabled device:
- Android: `adb shell setprop debug.firebase.analytics.app <applicationId>`
- Then open **Firebase Console → Analytics → DebugView** and drive the app.

Use `AppLogger`
(the facade declared in `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/logging/Logger.kt`
alongside the `Logger` interface it fans out to, initialized in `AppInitializer`) for local
`AppLogger.d(...)` traces alongside DebugView while confirming events fire.

## Done
An event appears in DebugView and a Remote Config parameter change flips your flag on next launch.
This is the `setup-analytics` step of the `growth` phase.
