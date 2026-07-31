# Phase 5 Progress — Growth

> [!NOTE]
> **Setup Instruction:** Copy this template file to the root of your repository and rename it to **`PROGRESS_P5_GROWTH.md`**.
> Use this file to tick items as you go. Full instructions are in the `growth` skill.
>
> **Role Labels:**
> - **[User]:** Developer does it in a browser/console.
> - **[Agent]:** AI/developer can execute directly (edit code, write configurations).
> - **[Validate]:** A verification gate.
>
> *STOP RULE:* Stop at any unchecked **[User]** item and wait for confirmation before proceeding.

## 1. Instrument the app — `setup-analytics`
- [ ] **[Agent]** Add analytics events at funnel points (`Analytics.logScreenView`, key conversions)
- [ ] **[Agent]** Add any new flag to `FeatureFlagManager.Keys` + `DEFAULT_VALUES`
- [ ] **[User]** Enable Analytics, Crashlytics, and Remote Config in the Firebase Console
- [ ] **[User]** Add each flag key as a Remote Config parameter with its production value
- [ ] **[Validate]** An event appears in Firebase DebugView (debug build)

## 2. Notifications — `enable-notifications`
- [ ] **[Agent]** Confirm listeners in `AppInitializer.initializeNotification()`
- [ ] **[Agent]** Prime notification permission with `rememberNotificationPermissionState()` (`add-permission`)
- [ ] **[User]** Generate APNs auth key (`.p8`) in Apple Developer portal
- [ ] **[User]** Upload the `.p8` under Firebase Project settings → Cloud Messaging
- [ ] **[User]** Add Push Notifications + Background Modes (Remote notifications) capabilities in Xcode (target 16.0+)
- [ ] **[User]** Send a test push from Firebase Cloud Messaging → Send a message
- [ ] **[Validate]** Test push received on a real device

## 3. Onboarding — `design-onboarding`
- [ ] **[Agent]** Fill `TAILOR PER APP` in `AiGuidelines/project/onboarding.md` (goal / first-taste / priming)
- [ ] **[Agent]** Build/refine onboarding screens via the `new-screen` skill
- [ ] **[Validate]** Onboarding runs end to end; per-step screen-view events land in DebugView

## 4. Virality + review loops — `add-virality-loop`
- [ ] **[Agent]** Fill `TAILOR PER APP` in `AiGuidelines/project/virality_loops.md`
- [ ] **[Agent]** Build the share/referral + high-intent + win-back surfaces
- [ ] **[Agent]** Wire `rememberInAppReviewTrigger()` at a post-value moment
- [ ] **[Validate]** A share/referral surface works; in-app review prompt fires (7-day cooldown)

## 5. Validation gate
- [ ] **[Validate]** An analytics event shows in Firebase DebugView AND a test push is received on-device
- [ ] **[Validate]** Run the `run-quality-gates` skill

## The loop repeats
- [ ] Measure → improve → measure. Ship changes behind Remote Config flags; re-read the funnel.
