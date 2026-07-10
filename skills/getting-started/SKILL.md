---
name: getting-started
description: Phase-1 blueprint for the KMP starter kit — get the app running locally on your own device, rebrand it, and drive it with a screen, a Room model, a preference, a network call, and a permission (all LOCAL-only). Use when getting started with the KMP contest starter kit / on first run / initial setup, before touching Firebase, backend, or publishing.
---

# Getting Started — Phase 1 (First Run, LOCAL-ONLY)

**Goal:** get the app **running on your own device/emulator**, rebranded to your app, and driven
**purely locally** — one screen, one Room model, one preference, one plain network request, one device
permission. Prove the loop end-to-end before adding any cloud services.

**Explicitly deferred to later phases** (do NOT do these now):
- Firebase (Analytics, Messaging, Crashlytics, RemoteConfig), authentication, backend / web-proxy → the **`integrations`** guide.
- App Store / Play Store, signing for release, subscriptions/monetization → the **`publishing`** guide.

## Two-Window Flow & STOP rule

> **Two-Window Developer Flow:**
> This repository uses an elegant Two-Window setup for seamless multi-environment management:
> 1. **Root Window:** Opened at the repository root. This window manages root-level configurations, the Node.js backend proxy (`Web/`), and Phase 2 (`integrations`) and Phase 3 (`publishing`) progress.
> 2. **MobileApp Window:** Opened at the `MobileApp/` directory. This window isolates the Android/iOS/Desktop/Web client environment and drives Phase 1 development.

> [!IMPORTANT]
> **CRITICAL AGENT INSTRUCTION (TWO-WINDOW TRANSITION)**:
> If this skill is executed from the **Root Window**, the very first step is to instruct the developer to open a new Android Studio window pointing to the `MobileApp/` directory:
> *"Our first step involves opening up a new Android Studio window for the MobileApp directory, so we can utilize the Two-Window Approach. Please open the `MobileApp/` directory (at the repository root) in a new window and type 'Run @koko-mobileapp-getting-started' there to load the client environment and begin building the local loop!"*

> **STOP rule:** When the next unchecked item is a **User Action**, stop and wait for the developer to confirm they've done it before continuing. Never fabricate device state or credentials.

## Role labels

- **Agent Action** — an AI agent (or dev) can do it directly: edit code, run a script/gradle.
- **User Action** — human-only: install tooling, set paths, run the app on a device, look at the screen.
- **Validation** — a concrete check/gate before moving on.

---

## Checklist (ordered)

### A. Prerequisites & first run

1. **User Action** — Install **JDK 17+** and **Android Studio** (bundles the Android SDK), then add the **Kotlin Multiplatform plugin** (`Settings → Plugins → Marketplace →` "Kotlin Multiplatform") — it's what makes the iOS/Desktop/Web run targets show up, not just Android. macOS-only for iOS: install **Xcode**.
2. **Agent Action** — Ensure `sdk.dir=/path/to/Android/sdk` is set in `MobileApp/local.properties` (see the `run-the-app` skill).
3. **User Action** — Run the app once. Fastest sanity check is Desktop: `./gradlew :desktopApp:run` from `MobileApp/`. (Android: emulator + `:androidApp:installDebug`; Web: `:webApp:wasmJsBrowserDevelopmentRun`.) Use the `run-the-app` skill for exact commands per platform.
4. **Validation** — The app launches and shows the **Home** screen on at least one platform.

### B. Rebrand to your app

5. **Agent Action** — Rename the package / applicationId / iOS bundle ID + display name with the **`refactor-package`** skill:
   ```bash
   ./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp
   ```
6. **Validation** — App still builds after the rename (`./gradlew :androidApp:assembleDebug` or `:desktopApp:run`).

### C. Define the product (grounds everything downstream)

7. **Agent Action** — Fill `AiGuidelines/project/prd.md` (what the app is, who it's for, scope, core value). Search for `TAILOR PER APP` markers.
8. **Agent Action** — Fill `AiGuidelines/project/user_flow.md` (primary flows + screen sequence).

### D. Build the local loop

9. **Agent Action** — Add a screen with the **`new-screen`** skill (`./scripts/generate_screen.sh YourScreenName` — scaffolds Screen + UiState + ViewModel and wires route + DI). Implement the UI.
10. **Agent Action** — Persist data locally with the **`new-local-model`** skill (`./scripts/make_local.sh ModelName` — Room entity + DAO + DB registration + Koin). Add real columns and use it from a repository/ViewModel.
11. **Agent Action** — Add a network request with the **`add-api-service`** skill (DTOs → API service → repository via `backgroundExecutor.execute { }` → ViewModel). Any public URL works — no project backend needed.
12. **Agent Action** — Store a setting with the **`save-preferences`** skill (add a key to `UserPreferences.Keys`, read/write via the injected `UserPreferences`).
13. **Agent Action** — Ask for a device permission with the **`add-permission`** skill (`rememberCameraPermissionState()` etc. or `rememberAppPermissionState(...)`; add iOS `NS*UsageDescription` if needed).

### E. Validate

14. **Agent Action** — Run the **`run-quality-gates`** skill (`spotlessApply`/`spotlessCheck`, `:shared:jvmTest :shared:testAndroidHostTest`, `:androidApp:assembleDebug`).
15. **User Action** — Re-run the app and confirm the new screen + data/preference/permission behave as expected on a device.

---

## Validation gate (Phase 1 done)

> **App launches on at least one platform and shows the Home screen; quality gates pass.**

Once green, switch back to the **Root Window** to proceed to the **`integrations`** guide (Firebase, auth, backend/web-proxy), then **`publishing`**.

Progress is tracked in `PROGRESS_P1_GETTING_STARTED.md` at the root folder.

---

## Troubleshooting

Environment/setup snags that can block the first build or the Phase-2 backend deploy:

- **`Several environment variables and/or system properties contain different paths to the Android
  Preferences folder`** (Gradle sync / AGP fails) — both `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME`
  are set to different paths (seen in some sandboxes/CI images). Keep one:

  ```bash
  unset ANDROID_PREFS_ROOT
  ```

- **`command not found: firebase`** (Phase 2) — the Firebase CLI isn't installed. Install it:
  `curl -sL https://firebase.tools | bash` (no Node needed) or `npm install -g firebase-tools`.
  See `integrate-web-proxy`.

- **`firebase deploy --only functions` fails with `403` / Secret Manager** — the Secret Manager API
  isn't enabled for the project. Enable it, then re-deploy. See `integrate-web-proxy` step 1.

- **`Failed to make request to generateUploadUrl`** on a brand-new project — no default Cloud
  Storage bucket yet. Firebase Console → **Storage → Get Started** (pick a region), then re-deploy.
  See `setup-firebase`.
