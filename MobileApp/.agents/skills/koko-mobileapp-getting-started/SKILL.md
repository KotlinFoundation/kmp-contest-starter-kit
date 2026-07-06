---
name: koko-mobileapp-getting-started
description: Environment setup and mobile-specific skill discovery for the Koko application.
---

# Koko MobileApp: Getting Started

This skill is dedicated to the development and configuration of the Koko (KMPStarterKit) mobile application.

## 🛠️ Environment Setup

Ensure your environment is configured for Kotlin Multiplatform and Compose Multiplatform development.

- [ ] **Open Android Studio (Mobile Window)**
    - Open the `MobileApp/` directory in its own dedicated Android Studio window.
- [ ] **Verify JetBrains KMP Plugin**
    - Navigate to `Settings` (macOS: `Android Studio` -> `Settings`) -> `Plugins`.
    - Ensure that the **Kotlin** and **Kotlin Multiplatform** plugins are installed and up to date.
    - *Note: The KMP plugin is essential for Compose Multiplatform previews and KMP project synchronization.*

---

## 🚀 Mobile Development Skills Index

The following skills are used to build, refactor, and extend the mobile application. Execute a skill by asking the agent to "Run the [Skill Name] skill".

### Phase 1: First Run (Local-only)
- [ ] **[run-the-app](../../../../skills/run-the-app/SKILL.md)**: Build and run the app on Android, Desktop, Web, or iOS.
- [ ] **[refactor-package](../../../../skills/refactor-package/SKILL.md)**: Rebrand the app by renaming package/applicationId/bundle ID and display name.
- [ ] **[new-screen](../../../../skills/new-screen/SKILL.md)**: Scaffold a new screen (UI, State, ViewModel, and Navigation).
- [ ] **[new-local-model](../../../../skills/new-local-model/SKILL.md)**: Scaffold a new Room 3 entity, DAO, and DI registration.
- [ ] **[add-api-service](../../../../skills/add-api-service/SKILL.md)**: Implement a Ktor-backed network request from DTO to Repository.
- [ ] **[save-preferences](../../../../skills/save-preferences/SKILL.md)**: Persist simple key/value settings using Jetpack DataStore.
- [ ] **[add-permission](../../../../skills/add-permission/SKILL.md)**: Request and handle device runtime permissions.
- [ ] **[new-module](../../../../skills/new-module/SKILL.md)**: Add a new Gradle Kotlin Multiplatform module.

### Phase 2: Integrations
- [ ] **[configure-environment](../../../../skills/configure-environment/SKILL.md)**: Manage environment variables and configuration values.
- [ ] **[setup-firebase](../../../../skills/setup-firebase/SKILL.md)**: Connect the app to Firebase services.
- [ ] **[enable-auth](../../../../skills/enable-auth/SKILL.md)**: Integrate social sign-in (Google/Apple).
- [ ] **[integrate-web-proxy](../../../../skills/integrate-web-proxy/SKILL.md)**: Connect the mobile app to the Firebase Cloud Functions proxy.

### Phase 3: Publication
- [ ] **[generate-app-icons](../../../../skills/generate-app-icons/SKILL.md)**: Generate and set launcher icons.
- [ ] **[bump-version](../../../../skills/bump-version/SKILL.md)**: Increment build versions for Android and iOS simultaneously.
- [ ] **[setup-signing](../../../../skills/setup-signing/SKILL.md)**: Configure release signing and secure key management.
- [ ] **[store-screenshots](../../../../skills/store-screenshots/SKILL.md)**: Generate App Store/Play Store screenshots from Compose Previews.
- [ ] **[setup-appstore-connect](../../../../skills/setup-appstore-connect/SKILL.md)**: Set up the Apple App Store Connect listing.
- [ ] **[setup-google-play](../../../../skills/setup-google-play/SKILL.md)**: Set up the Google Play Console listing.
- [ ] **[publish-release](../../../../skills/publish-release/SKILL.md)**: Build and submit final artifacts for review.

### Phase 4: Monetization
- [ ] **[design-paywall](../../../../skills/design-paywall/SKILL.md)**: Design and implement the subscription/credit paywall UI.
- [ ] **[setup-subscriptions](../../../../skills/setup-subscriptions/SKILL.md)**: Integrate Adapty or RevenueCat subscription providers.
- [ ] **[enable-credits](../../../../skills/enable-credits/SKILL.md)**: Implement a credit balance and credit-pack purchase system.
- [ ] **[enable-ads](../../../../skills/enable-ads/SKILL.md)**: Integrate AdMob for banner, interstitial, or rewarded ads.

### Phase 5: Growth
- [ ] **[setup-analytics](../../../../skills/setup-analytics/SKILL.md)**: Implement Firebase Analytics, Crashlytics, and Remote Config.
- [ ] **[enable-notifications](../../../../skills/enable-notifications/SKILL.md)**: Implement push notifications and local alerts.
- [ ] **[design-onboarding](../../../../skills/design-onboarding/SKILL.md)**: Design and implement user onboarding flows.
- [ ] **[add-virality-loop](../../../../skills/add-virality-loop/SKILL.md)**: Implement sharing and referral mechanisms.

### 🛠️ Quality & Maintenance
- [ ] **[run-quality-gates](../../../../skills/run-quality-gates/SKILL.md)**: Execute lint checks, unit tests, and build validation.
