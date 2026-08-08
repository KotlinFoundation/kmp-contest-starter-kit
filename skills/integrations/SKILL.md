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

## Keys / secrets you'll need

- **Firebase config files** (required) — your own `google-services.json` + `GoogleService-Info.plist`
  (the committed ones are boilerplate: `project_id = PROJECT_ID`).
- **`GOOGLE_WEB_CLIENT_ID`** (opt-in) — only if you add Google/Apple sign-in.
- **Subscription SDK keys** (opt-in) — only if you set up monetization now.
- **`CLOUD_FUNCTIONS_URL` + Secret Manager keys** (opt-in) — only if you use the AI web-proxy.

The recommended anonymous-only path needs just the Firebase config. Verify:
`./scripts/check_env.sh --phase integrations`.

## Checklist

### 1. Lay out the key catalog — `configure-environment`
- **Agent Action** — Read the `configure-environment` skill. Walk the developer through the full
  catalog of `MobileApp/local.properties` keys, the `MobileApp/gradle.properties`
  `SUBSCRIPTION_PROVIDER` toggle, and the `AppConfiguration.kt` fields. Establish which values this phase
  needs (`GOOGLE_WEB_CLIENT_ID`, `CLOUD_FUNCTIONS_URL`) vs. later phases.
- **User Action** — Ensure `MobileApp/local.properties` exists with at least `sdk.dir` (copy it from
  the committed `MobileApp/local.properties.example` if needed). It is gitignored, so it never came
  from the template. Run `./scripts/check_env.sh --phase integrations` to see which keys this phase
  will need. **Stop and confirm.**

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

### 3. Authentication — anonymous first, social opt-in — `enable-auth`
Easiest-first: the recommended path is **Firebase + Anonymous auth only** — the app already has
working authentication (`continueAsGuest()`), no extra keys, no social config. `setup-firebase`
(step 2) already enables Anonymous, so **by default this step is done** — `AUTH_SOCIAL_LOGIN_ENABLED`
defaults to `false`, so the Google/Apple buttons are hidden and `GOOGLE_WEB_CLIENT_ID` is not needed.

- **Agent Action** — **Ask the developer: "do you also want Google / Apple sign-in?"**
  - **No** (recommended for a first app) → nothing to do; anonymous auth works. Skip to step 4.
  - **Yes** → read the `enable-auth` skill and proceed with the User Action below.
- **User Action** (only if they said yes) — Set `AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED = true`, then in
  Firebase Auth → Sign-in method enable **Google** and **Apple** (re-download **both**
  `google-services.json` and `GoogleService-Info.plist`); copy the **Web client ID** into
  `GOOGLE_WEB_CLIENT_ID` in `MobileApp/local.properties`; fill the iOS `Info.plist` client IDs
  (`GIDServerClientID` / `GIDClientID` / `CFBundleURLSchemes`); add the **Sign In with Apple**
  capability in Xcode; and for Apple-on-Android complete the Apple Developer `.p8` + Service ID web
  flow. The `enable-auth` skill lists each of these. **Stop and confirm each.**

### 4. Subscriptions — OPTIONAL commercial step (opt-in) — `setup-subscriptions`
Not required for a working app — skip it unless the developer is setting up monetization now. Full
product / entitlement / paywall dashboard wiring lives in Phase 4 (`monetization`); here we only grab
the SDK **credentials** early if they want them.

- **Agent Action** — **Ask: "are you setting up subscriptions/monetization now?"** If no, skip to
  step 5 — the app still works fully.
- **User Action** (only if yes) — Log into Adapty (default) or RevenueCat, copy both **public SDK
  keys**, and paste them into `MobileApp/local.properties` (`SUBSCRIPTION_PROVIDER_ANDROID_API_KEY`,
  `SUBSCRIPTION_PROVIDER_IOS_API_KEY`). **Stop and confirm.**

### 5. Deploy the web proxy + point the client at it — `integrate-web-proxy`
- **Agent Action** — Read the `integrate-web-proxy` skill. Explain the `Web/functions` backend, the
  `{statusCode, errorMessage, data}` response shape, and the `requireAuth` Firebase-token gate.
- **Prototyping shortcut** — to try AI *without* deploying the proxy, set `OPENAI_API_KEY`/`REPLICATE_API_KEY`
  in `local.properties` and leave `CLOUD_FUNCTIONS_URL` blank → the app calls the provider directly
  (key on device, prototyping only). See `integrate-web-proxy` for the caveats.
- **User Action** — Set Secret Manager secrets `OPENAI_API_KEY` / `REPLICATE_API_KEY`
  (https://console.cloud.google.com/security/secret-manager), then from `Web/` run
  `firebase deploy --only functions`. Copy the printed base URL
  (`https://REGION-PROJECT_ID.cloudfunctions.net`) into `AppConfiguration.CLOUD_FUNCTIONS_URL`. **Stop and confirm.**
- **Agent Action** — Wire a client that calls the deployed function following the `add-api-service`
  pattern (Ktor service + DTOs, Firebase ID token in the `Authorization: Bearer` header, repository
  wraps in `Result`). Optionally test the backend locally first with
  `firebase emulators:start --only functions` from `Web/`.

### 5b. Cloud data — optional, only if the app needs it — `sync-data-firebase`
- **Agent Action** — Skip unless the developer wants data in Firestore, cross-device sync, or
  server-authoritative state (a credit balance, an entitlement). Nothing in this phase requires it.
- **User Action** — If they do want it, the `sync-data-firebase` skill opens with a question the
  developer must answer: **no Firebase client SDK supports the `wasmJs` target**, so the choice is
  keeping the web build (Firestore behind Cloud Functions) or dropping it (GitLive SDK in shared code).
  **Stop and ask. Never decide this for them.**

### 6. Validation gate
- **Validation** — Run `./scripts/check_env.sh --phase integrations` from `MobileApp/`. For the
  recommended anonymous-only path it should report clean once your real Firebase config is in place
  (`GOOGLE_WEB_CLIENT_ID` and the subscription keys stay `⚪` unless you opted in above). Any `⚠️` is
  a key to go get.
- **Validation** — The app authenticates (anonymous **or** social) and a **live remote call**
  (e.g. one of the Cloud Functions) returns data on a real device/emulator. Run the
  `run-quality-gates` skill.

## Done → next phase

When authentication works and a live Cloud Function returns data, the app is connected. **Trigger the
next phase explicitly** — tell your agent **"start the publishing phase"** (or run the `publishing`
skill) to prepare icons, release signing, store listings, and the first review build.
