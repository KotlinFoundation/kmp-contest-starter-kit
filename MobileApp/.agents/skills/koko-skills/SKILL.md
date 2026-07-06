---
name: koko-skills
description: Reference index for all available development skills in the Koko (KMPStarterKit) monorepo. Use when working in the MobileApp/ directory to discover step-by-step guides for adding screens, Room entities, preferences, permissions, and building or publishing the app.
---

# Koko (KMPStarterKit) Agent Skills Index

When working inside the `MobileApp/` Android Studio window, you are isolated from the root directory context. However, a comprehensive suite of development skills exists one level up in the parent directory (`../skills/`). 

This index provides you with the name, location, and exact description of every skill available in the project so you can reference and execute them directly.

---

## Accessing Skills
To use any of these skills, read the corresponding `SKILL.md` file using your file-reading tool. 
All paths below are relative to the `MobileApp/` directory.

---

## 🚀 The Developer Journey (5 Phase Guides)

These sequential guides walk you from first-run setup to monetization and growth.

| Guide Name | Location | Description |
| :--- | :--- | :--- |
| **`getting-started`** | [SKILL.md](../skills/getting-started/SKILL.md) | Phase 1: Run the app locally (local-only), rebrand package IDs, and implement local Room, Preferences, permission, and Ktor loops. |
| **`integrations`** | [SKILL.md](../skills/integrations/SKILL.md) | Phase 2: Connect Firebase, set up Social Sign-In, and configure/deploy the Node.js backend proxy. |
| **`publishing`** | [SKILL.md](../skills/publishing/SKILL.md) | Phase 3: Set up app/launcher icons, configure secure release signing, and register store listings. |
| **`monetization`** | [SKILL.md](../skills/monetization/SKILL.md) | Phase 4: Configure paywalls, Adapty/RevenueCat subscriptions, credit-pack IAPs, and AdMob ads. |
| **`growth`** | [SKILL.md](../skills/growth/SKILL.md) | Phase 5: Integrate Firebase Analytics, Crashlytics, Remote Config, Push Notifications, and onboarding loops. |

---

## 🛠️ Phase 1 Tasks (First Run & Local Loop)

| Skill Name | Location | Description |
| :--- | :--- | :--- |
| **`run-the-app`** | [SKILL.md](../skills/run-the-app/SKILL.md) | Instructions to run/build the app on Android, Desktop, Web, or iOS from source. |
| **`refactor-package`** | [SKILL.md](../skills/refactor-package/SKILL.md) | Shell-script command to safely rename Android package namespaces, app IDs, and iOS bundles. |
| **`new-screen`** | [SKILL.md](../skills/new-screen/SKILL.md) | Scaffold a new Screen, UiState, and ViewModel, and register them with Jetpack Navigation 3 & Koin. |
| **`new-local-model`** | [SKILL.md](../skills/new-local-model/SKILL.md) | Scaffold a new Room 3 Entity, Dao, Database registration, and dependency injection binding. |
| **`add-api-service`** | [SKILL.md](../skills/add-api-service/SKILL.md) | Implement a Ktor-backed HTTP request with safe exception handling in repositories. |
| **`save-preferences`** | [SKILL.md](../skills/save-preferences/SKILL.md) | Persist and read typed key-value pairs using Jetpack DataStore Preferences. |
| **`add-permission`** | [SKILL.md](../skills/add-permission/SKILL.md) | Request and manage device permissions (Camera, Location, Push, etc.) using Calf wrappers. |
| **`new-module`** | [SKILL.md](../skills/new-module/SKILL.md) | Setup and configure a new Gradle Kotlin Multiplatform library module. |

---

## ⚙️ Phase 2 Tasks (Integrations)

| Skill Name | Location | Description |
| :--- | :--- | :--- |
| **`configure-environment`** | [SKILL.md](../skills/configure-environment/SKILL.md) | Find and assign API keys and configuration constants across target environments. |
| **`setup-firebase`** | [SKILL.md](../skills/setup-firebase/SKILL.md) | Wire Android/iOS targets to Firebase Services and generate config credentials. |
| **`enable-auth`** | [SKILL.md](../skills/enable-auth/SKILL.md) | Integrate Firebase Authentication with Google and Apple Sign-In. |
| **`integrate-web-proxy`** | [SKILL.md](../skills/integrate-web-proxy/SKILL.md) | Wire the app securely to Firebase Cloud Functions for backend logic. |

---

## 📦 Phase 3 Tasks (Publication)

| Skill Name | Location | Description |
| :--- | :--- | :--- |
| **`generate-app-icons`** | [SKILL.md](../skills/generate-app-icons/SKILL.md) | Automatically generate and export app launcher icons. |
| **`bump-version`** | [SKILL.md](../skills/bump-version/SKILL.md) | Increment build and marketing version descriptors in parallel for Android and iOS. |
| **`setup-signing`** | [SKILL.md](../skills/setup-signing/SKILL.md) | Setup secure build-signing keystores and push keys to CI environments. |
| **`store-screenshots`** | [SKILL.md](../skills/store-screenshots/SKILL.md) | Generate store preview assets from `@StoreScreenshot` annotated Compose previews. |
| **`setup-appstore-connect`** | [SKILL.md](../skills/setup-appstore-connect/SKILL.md) | Step-by-step Apple developer console setup. |
| **`setup-google-play`** | [SKILL.md](../skills/setup-google-play/SKILL.md) | Step-by-step Google Play developer console setup. |
| **`publish-release`** | [SKILL.md](../skills/publish-release/SKILL.md) | Submit finalized builds to their respective App Stores for review. |

---

## 💳 Phase 4 Tasks (Monetization)

| Skill Name | Location | Description |
| :--- | :--- | :--- |
| **`design-paywall`** | [SKILL.md](../skills/design-paywall/SKILL.md) | Configure paywall layouts, features, and marketing copy. |
| **`setup-subscriptions`** | [SKILL.md](../skills/setup-subscriptions/SKILL.md) | Integrate Adapty or RevenueCat billing systems. |
| **`enable-credits`** | [SKILL.md](../skills/enable-credits/SKILL.md) | Set up a credit consumption engine and corresponding IAP packages. |
| **`enable-ads`** | [SKILL.md](../skills/enable-ads/SKILL.md) | Display AdMob Banner, Interstitial, or Rewarded ads. |

---

## 📈 Phase 5 Tasks (Growth & Optimization)

| Skill Name | Location | Description |
| :--- | :--- | :--- |
| **`setup-analytics`** | [SKILL.md](../skills/setup-analytics/SKILL.md) | Set up Firebase Analytics event tracking, Crashlytics, and Remote Config flags. |
| **`enable-notifications`** | [SKILL.md](../skills/enable-notifications/SKILL.md) | Register device push notifications and trigger local alerts. |
| **`design-onboarding`** | [SKILL.md](../skills/design-onboarding/SKILL.md) | Optimize first-time user engagement flows and feature onboarding. |
| **`add-virality-loop`** | [SKILL.md](../skills/add-virality-loop/SKILL.md) | Add share features, referral links, and App Store rating prompts. |

---

## 🧪 Global Tasks

| Skill Name | Location | Description |
| :--- | :--- | :--- |
| **`run-quality-gates`** | [SKILL.md](../skills/run-quality-gates/SKILL.md) | Execute codebase standard checks (Spotless code formatting check, unit tests, debug build validations). |

> [!NOTE]
> When executing a skill, make sure you perform any associated bash/gradle commands from the `MobileApp/` directory. Every path inside the parent skills expects execution inside the mobile subproject.
