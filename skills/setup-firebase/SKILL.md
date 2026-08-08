---
name: setup-firebase
description: Create a Firebase project, register the Android + iOS apps, place google-services.json / GoogleService-Info.plist, and enable anonymous auth. Use when connecting the app to Firebase for the first time, or when the developer asks to set up Firebase / add the config files. ALSO use before any work that puts Firebase data or a Firebase client SDK into shared code — Firestore / Cloud Firestore, a Firebase database, realtime database, cloud storage, cross-device sync, syncing credits/settings/user data to Firebase, server-side balances — because there is no Firebase client SDK for the wasmJs target and the developer must be asked which trade-off they want before an approach is chosen.
---

# Set up Firebase

Firebase is the backend for auth, push notifications, and the AI Cloud Functions proxy. This skill
covers the base project + app registration + anonymous auth. Social sign-in is a separate step —
see `enable-auth`. The Cloud Functions deploy is `integrate-web-proxy`.

Most of this happens in the browser at https://console.firebase.google.com/ — you (the agent) can
prep identifiers and the SHA-1, but the developer performs the console clicks and file downloads.

> [!IMPORTANT]
> **Firestore + Kotlin/Wasm — ask before you pick an approach.**
>
> There is no Firebase client SDK for the `wasmJs` target. The GitLive
> [firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk) covers Android, iOS, JVM
> and JS, but **not** Kotlin/Wasm, so putting it in `commonMain` breaks the web build. Nothing about
> the base setup below is affected — this only matters once the developer wants **Firestore** (or
> another Firebase *client* SDK) in shared code.
>
> When that comes up, **put the question to the developer and wait for their answer** before writing
> any code or naming a recommendation. Present both options below verbatim, then stop. Do not pick
> one and proceed, do not answer with a recommendation as if it were settled, and do not silently
> drop a target:
>
> - **Keep Wasm (recommended, default).** Don't use a Firebase client SDK in shared code at all.
>   Put Firestore behind the Cloud Functions backend this kit already ships — see
>   *Firestore via Cloud Functions* below. Every platform, Wasm included, keeps working.
> - **Drop Wasm.** If the app doesn't need the web target, remove it and use the GitLive SDK
>   directly for a shorter path to Firestore. GitLive covers Android, iOS, JVM **and** JS — only
>   `wasmJs` is missing, so desktop is never the blocker; the browser build is. This is a real
>   trade-off, so it must be the developer's explicit decision, not an assumption.
>
> "Recommended, default" describes which option to *present first*, not permission to skip the ask.
> The web target is a deliverable the developer chose; trading it away is theirs to decide.
>
> If GitLive is used, take the **latest stable version** from its
> [releases](https://github.com/GitLiveApp/firebase-kotlin-sdk/releases) — don't pin an old one.

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

## 6. Set the default resource location — User Action

Firebase Console → **Storage → Get Started** → accept rules → pick a region.

> **This is not an app feature** — the app doesn't use Cloud Storage. It's a one-time
> Cloud Functions *deploy* prerequisite: `firebase deploy --only functions` uploads its source
> bundle to a GCS staging bucket, and a brand-new project that has never opened Storage/Firestore
> has no default resource location set, so that bucket can't be created — the first deploy
> (`integrate-web-proxy`) then fails with `Failed to make request to generateUploadUrl`. Clicking
> **Storage → Get Started** is just the simplest way to set the default location and avoid that.
> The region is **permanent** — pick the one closest to your users. Skip this if you're not
> deploying the Cloud Functions backend.

## 7. Validate — Validation

With the real `google-services.json` in place, from `MobileApp/`:

```bash
./gradlew :androidApp:assembleDebug
```

Run the app; the first launch should silently obtain an anonymous session.

> **Desktop / Web auth.** Android and iOS auth works from the config files above. Desktop and Web have no
> native Firebase SDK, so KMPAuth runs on a REST engine that needs explicit config: register a **Web app**
> in Firebase (Project settings → Add app → Web) and put its `apiKey` / `projectId` / `appId` into
> `MobileApp/local.properties` as `FIREBASE_API_KEY` / `FIREBASE_PROJECT_ID` / `FIREBASE_APPLICATION_ID`.
> `AppInitializer` wires them automatically when set; blank = no desktop/web auth (fine until you need it).
> Leave them empty on mobile-only projects.

## Firestore via Cloud Functions — the Wasm-safe architecture

Reach for this when the app needs Firestore and you're keeping the web target (the default answer to
the callout above). For the full build — what to sync and why, backend layout, idempotency, the
offline/local layer, and a worked credit-balance example — use the **`sync-data-firebase`** skill;
what follows is the architecture summary it builds on. It's the same shape as the AI proxy already in `Web/functions`, so the pieces
exist: HTTPS Cloud Functions authenticate the caller with a Firebase ID token
(`Web/functions/utils/validation.js` → `admin.auth().verifyIdToken(...)`) and reach Firestore through
the **Firebase Admin SDK**.

```
KMP app (Android, iOS, Desktop, Wasm)
   │  HTTPS + Firebase ID token
   ▼
HTTPS Cloud Functions  ──►  Firebase Admin SDK  ──►  Cloud Firestore
```

Rules to follow when building it:

- Every Firestore read/write goes through a Cloud Function; the client never touches Firestore.
- Cloud Functions use the Firebase Admin SDK — not the Firestore REST API from the client.
- Functions verify the caller's Firebase Auth ID token before any database operation.
- Keep the endpoints plain REST so one client implementation serves every platform.
- On the client, add them like any other backend API — DTOs + an `*ApiService` + a repository, per
  the `add-api-service` skill. The app stays free of Firebase implementation details.

Add new endpoints next to the existing ones in `Web/functions/api/`, export them from
`Web/functions/index.js`, and deploy with `integrate-web-proxy`.

## Next

- Enable Google/Apple sign-in → `enable-auth`.
- Deploy the AI backend and wire live calls → `integrate-web-proxy`.
- Whole phase orchestration → the `integrations` guide.
