---
name: publishing
description: Phase-3 guide for publishing the KMP contest starter kit / publish to Play Store and App Store — icons, release signing (keys moved into CI secrets), store metadata + screenshots, App Store Connect + Google Play Console app creation, and the first build submitted for review. Use when the developer wants to publish / ship / release the app to the stores, prepare store listings and signing, or asks "is my app ready to publish / release?" (run the Release readiness gate below).
---

# Phase 3 — Publishing (ship to Google Play + App Store)

**Goal:** take the integrated app (from the `integrations` phase) to the stores — final app
identity + icons, **release signing with keys moved out of the app into CI secrets**, store
metadata + screenshots, App Store Connect and Google Play Console apps created, and the first
signed build submitted for review.

This is the **blueprint orchestrator** for the phase. Work the checklist top to bottom. Each
step is tagged **Agent Action** (you do it), **User Action** (the developer does it in a
console / Xcode), or **Validation** (prove it works). Steps compose the atomic skills — read
the referenced skill before doing that step.

Track progress in a copy of [`progress-template.md`](progress-template.md).

## Release readiness gate — run this when asked "is my app ready to publish?"

Don't answer from memory — **run the checks and report what's unmet**, then point at the step/skill that fixes each. Two parts:

**A. Automated config check.** From `MobileApp/`:
```bash
./scripts/check_env.sh --phase publishing
```
Every `⚠️` must be resolved before release. It covers:
- `AppConfiguration.URL_PRIVACY_POLICY` / `URL_TERMS_CONDITIONS` set to **live, reachable** pages (stores reject placeholders).
- `AppConfiguration.CONTACT_EMAIL` is your own (not the `support@example.com` boilerplate).
- **AI backend uses the proxy, not on-device keys** — flags `USE_AI_PROXY_SERVER=false`, or a blank
  `CLOUD_FUNCTIONS_URL` with a direct `OPENAI_API_KEY`/`REPLICATE_API_KEY` (key would ship in the binary).
  Production must deploy the web-proxy (`integrate-web-proxy`) and clear direct keys.
- **Subscription keys are real, not the demo mock** — if you sell subscriptions/IAPs, set the real
  Adapty/RevenueCat SDK keys (`setup-subscriptions`); otherwise the paywall runs the client-only mock.
  (Skip entirely if the app has **no premium features** — `AppConfiguration.PREMIUM_FEATURES_ENABLED = false`;
  `check_env` then reports subscription keys as `n/a`.)
- `AppConfiguration.APPSTORE_APP_ID` (set once App Store Connect assigns it).

