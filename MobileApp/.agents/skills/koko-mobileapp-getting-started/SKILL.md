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

1. **Activate the Central Getting-Started Checklist:**
   Read and load the root-level getting-started blueprint located at:
   `../../../../skills/getting-started/SKILL.md`

2. **Initialize Phase 1 Progress Tracker:**
   Copy the progress template from the root-level `../../../../skills/getting-started/progress-template.md` to:
   `../../PROGRESS_P1_GETTING_STARTED.md` (which maps to the root-level `/PROGRESS_P1_GETTING_STARTED.md`).
   This ensures that progress is saved in the root folder alongside any other phase trackers.

3. **Drive Phase 1 Implementation:**
   - Review `PROGRESS_P1_GETTING_STARTED.md` and build out the local loops (Room, Preferences, API service, permissions, etc.).
   - Follow the **STOP rule**: stop and wait for confirmation whenever a step is a **User Action**.
   - Keep `PROGRESS_P1_GETTING_STARTED.md` updated as you complete tasks.
   - **Environment Quirk:** If you need to run any `./gradlew` commands and you encounter an error about "different paths to the Android Preferences folder" (ANDROID_PREFS_ROOT vs ANDROID_USER_HOME), always prefix your commands with `unset ANDROID_PREFS_ROOT &&` (e.g., `unset ANDROID_PREFS_ROOT && ./gradlew assembleDebug`).
   - **Prototyping AI shortcut:** By default, the `AiGenerationProvider` looks for a deployed Cloud Function proxy. To test AI generation locally without deploying the backend in Phase 1, instruct the user to add `OPENAI_API_KEY=sk-your-key` or `REPLICATE_API_KEY=your-key` directly into their `MobileApp/local.properties` file. Ensure `CLOUD_FUNCTIONS_URL` in `AppConfiguration.kt` is left blank, which triggers the direct-device API call.

4. **Phase 1 Hand-Off:**
   Once all Phase 1 tasks are complete and verified, present a clear next-step message to the developer:
   > *"Phase 1 is officially complete! Your local loop is fully verified. To continue with Phase 2 (Integrations) or Phase 3 (Publishing), please switch back to your root directory Android Studio window and invoke the respective root-level skill there."*
