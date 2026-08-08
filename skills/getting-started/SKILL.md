---
name: getting-started
description: Phase-1 blueprint for the KMP starter kit — get the app running locally on your own device, rebrand it, and build your MVP features from the PRD (all LOCAL-only, no accounts). Use when getting started with the KMP contest starter kit / on first run / initial setup, before touching Firebase, backend, or publishing.
---

# Getting Started — Phase 1 (First Run, LOCAL-ONLY)

**Goal:** get the app **running on your own device/emulator**, rebranded to your app, with **your MVP
features built** — all **purely locally**. Prove the loop end-to-end before adding any cloud services.

**Start here?** If the developer arrived with an *idea* and no product docs yet ("build me a habit
tracker"), run the **`new-app`** skill first — it interviews them and writes `prd.md` / `user_flow.md` /
`ui_ux.md` plus the app name/id this guide needs. `new-app` hands straight back here.

**No accounts needed in this phase.** No Firebase, no Adapty/RevenueCat, no Play/App Store account —
the kit's mock subscription provider and direct-mode AI cover it.

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

> **Progress files still live at the repo root.** Even though Phase 1 is driven from the **MobileApp
> Window**, the `PROGRESS_*.md` files belong at the **git repo root** (the folder containing `MobileApp/`),
> so from this window write/read them **one level up** (`../PROGRESS_FEATURES.md`,
> `../PROGRESS_P1_GETTING_STARTED.md`). Never create them inside `MobileApp/` — a later session (or the
> Root Window) will look at the root and think nothing was done.

> **STOP rule:** When the next unchecked item is a **User Action**, stop and wait for the developer to confirm they've done it before continuing. Never fabricate device state or credentials.

## Role labels

- **Agent Action** — an AI agent (or dev) can do it directly: edit code, run a script/gradle.
- **User Action** — human-only: install tooling, set paths, run the app on a device, look at the screen.
- **Validation** — a concrete check/gate before moving on.

---

## Keys / secrets you'll need

Just one — this phase is local-only, no cloud, no accounts:
- **`sdk.dir`** (required) — your machine's Android SDK path. Copy
  `MobileApp/local.properties.example` → `local.properties` and set it.

Verify: `./scripts/check_env.sh --phase getting-started`.

## Checklist (ordered)

### A. Prerequisites & first run

1. **User Action** — Install **JDK 17+** and **Android Studio** (bundles the Android SDK), then add the **Kotlin Multiplatform plugin** (`Settings → Plugins → Marketplace →` "Kotlin Multiplatform") — it's what makes the iOS/Desktop/Web run targets show up, not just Android. macOS-only for iOS: install **Xcode**.
2. **Agent Action** — Ensure `sdk.dir=/path/to/Android/sdk` is set in `MobileApp/local.properties` (see the `run-the-app` skill).
3. **User Action** — Run the app once. Fastest sanity check is Desktop: `./gradlew :desktopApp:run` from `MobileApp/`. (Android: emulator + `:androidApp:installDebug`; Web: `:webApp:wasmJsBrowserDevelopmentRun`.) Use the `run-the-app` skill for exact commands per platform.
4. **Validation** — The app launches and shows the **Home** screen on at least one platform.

### B. Rebrand to your app

5. **Agent Action** — Rename the package / applicationId / iOS bundle ID + display name with the **`refactor-package`** skill, using the **app id + name decided in `new-app`** (if the developer hasn't picked one yet, ask — offer 3 suggestions with one recommended):
   ```bash
   ./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp
   ```
6. **Validation** — App still builds after the rename (`./gradlew :androidApp:assembleDebug` or `:desktopApp:run`).

### C. Define the product (grounds everything downstream)

7. **Agent Action** — The product must be defined before building: `AiGuidelines/project/prd.md`, `user_flow.md`, and `ui_ux.md` filled, **app name + id decided**, deferred decisions marked `TODO(<phase>)` in `root/AppConfiguration.kt`. This is normally produced by the **`new-app`** skill.
   - **If any of that is missing** — the docs are still just a heading, or no name/id has been chosen (common when the developer jumped straight into `getting-started`) — invoke **`new-app`** to produce it. It interviews the developer and writes the docs + picks the name/id; never invent the product yourself. When it finishes, **resume this guide at the next unchecked item** (don't restart from step A).
   - **This cannot loop.** `new-app` fills exactly the files/values checked here, so on return the precondition is satisfied and `new-app` is never re-invoked. The trigger is state-based (docs present + name/id set), not a blind re-run — if the state is already good, skip straight past this step.

### D. Build your features

8. **Agent Action** — Build the MVP with the **`build-features`** skill: it derives the screens + local models from `prd.md`/`user_flow.md`, scaffolds them (`make_local.sh` / `generate_screen.sh`, **run sequentially** — they patch shared files), implements the UI per `ui_ux.md`, and brands the onboarding + paywall screens that already ship.
   - **Speed matters here:** after the sequential scaffold, `build-features` step 3 **fans out one subagent per screen/model in parallel** (onboarding + paywall branding join the same fan-out). Implementing screens one after another is the main reason this phase feels slow — see "Agent Working Style" in the root `AGENTS.md`.
   - It records every model/screen in **`PROGRESS_FEATURES.md`** at the **git repo root** (the folder that contains `MobileApp/` — from the MobileApp Window that's one level up, `../PROGRESS_FEATURES.md`; never inside `MobileApp/`) and ticks them off as it goes — so if a session stops, the next one resumes from the first unchecked item instead of guessing.
   - Underlying skills it uses: **`new-local-model`**, **`new-screen`**, and — only if a feature needs them — **`add-api-service`** (any public URL, no backend), **`save-preferences`**, **`add-permission`**.
   - Everything stays local: **no Firebase, no provider account, no store account** in this phase. The paywall runs on the mock provider if you touch it.

### E. Validate

9. **Agent Action** — Verify the UI with the **`verify-ui`** skill (headless Compose test for behaviour ~2s + render a `@Preview` to a PNG and look at it), then run the **`run-quality-gates`** skill (`spotlessApply`/`spotlessCheck`, `:shared:jvmTest :shared:testAndroidHostTest`, `:androidApp:assembleDebug`).
10. **User Action** — Re-run the app and confirm your features behave as expected on a device.

---

## Validation gate (Phase 1 done)

> **App launches on at least one platform and shows the Home screen; quality gates pass.**

Progress is tracked in `PROGRESS_P1_GETTING_STARTED.md` at the **git repo root** (the folder containing
`MobileApp/`; from the MobileApp Window it's `../PROGRESS_P1_GETTING_STARTED.md` — never inside `MobileApp/`).

## If the agent stalls or loses its place

This guide is a single ordered checklist — after finishing any step (e.g. the onboarding screens), the
agent should continue to the **next unchecked item**, not stop and ask "what's next". If it does stall,
finishes prematurely, or can't find the progress files:

- **Developer:** just say **"continue @koko-mobileapp-getting-started"** (in the MobileApp Window). That
  reloads this guide; the agent then re-reads the progress files and resumes at the first unchecked item.
- **Agent, on resume:** first locate the progress files at the **git repo root** — `git rev-parse
  --show-toplevel`, then read `../PROGRESS_FEATURES.md` and `../PROGRESS_P1_GETTING_STARTED.md`. If they
  genuinely don't exist yet, create them there (copy `progress-template.md`) — do **not** create them
  inside `MobileApp/`, and do **not** re-derive a plan or redo checked work.

## Done → next phase

Once green, switch back to the **Root Window** and start Phase 2. **Trigger it explicitly** — tell
your agent **"start the integrations phase"** (or run the `integrations` skill). That connects the
app to real services: Firebase + anonymous auth, optional social sign-in, and the web-proxy backend.
Then **`publishing`**. Don't stop here assuming the next phase auto-starts — name the phase to begin it.

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
