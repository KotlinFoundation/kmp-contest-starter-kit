# Phase 3 Progress — Publishing

> [!NOTE]
> **Setup Instruction:** Copy this template file to the root of your repository and rename it to **`PROGRESS_P3_PUBLISHING.md`**.
> Use this file to tick items as you go. Full instructions are in the `publishing` skill.
>
> **Role Labels:**
> - **[User]:** Developer does it in a console / Xcode / developer portal.
> - **[Agent]:** AI/developer can execute directly (edit build files, generate assets, run scripts).
> - **[Validate]:** A verification gate.
>
> *STOP RULE:* Stop at any unchecked **[User]** item and wait for confirmation before proceeding.

## 0. Release readiness gate ("is my app ready to publish?")
- [ ] **[Validate]** `./scripts/check_env.sh --phase publishing` — no `⚠️` (privacy/terms URLs, CONTACT_EMAIL, AI backend on the proxy not on-device keys, real subscription keys)
- [ ] **[Validate]** AI uses the **web-proxy** in production — `CLOUD_FUNCTIONS_URL` set, no direct `OPENAI_API_KEY`/`REPLICATE_API_KEY` shipping, `USE_AI_PROXY_SERVER` not forced to direct
- [ ] **[Validate]** Real subscription keys set (not the demo mock) if the app sells subscriptions/IAPs
- [ ] **[Validate]** All items 1–10 below complete; `run-quality-gates` passes

## 1. Lock final app identity — `refactor-package`
- [ ] **[Agent]** App id / bundle id / display name are final (`./scripts/refactor_package.sh …`, or already done)

## 2. App icons — `generate-app-icons`
- [ ] **[User]** Provide square source logo (≥1024×1024 PNG)
- [ ] **[Agent]** Generate iOS `AppIcon.appiconset` (all sizes + `Contents.json`)
- [ ] **[Agent]** Generate Android adaptive mipmaps (5 buckets) + `ic_launcher_background` color
- [ ] **[Agent]** Update native splash logo (iOS `ic_logo.imageset`, splash background colors)
- [ ] **[Validate]** `:androidApp:assembleDebug` OK; launcher icon crisp; Xcode icon set complete

## 3. Bump version — `bump-version`
- [ ] **[Agent]** `./scripts/update_version.sh` (Android + iOS together); show versions

## 4. Release signing — `setup-signing`
- [ ] **[Agent]** Generate keystore (`generate_android_keystore.sh` or `keytool`) + gitignored `keystore.properties`
- [ ] **[User]** iOS certificates (Dev + Distribution → `Certificates.p12`) + provisioning profiles, selected in Xcode
- [ ] **[User]** Back up keystore + passwords safely (losing the Android keystore = can never update the app)
- [ ] **[User]** *(optional — only if publishing via CI)* Add signing secrets to GitHub Actions: `SIGNING_KEY_STORE_FILE_BASE64`, `SIGNING_KEY_STORE_PROPERTIES_BASE64`, `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`, `IOS_APP_CERTIFICATE_P12_BASE64` (+ password), `APPSTORE_KEY_ID`/`ISSUER_ID`/`PRIVATE_KEY`/`TEAM_ID`, both provision UUIDs, `GRADLE_CACHE_ENCRYPTION_KEY`
- [ ] **[Validate]** `:androidApp:bundleRelease` produces a signed AAB (`keytool -printcert -jarfile` = your cert)

## 5. Store screenshots — `capture-app-screens`
- [ ] **[Agent]** `@StoreScreenshot` previews exist for key screens
- [ ] **[Agent]** `./scripts/generate_store_screenshots.sh` → `distribution/store_screenshots/<locale>/<device>/`
- [ ] **[User]** Decided: ship these plain captures as-is, or use them as the base artwork for designed marketing images made outside this kit

## 6. Store URLs + contact in `AppConfiguration.kt`
- [ ] **[Agent]** Set `URL_PRIVACY_POLICY`, `URL_TERMS_CONDITIONS`, `CONTACT_EMAIL`
- [ ] **[User]** Privacy + terms pages are published and reachable (no placeholders)

## 7. App Store Connect app — `setup-appstore-connect`
- [ ] **[User]** Create app (bundle id, SKU, primary language) + 1.0.0 version
- [ ] **[User]** Categories, age rating, App Privacy / data usage, App Review info
- [ ] **[User]** Metadata (subtitle/description/keywords/what's-new/support+marketing URLs) + screenshots + 1024 icon
- [ ] **[User]** Copy numeric Apple ID → `AppConfiguration.APPSTORE_APP_ID`

## 8. Google Play Console app — `setup-google-play`
- [ ] **[User]** Create app
- [ ] **[User]** Main store listing (title / short + full description / 512 icon / 1024×500 feature graphic / screenshots)
- [ ] **[User]** Data safety questionnaire + content rating
- [ ] **[User]** Internal testing track set up
- [ ] **[User]** Play service-account JSON created for CI

## 9. Build + submit first release — `publish-release`
- [ ] **[Agent]** Build signed AAB (`:androidApp:bundleRelease`); iOS archive / Fastlane `appstore_release`
- [ ] **[User]** Upload first AAB to Play internal track (manual first time)
- [ ] **[User]** Upload iOS build to TestFlight
- [ ] **[User]** Submit for review on both stores

## 10. Validation gate
- [ ] **[Validate]** `./scripts/check_env.sh --phase publishing` — AppConfiguration legal URLs + `CONTACT_EMAIL` set
- [ ] **[Validate]** Signed build uploads to Play internal track / TestFlight and appears in the console
- [ ] **[Validate]** Run the `run-quality-gates` skill

## Next
- [ ] Proceed to the `monetization` phase
