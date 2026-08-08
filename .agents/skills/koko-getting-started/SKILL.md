---
name: koko-getting-started
description: High-level monorepo orchestration and environment setup for the KMP Contest Starter Kit Monorepo (nicknamed Koko).
---

# Koko Monorepo: Getting Started

Welcome to the KMP Contest Starter Kit Monorepo (nicknamed Koko). This project is organized into multiple specialized sub-projects to separate concerns between mobile application development (`MobileApp`) and backend/web services in support of the mobile application (`Web`).

## 🛠️ Development Workflow: The Two-Window Approach

To work effectively with this monorepo, we recommend the Two-Window Approach in Android Studio:

1. **Window 1 (this window): Orchestration & Web**
    * **Purpose**: Managing the monorepo, running orchestration skills, managing the supporting Web app (e.g., that serves as a service layer for the mobile apps including securely handling API Keys), and managing Firebase services.
    * **Features**: Command-line tools, Firebase CLI, and backend configuration.

2. **Window 2 (new window): Mobile Application (`MobileApp/`)**
    * **Purpose**: Active development of the Kotlin Multiplatform app.
    * **Features**: Compose Previews, Android/iOS/Desktop/Web targets, and mobile-specific debugging.
    * **Getting Started**: Open the `MobileApp/` directory in a new Android Studio window and type `Proceed with @koko-mobileapp-getting-started` into the agent's chat panel to begin the Phase 1 developer journey guide.

> [!IMPORTANT]
> **CRITICAL AGENT INSTRUCTION**: Whenever a developer says `please proceed with @koko-getting-started`, "getting started", or asks to start Phase 1, the agent's very first step and response MUST explicitly instruct the developer to open a new Android Studio window for the `MobileApp/` directory. The agent must state this clearly:
> *"Our first step involves opening up a new Android Studio window for the MobileApp directory, so we can utilize the Two-Window Approach. Please open the `MobileApp/` directory (at the repository root) in a new window and type `Proceed with @koko-mobileapp-getting-started` into the agent's chat panel there to get the mobile environment loaded!"*


---

## 🚀 Skill Discovery

This monorepo uses skills in the root level `skills` directory. A summary of available skills can be found by activating the `koko-skills` skill in the `MobileApp` directory. 

### 📱 Mobile Development
For all mobile-specific tasks (UI, Data, Navigation, Monetization, etc.), use the `MobileApp/` window and load the specific skill you need. The phase 1 developer journey is managed via:
- [**koko-mobileapp-getting-started**](../../../MobileApp/.agents/skills/koko-mobileapp-getting-started/SKILL.md)

### 🌐 Web & Backend
Once the KMP app is ready (the getting started skill mentioned above is complete), start working on the `integrations` skill in this window to complete tasks related to Firebase Hosting, Cloud Functions, etc. 
- [**integrations**](../../../skills/integrations/SKILL.md) 

---

## 🚀 Available Skills Index

### Phase 1: First Run (Local-only)
- [ ] **[new-app](../../../skills/new-app/SKILL.md)**: Turn a raw idea into a defined product (prd/user_flow/ui_ux + name/id) — the entry point when starting from just an idea.
- [ ] **[run-the-app](../../../skills/run-the-app/SKILL.md)**: Build and run the app on Android, Desktop, Web, or iOS.
- [ ] **[build-features](../../../skills/build-features/SKILL.md)**: Derive and implement the MVP screens + local models from the product docs (tracked in PROGRESS_FEATURES.md).
- [ ] **[refactor-package](../../../skills/refactor-package/SKILL.md)**: Rebrand the app by renaming package/applicationId/bundle ID and display name.
- [ ] **[new-screen](../../../skills/new-screen/SKILL.md)**: Scaffold a new screen (UI, State, ViewModel, and Navigation).
- [ ] **[new-local-model](../../../skills/new-local-model/SKILL.md)**: Scaffold a new Room 3 entity, DAO, and DI registration.
- [ ] **[add-api-service](../../../skills/add-api-service/SKILL.md)**: Implement a Ktor-backed network request from DTO to Repository.
- [ ] **[save-preferences](../../../skills/save-preferences/SKILL.md)**: Persist simple key/value settings using Jetpack DataStore.
- [ ] **[add-permission](../../../skills/add-permission/SKILL.md)**: Request and handle device runtime permissions.
- [ ] **[new-module](../../../skills/new-module/SKILL.md)**: Add a new Gradle Kotlin Multiplatform module.

