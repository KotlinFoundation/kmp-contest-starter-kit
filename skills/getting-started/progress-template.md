# Phase 1 Progress — First Run (LOCAL-ONLY)

> [!NOTE]
> **Setup Instruction:** Copy this template file to the root of your repository and rename it to **`PROGRESS_P1_GETTING_STARTED.md`**.
> Use this file to tick items as you go. Full instructions are in the `getting-started` skill.
>
> **Role Labels:**
> - **User:** Human-only actions.
> - **Agent:** AI/developer can execute (edit code, run gradle, etc.).
> - **Validation:** A verification gate.
>
> *STOP RULE:* Stop at any unchecked **User** item and wait for confirmation before proceeding.

## A. Prerequisites & first run
- [ ] **User** — Install JDK 17+ and Android Studio (Android SDK). macOS/iOS: install Xcode.
- [ ] **Agent** — `sdk.dir=` set in `MobileApp/local.properties` (`run-the-app` skill).
- [ ] **User** — Run the app (`./gradlew :desktopApp:run`, or Android/Web) from `MobileApp/`.
- [ ] **Validation** — App launches and shows the Home screen on at least one platform.

## B. Rebrand
- [ ] **Agent** — Rename package/appId/bundle/name (`refactor-package` skill).
- [ ] **Validation** — App still builds after the rename.

## C. Define the product
- [ ] **Agent** — Fill `AiGuidelines/project/prd.md`.
- [ ] **Agent** — Fill `AiGuidelines/project/user_flow.md`.

## D. Local loop
- [ ] **Agent** — Add a screen (`new-screen` skill) and implement its UI.
- [ ] **Agent** — Persist a model locally (`new-local-model` skill).
- [ ] **Agent** — Add a network request (`add-api-service` skill).
- [ ] **Agent** — Store a setting (`save-preferences` skill).
- [ ] **Agent** — Request a device permission (`add-permission` skill).

## E. Validate
- [ ] **Agent** — Run quality gates (`run-quality-gates` skill).
- [ ] **User** — Re-run the app; confirm new screen/data/preference/permission work on a device.

## Gate
- [ ] App launches on at least one platform and shows the Home screen; quality gates pass.

Next: the `integrations` guide (Firebase, auth, backend), then `publishing`.
