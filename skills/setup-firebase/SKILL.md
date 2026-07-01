---
name: setup-firebase
description: Create a Firebase project, register the Android + iOS apps, place google-services.json / GoogleService-Info.plist, and enable anonymous auth. Use when connecting the app to Firebase for the first time, or when the developer asks to set up Firebase / add the config files.
---

# Set up Firebase

Firebase is the backend for auth, push notifications, and the AI Cloud Functions proxy. This skill
covers the base project + app registration + anonymous auth. Social sign-in is a separate step —
see `enable-auth`. The Cloud Functions deploy is `integrate-web-proxy`.

Most of this happens in the browser at https://console.firebase.google.com/ — you (the agent) can
prep identifiers and the SHA-1, but the developer performs the console clicks and file downloads.

## 1. Prep the identifiers — Agent Action

- **Android package** = the app's `applicationId` (in `MobileApp/androidApp/build.gradle.kts`).
- **iOS bundle id** = `PRODUCT_BUNDLE_IDENTIFIER` in `MobileApp/iosApp/iosApp.xcodeproj/project.pbxproj`.
- **Debug SHA-1** (required for Google sign-in on Android) — from `MobileApp/`:

  ```bash
  ./gradlew :androidApp:signingReport
  ```

  Copy the `SHA1` line under the `debug` variant for the developer to paste into Firebase.

## 2. Create the project — User Action

1. Open https://console.firebase.google.com/ → **Add project**, give it a name + unique project ID.
2. **Upgrade to the Blaze (pay-as-you-go) plan** — Project Overview → Usage and billing → Details &
   settings → Modify plan → **Blaze**. Required for Cloud Functions (`integrate-web-proxy`);
   generous free tier, set a budget alert.

## 3. Register the Android app + place config — User Action

1. In the project, **Add app → Android**.
2. **Package name** = the `applicationId` from step 1.
3. Paste the **debug SHA-1** from step 1 (needed for Google sign-in).
4. Download `google-services.json` and place it at:

   ```
   MobileApp/androidApp/google-services.json
   ```

## 4. Register the iOS app + place config — User Action

1. **Add app → iOS**.
2. **Bundle ID** = the iOS bundle id from step 1.
3. Download `GoogleService-Info.plist` and place it at:

   ```
   MobileApp/iosApp/iosApp/GoogleService-Info.plist
   ```

## 5. Enable Anonymous auth — User Action

Firebase Console → **Authentication → Sign-in method → Anonymous → Enable**. The app auto-signs in
anonymously on first launch (`signInAnonymouslyIfNecessary()`), and the Cloud Functions gate on a
Firebase ID token, so this is required even before social sign-in.

## 6. Validate — Validation

With the real `google-services.json` in place, from `MobileApp/`:

```bash
./gradlew :androidApp:assembleDebug
```

Run the app; the first launch should silently obtain an anonymous session.

## Next

- Enable Google/Apple sign-in → `enable-auth`.
- Deploy the AI backend and wire live calls → `integrate-web-proxy`.
- Whole phase orchestration → the `integrations` guide.