### Phase 2: Integrations
- [ ] **[configure-environment](../../../skills/configure-environment/SKILL.md)**: Manage environment variables and configuration values.
- [ ] **[setup-firebase](../../../skills/setup-firebase/SKILL.md)**: Connect the app to Firebase services.
- [ ] **[enable-auth](../../../skills/enable-auth/SKILL.md)**: Integrate social sign-in (Google/Apple).
- [ ] **[integrate-web-proxy](../../../skills/integrate-web-proxy/SKILL.md)**: Connect the mobile app to the Firebase Cloud Functions proxy.
- [ ] **[sync-data-firebase](../../../skills/sync-data-firebase/SKILL.md)**: Put data in Firestore, sync across devices, or make state server-authoritative (optional — asks the wasmJs trade-off first).

### Phase 3: Publication
- [ ] **[generate-app-icons](../../../skills/generate-app-icons/SKILL.md)**: Generate and set launcher icons.
- [ ] **[bump-version](../../../skills/bump-version/SKILL.md)**: Increment build versions for Android and iOS simultaneously.
- [ ] **[setup-signing](../../../skills/setup-signing/SKILL.md)**: Configure release signing and secure key management.
- [ ] **[capture-app-screens](../../../skills/capture-app-screens/SKILL.md)**: Render your real screens to plain PNGs at App Store / Play Store pixel sizes (plain UI captures, not designed marketing images).
- [ ] **[setup-appstore-connect](../../../skills/setup-appstore-connect/SKILL.md)**: Set up the Apple App Store Connect listing.
- [ ] **[setup-google-play](../../../skills/setup-google-play/SKILL.md)**: Set up the Google Play Console listing.
- [ ] **[publish-release](../../../skills/publish-release/SKILL.md)**: Build and submit final artifacts for review.

### Phase 4: Monetization
- [ ] **[design-paywall](../../../skills/design-paywall/SKILL.md)**: Design and implement the subscription/credit paywall UI.
- [ ] **[setup-subscriptions](../../../skills/setup-subscriptions/SKILL.md)**: Integrate Adapty or RevenueCat subscription providers.
- [ ] **[enable-credits](../../../skills/enable-credits/SKILL.md)**: Implement a credit balance and credit-pack purchase system.
- [ ] **[enable-ads](../../../skills/enable-ads/SKILL.md)**: Integrate AdMob for banner, interstitial, or rewarded ads.

### Phase 5: Growth
- [ ] **[setup-analytics](../../../skills/setup-analytics/SKILL.md)**: Implement Firebase Analytics, Crashlytics, and Remote Config.
- [ ] **[enable-notifications](../../../skills/enable-notifications/SKILL.md)**: Implement push notifications and local alerts.
- [ ] **[design-onboarding](../../../skills/design-onboarding/SKILL.md)**: Design and implement user onboarding flows.
- [ ] **[add-virality-loop](../../../skills/add-virality-loop/SKILL.md)**: Implement sharing and referral mechanisms.

### 🛠️ Quality & Maintenance
- [ ] **[run-quality-gates](../../../skills/run-quality-gates/SKILL.md)**: Execute lint checks, unit tests, and build validation.
- [ ] **[verify-ui](../../../skills/verify-ui/SKILL.md)**: Verify a screen's behaviour (headless Compose test) and appearance (render a `@Preview` to a PNG).
