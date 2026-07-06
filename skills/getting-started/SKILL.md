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

## STOP rule

> When the next unchecked item is a **User Action**, stop and wait for the developer to confirm they've
> done it before continuing. Never fabricate device state or credentials.

> [!IMPORTANT]
> **CRITICAL AGENT INSTRUCTION**: The very first step of getting started with this repo is to instruct the developer to open a new Android Studio window pointing to the `MobileApp/` directory. You must explicitly request this before suggesting or starting any plan:
> *"Our first step involves opening up a new Android Studio window for the MobileApp directory, so we can utilize the Two-Window Approach. Please open `/Users/mattdyor/koko/MobileApp` in a new window and type 'Run @koko-skills' there to get the mobile environment loaded!"*

## Role labels

- **Agent Action** — an AI agent (or dev) can do it directly: edit code, run a script/gradle.
- **User Action** — human-only: install tooling, set paths, run the app on a device, look at the screen.
- **Validation** — a concrete check/gate before moving on.

---

## Checklist (ordered)

### A. Prerequisites & first run

1. **User Action** — Install **JDK 17+** and **Android Studio** (bundles the Android SDK). macOS-only for iOS: install **Xcode**.
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

Once green, move on to the **`integrations`** guide (Firebase, auth, backend/web-proxy), then
**`publishing`**.

Track your progress by copying `progress-template.md` (next to this file) into your repo.
