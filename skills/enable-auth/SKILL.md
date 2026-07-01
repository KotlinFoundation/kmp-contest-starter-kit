---
name: enable-auth
description: Enable Google and Apple social sign-in on top of Firebase Auth — enable the providers, set GOOGLE_WEB_CLIENT_ID, wire iOS Info.plist client IDs, add the iOS Sign In with Apple capability, and confirm the AUTH_SOCIAL_LOGIN_ENABLED flag. Use when the developer wants social login / Google or Apple sign-in in the app.
---

# Enable social auth (Google + Apple)

Adds Google and Apple sign-in on top of the base Firebase project. Requires `setup-firebase` done
first (project + registered apps + anonymous auth). Auth is implemented via the `libs/auth/` module
(`auth-api` contracts + `auth-firebase` implementation, using KMPAuth); `UserRepository` exposes
`continueAsGuest()`, `logOut()`, `deleteAccount()`, and `currentUser`. `SignInScreen` already wires
the UI — you're supplying credentials, not writing auth code.

## 1. Confirm the feature flag — Agent Action

In `shared/src/commonMain/kotlin/com/kotlinfoundation/kmpstarterkit/util/Constants.kt`:

```kotlin
const val AUTH_SOCIAL_LOGIN_ENABLED = true
```

`true` (default) shows Apple + Google buttons; `false` = anonymous/guest only. Leave `true` for this
skill.

## 2. Enable providers in Firebase — User Action

Firebase Console → **Authentication → Sign-in method**:
- Enable **Google**.
- Enable **Apple**.

After enabling, re-download `google-services.json` (Android) and `GoogleService-Info.plist` (iOS)
and replace the files placed in `setup-firebase` — enabling a provider can change the config.

## 3. Google sign-in — Web client ID — User Action

Google sign-in on **both** Android and iOS uses the Firebase **Web client ID**.

- Find it in Firebase → Auth → Google provider (or Google Cloud → APIs & Services → Credentials →
  "Web client (auto created by Google Service)").
- Put it in `MobileApp/local.properties`:

  ```properties
  GOOGLE_WEB_CLIENT_ID=1234567890-abcdef.apps.googleusercontent.com
  ```

### iOS Info.plist — User Action

In `MobileApp/iosApp/iosApp/Info.plist`, fill these using values from `GoogleService-Info.plist`
(`CLIENT_ID`, `REVERSED_CLIENT_ID`) and the same Web client ID:

```xml
<key>GIDServerClientID</key>
<string>YOUR_WEB_CLIENT_ID</string>

<key>GIDClientID</key>
<string>YOUR_IOS_CLIENT_ID</string>

<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>YOUR_DOT_REVERSED_IOS_CLIENT_ID</string>
    </array>
  </dict>
</array>
```

## 4. Apple sign-in — User Action

**iOS:** In Xcode → target → **Signing & Capabilities → + → Sign In with Apple**.

**Android (Apple via web flow):** needs an Apple Developer setup —
1. Create a **Sign In with Apple** auth key (`.p8`); note the **Key ID** and **Team ID**.
2. Create a **Service ID** (e.g. `com.yourcompany.yourapp.auth`), enable Sign In with Apple,
   configure with the Primary App ID, and set:
   - Return URL: `https://<your-app-id>.firebaseapp.com/__/auth/handler`
   - Domain: `<your-app-id>.firebaseapp.com`
3. In Firebase → Auth → Apple provider, paste the `.p8` content, Key ID, Team ID, and Service ID.

## 5. Validate — Validation

Build and run (Android is fastest — `./gradlew :androidApp:assembleDebug` from `MobileApp/`, then
launch). On `SignInScreen`, Google and Apple buttons should appear and complete a real sign-in;
**Continue as guest** should still work (calls `continueAsGuest()`).

## Next

Deploy the AI web proxy and make an authenticated remote call → `integrate-web-proxy`.
