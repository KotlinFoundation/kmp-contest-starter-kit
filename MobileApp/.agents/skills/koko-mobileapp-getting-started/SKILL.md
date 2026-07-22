---
name: koko-mobileapp-getting-started
description: Phase 1 developer journey guide for the MobileApp directory. Use this skill when the developer asks to proceed with getting started or to run koko-mobileapp-getting-started. It sets up Phase 1 progress tracking in the root folder and guides you through building the local app loops.
---

# Koko MobileApp Getting Started (Phase 1)

This skill coordinates the local-only development loop (Phase 1) within the `MobileApp/` project window.

## Elegant Two-Window Flow Overview

This repository uses a two-window approach:
1. **Root Window:** The developer opens the root repository directory. This is where high-level orchestration, the backend `Web/` proxy, and Phase 2 (`integrations`) / Phase 3 (`publishing`) tasks are run.
2. **MobileApp Window:** The developer opens the `MobileApp/` directory. This is where local-only app development (Phase 1) takes place.

---

## Agent Instructions

When the user asks to "proceed with koko-mobileapp-getting-started" or "get started" inside the `MobileApp/` workspace, perform the following steps:

0. **Find the git repo root once.** Run `git rev-parse --show-toplevel` — it's the folder that contains
   `MobileApp/`. From this MobileApp window that's **one level up** (`../`). Every `PROGRESS_*.md` and the
   root `skills/` live there, **not** inside `MobileApp/` (or `MobileApp/.agents/`). All paths below are
   written relative to `MobileApp/` (the window's working directory).

1. **Activate the central getting-started checklist.**
   Read and follow the blueprint at `../skills/getting-started/SKILL.md` (repo-root `skills/getting-started/`).

2. **Initialize the Phase 1 progress tracker — at the repo root, NEVER inside `MobileApp/`.**
   The tracker is `../PROGRESS_P1_GETTING_STARTED.md` (repo root, one level up from `MobileApp/`).
   - If it **already exists**, do NOT recreate it — read it and resume from the first unchecked item.
   - If it doesn't, copy `../skills/getting-started/progress-template.md` → `../PROGRESS_P1_GETTING_STARTED.md`.
   - Writing it inside `MobileApp/` or `MobileApp/.agents/` is the classic mistake — a later session (or the
     Root Window) looks at the repo root and thinks nothing was done.

3. **Is the product defined yet?** Before building features, `AiGuidelines/project/prd.md` / `user_flow.md`
   / `ui_ux.md` must be filled and the app name + id chosen. If the developer jumped straight here and those
   are missing, the blueprint's step C routes to the **`new-app`** skill first (it interviews the developer
   and writes them); then resume this guide. This is one-way and state-guarded — it can't loop.

4. **Drive Phase 1 Implementation:**
   - Review `../PROGRESS_P1_GETTING_STARTED.md` and build out the local loops (Room, Preferences, API service, permissions, etc.).
   - Follow the **STOP rule**: stop and wait for confirmation whenever a step is a **User Action**.
   - After finishing a chunk (e.g. onboarding screens), **continue to the next unchecked item** — don't stop and ask "what next".
   - Keep `../PROGRESS_P1_GETTING_STARTED.md` updated as you complete tasks (features also land in `../PROGRESS_FEATURES.md`, same repo root).
   - **Don't loop on Gradle.** Validate with the scoped tasks only — `spotlessCheck`,
     `:shared:jvmTest :shared:testAndroidHostTest`, `:androidApp:assembleDebug`. **Never** the aggregate
     `check` / `build` / `clean build` (they pull in iOS and fail on unrelated cache issues, which looks
     like a failure and tempts a re-run). A run task like `:desktopApp:run` never returns — that's
     running, not hung; don't kill and re-run. `assembleDebug` compiling green is the Android check — don't
     adb-install-and-launch to "confirm it works". See `run-quality-gates`.
   - **Environment Quirk:** If you need to run any `./gradlew` commands and you encounter an error about "different paths to the Android Preferences folder" (ANDROID_PREFS_ROOT vs ANDROID_USER_HOME), always prefix your commands with `unset ANDROID_PREFS_ROOT &&` (e.g., `unset ANDROID_PREFS_ROOT && ./gradlew assembleDebug`).
   - **Prototyping AI shortcut:** By default, the `AiGenerationProvider` looks for a deployed Cloud Function proxy. To test AI generation locally without deploying the backend in Phase 1, instruct the user to add `OPENAI_API_KEY=sk-your-key` or `REPLICATE_API_KEY=your-key` directly into their `MobileApp/local.properties` file. Ensure `CLOUD_FUNCTIONS_URL` in `AppConfiguration.kt` is left blank, which triggers the direct-device API call.

5. **Phase 1 Hand-Off:**
   Once all Phase 1 tasks are complete and verified, present a clear next-step message to the developer:
   > *"Phase 1 is officially complete! Your local loop is fully verified. To continue with Phase 2 (Integrations) or Phase 3 (Publishing), please switch back to your root directory Android Studio window and invoke the respective root-level skill there."*
