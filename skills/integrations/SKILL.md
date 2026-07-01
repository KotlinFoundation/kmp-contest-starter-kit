---
name: integrations
description: Phase-2 guide that connects the locally-running KMP app to services — Firebase + anonymous auth, Google/Apple social auth, and the web-proxy Cloud Functions (OpenAI/Replicate) wired via CLOUD_FUNCTIONS_URL. Use when the developer asks to do integrations / connect services for the KMP contest starter kit, hook the app up to Firebase, enable sign-in, or wire real remote API calls.
---

# Phase 2 — Integrations (connect to services)

**Goal:** take the app that already builds and runs locally (from the `getting-started` phase) and
connect it to real services — a Firebase project with anonymous auth, Google/Apple social sign-in,
and the web-proxy Cloud Functions backend (OpenAI / Replicate) so live remote calls return data.

This is the **blueprint orchestrator** for the phase. Work the checklist top to bottom. Each step is
tagged **Agent Action** (you do it), **User Action** (the developer must do it in a browser/console),
or **Validation** (prove it works). Steps compose the four atomic skills — read the referenced skill
before doing that step.

Track progress in a copy of [`progress-template.md`](progress-template.md).

## STOP rule (read verbatim)

> When the next unchecked item is a **User Action**, stop and wait for the developer to confirm
> they've done it before continuing. Never fabricate credentials, project IDs, or console state.

Most of this phase is heavy **User Action** work — creating a Firebase project in the browser,
downloading `google-services.json` / `GoogleService-Info.plist`, enabling providers, setting Secret
Manager keys, pasting API keys into `local.properties`. You cannot do these; guide, then wait.

## Checklist

### 1. Lay out the key catalog — `configure-environment`
- **Agent Action** — Read the `configure-environment` skill. Walk the developer through the full
  catalog of `MobileApp/local.properties` keys, the `MobileApp/gradle.properties`
  `SUBSCRIPTION_PROVIDER` toggle, and the `Constants.kt` fields. Establish which values this phase
  needs (`GOOGLE_WEB_CLIENT_ID`, `CLOUD_FUNCTIONS_URL`) vs. later phases.
- **User Action** — Ensure `MobileApp/local.properties` exists with at least `sdk.dir`. It is
  gitignored, so it never came from the template. **Stop and confirm.**

### 2. Create the Firebase project + apps + anonymous auth — `setup-firebase`
- **Agent Action** — Read the `setup-firebase` skill. Read the app's `applicationId` /
  iOS bundle id so the developer registers the right identifiers. Run
  `./gradlew :androidApp:signingReport` from `MobileApp/` to get the debug SHA-1 for them to paste.
- **User Action** — In https://console.firebase.google.com/ create the project, register the Android
  app (package = `applicationId`, add the SHA-1) and the iOS app (bundle id), download and place
  `google-services.json` → `MobileApp/androidApp/` and `GoogleService-Info.plist` →
  `MobileApp/iosApp/iosApp/`, enable **Anonymous** sign-in, and upgrade to the **Blaze** plan (needed
  for Cloud Functions in step 4). **Stop and confirm each.**
- **Validation** — `./gradlew :androidApp:assembleDebug` succeeds with the real
  `google-services.json` in place.

### 3. Enable social sign-in (Google / Apple) — `enable-auth`
- **Agent Action** — Read the `enable-auth` skill. Confirm `Constants.AUTH_SOCIAL_LOGIN_ENABLED = true`
  (default). Point the developer at the **Web client ID** they'll copy from Firebase.
- **User Action** — In Firebase Auth → Sign-in method, enable **Google** and **Apple**; copy the
  **Web client ID** into `GOOGLE_WEB_CLIENT_ID` in `MobileApp/local.properties`; wire iOS
  `Info.plist` client IDs and (for Apple on iOS) add the **Sign In with Apple** capability in Xcode.
  **Stop and confirm.**

### 4. Deploy the web proxy + point the client at it — `integrate-web-proxy`
- **Agent Action** — Read the `integrate-web-proxy` skill. Explain the `Web/functions` backend, the
  `{statusCode, errorMessage, data}` response shape, and the `requireAuth` Firebase-token gate.
- **User Action** — Set Secret Manager secrets `OPENAI_API_KEY` / `REPLICATE_API_KEY`
  (https://console.cloud.google.com/security/secret-manager), then from `Web/` run
  `firebase deploy --only functions`. Copy the printed base URL
  (`https://REGION-PROJECT_ID.cloudfunctions.net`) into `Constants.CLOUD_FUNCTIONS_URL`. **Stop and confirm.**
- **Agent Action** — Wire a client that calls the deployed function following the `add-api-service`
  pattern (Ktor service + DTOs, Firebase ID token in the `Authorization: Bearer` header, repository
  wraps in `Result`). Optionally test the backend locally first with
  `firebase emulators:start --only functions` from `Web/`.

### 5. Validation gate
- **Validation** — The app authenticates (anonymous **or** social) and a **live remote call**
  (e.g. one of the Cloud Functions) returns data on a real device/emulator. Run the
  `run-quality-gates` skill.

## Done → next phase

When authentication works and a live Cloud Function returns data, the app is connected. Proceed to
the **`publishing`** phase to prepare store listings, signing, and release builds.