**B. Human-only readiness (can't be auto-checked)** — confirm each, sending the developer to the step/skill:
- Package / applicationId / bundle id + display name **locked** (`refactor-package`, step 1).
- Final **app icons** generated (`generate-app-icons`, step 2).
- **Release signing keys moved into CI secrets**, keystore backed up (`setup-signing`, step 4) — never in the app.
- **Version** bumped for both platforms (`bump-version`, step 3).
- **Store listings** created + metadata/graphics/data-safety/content-rating filled: App Store Connect
  (`setup-appstore-connect`) and Google Play Console (`setup-google-play`), steps 7–8.
- **Store screenshots** generated (`store-screenshots`, step 5).
- `run-quality-gates` passes.

The published docs mirror this: **`Documentation/docs/production/pre-publishing-checklist.md`**. If any item
is unmet, walk the developer through the matching numbered step below.

## STOP rule (read verbatim)

> When the next unchecked item is a **User Action**, stop and wait for the developer to
> confirm they've done it before continuing. Never fabricate store listings, signing
> identities, or review submissions.

Store-console work (App Store Connect, Google Play Console) and Xcode signing are **User
Actions** — you cannot create developer accounts, listings, certificates, or submit for
review. Guide precisely, stage local files, then wait.

## Security principle (read before signing)

> Signing keys and secrets must never be committed. Move the keystore, `keystore.properties`,
> the iOS `.p12` + ASC API key, and any embedded API keys **out of the app** into CI secrets
> (GitHub Actions) or the backend. The starter kit's `.gitignore` already excludes
> `keystore.jks`, `keystore.properties`, `*.aab`, `*.ipa`, and `local.properties` — keep them
> there and put the CI-facing copies in **repo Settings → Secrets and variables → Actions**.

## Keys / secrets you'll need

- **`Constants.kt` fields** (required) — set these before you ship:
  - `URL_PRIVACY_POLICY` + `URL_TERMS_CONDITIONS` — your published legal URLs.
  - `CONTACT_EMAIL` — your own support email (ships as boilerplate `support@example.com`).
  - `APPSTORE_APP_ID` — numeric App Store id for rate/review + manage-subscription deep links; set it
    once App Store Connect assigns it (`setup-appstore-connect`).
- **Signing + store credentials** (required — CI secrets, not local files) — upload keystore, App
  Store Connect API key, provisioning profiles, Play service account. All move into **GitHub Actions
  secrets**, never committed — see `setup-signing`.

Verify Constants config: `./scripts/check_env.sh --phase publishing`.

## Checklist

### 1. Lock the final app identity — `refactor-package`
- **Agent Action** — If the app id / bundle id / display name aren't final yet, read the
  `refactor-package` skill and run `./scripts/refactor_package.sh --app-id … --app-name …`
  from `MobileApp/`. The Play package name binds permanently on first upload and the iOS
  bundle id must match the ASC record — get it right **now**. Skip if already done in an
  earlier phase.

### 2. Generate app icons — `generate-app-icons`
- **Agent Action** — Read the `generate-app-icons` skill. From one square source logo,
  produce the iOS `AppIcon.appiconset` (all sizes + `Contents.json`) and the Android adaptive
  mipmaps (5 density buckets + `ic_launcher_background` color), plus the native splash logo.
- **User Action** — Supply the source logo (≥1024×1024 PNG) and confirm the generated icons
  look right in the launcher / Xcode icon slots. **Stop and confirm.**
- **Validation** — `./gradlew :androidApp:assembleDebug` succeeds and the launcher icon
  renders crisp; Xcode shows a full icon set with no missing slots.

### 3. Bump the release version — `bump-version`
- **Agent Action** — Read the `bump-version` skill. Run `./scripts/update_version.sh`
  (or `-v X.Y.Z`) from `MobileApp/` so Android `versionCode`/`versionName` and the iOS build
  number / marketing version move together. Show the resulting versions.

### 4. Set up release signing + move keys into CI — `setup-signing`
- **Agent Action** — Read the `setup-signing` skill. Run
  `./scripts/generate_android_keystore.sh "Name" "Org"` (or `keytool` manually) to create the
  gitignored `keystore.jks` + `keystore.properties`. Confirm `androidApp/build.gradle.kts`
  picks them up (it does automatically).
- **User Action** — Create iOS certificates (Apple Development + Distribution → `Certificates.p12`)
  and provisioning profiles, select them in Xcode. Add **all** signing secrets to GitHub
  Actions: `SIGNING_KEY_STORE_FILE_BASE64`, `SIGNING_KEY_STORE_PROPERTIES_BASE64`,
  `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`, `IOS_APP_CERTIFICATE_P12_BASE64` (+ password),
  `APPSTORE_KEY_ID` / `APPSTORE_ISSUER_ID` / `APPSTORE_PRIVATE_KEY` / `APPSTORE_TEAM_ID`,
  the two provision UUIDs, and `GRADLE_CACHE_ENCRYPTION_KEY`. **Back up the keystore + passwords.
  Stop and confirm.**
- **Validation** — `./gradlew :androidApp:bundleRelease` produces a **signed** AAB
  (`keytool -printcert -jarfile <aab>` shows your cert, not the debug cert).

### 5. Generate store screenshots — `store-screenshots`
- **Agent Action** — Read the `store-screenshots` skill. Ensure `@StoreScreenshot` previews
  exist for the key screens, then run `./scripts/generate_store_screenshots.sh`. Output lands
  at `distribution/store_screenshots/<locale>/<device>/`.

### 6. Set store URLs + contact in `Constants.kt`
- **Agent Action** — In `shared/src/commonMain/.../util/Constants.kt` set
  `URL_PRIVACY_POLICY`, `URL_TERMS_CONDITIONS`, and `CONTACT_EMAIL`. These must be **real,
  reachable** pages — both stores reject placeholders. (`APPSTORE_APP_ID` is filled in step 7
  once the ASC app exists.)
- **User Action** — Confirm the privacy/terms pages are published (e.g. via the `Web/public`
  landing-page template). **Stop and confirm.**

### 7. Create the App Store Connect app — `setup-appstore-connect`
- **Agent Action** — Read the `setup-appstore-connect` skill; walk the developer through it.
- **User Action** — In https://appstoreconnect.apple.com create the app (bundle id, SKU,
  primary language), the 1.0.0 version, categories, age-rating questionnaire, App Privacy /
  data usage, App Review info, and localized metadata (subtitle / description / keywords /
  what's-new / support + marketing URLs) with the generated screenshots + 1024 icon. Copy the
  numeric **Apple ID** back into `AppConfiguration.APPSTORE_APP_ID`. **Stop and confirm.**

### 8. Create the Google Play Console app — `setup-google-play`
- **Agent Action** — Read the `setup-google-play` skill; walk the developer through it.
- **User Action** — In https://play.google.com/console create the app; complete the main
  store listing (title / short + full description / 512 icon / **1024×500 feature graphic** /
  phone screenshots), the **Data safety** questionnaire, **content rating**, set up an
  **internal testing** track, and create the Play **service account** JSON for CI. **Stop and
  confirm.**

### 9. Build + submit the first release — `publish-release`
- **Agent Action** — Read the `publish-release` skill. Build the signed Android AAB
  (`./gradlew :androidApp:bundleRelease` → `androidApp/build/outputs/bundle/release/androidApp-release.aab`)
  and, for iOS, archive in Xcode or run the Fastlane `appstore_release` lane. First Play
  upload is manual (`fastlane android first_time_build` → upload in console); afterwards
  Fastlane `playstore_release` / TestFlight, or the tag-driven CI workflows
  (`*-android` / `*-ios` tags).
- **User Action** — Upload the first AAB to the Play **internal** track, upload the iOS build
  to **TestFlight**, then **submit for review** on both. **Stop and confirm.**

### 10. Validation gate
- **Validation** — Run `./scripts/check_env.sh --phase publishing` from `MobileApp/` — the
  `Constants.kt` legal URLs + `CONTACT_EMAIL` must be set (no `⚠️`), `APPSTORE_APP_ID` once ASC
  assigned it.
- **Validation** — The signed release build **uploads to the Play internal track /
  TestFlight and appears in the console**. Run the `run-quality-gates` skill before tagging a
  release.

## Done → next phase

When the first signed build is in the Play internal track and TestFlight and submitted for
review, the app is publishable. **Trigger the next phase explicitly** — tell your agent **"start the
monetization phase"** (or run the `monetization` skill) to add subscriptions, credit-pack IAPs, the
paywall, and ads (the store subscription/IAP products are created there, not here).
