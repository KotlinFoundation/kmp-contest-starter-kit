# Phase 3 — Publishing progress

Copy this file and tick items as you go. Role labels: **[Agent]** you do it, **[User]** the developer
does it in a console / Xcode, **[Validate]** prove it works.

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

## 4. Release signing + keys into CI — `setup-signing`
- [ ] **[Agent]** Generate keystore (`generate_android_keystore.sh` or `keytool`) + gitignored `keystore.properties`
- [ ] **[User]** iOS certificates (Dev + Distribution → `Certificates.p12`) + provisioning profiles, selected in Xcode
- [ ] **[User]** Add Android CI secrets: `SIGNING_KEY_STORE_FILE_BASE64`, `SIGNING_KEY_STORE_PROPERTIES_BASE64`, `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
- [ ] **[User]** Add iOS CI secrets: `IOS_APP_CERTIFICATE_P12_BASE64` (+ password), `APPSTORE_KEY_ID`/`ISSUER_ID`/`PRIVATE_KEY`/`TEAM_ID`, both provision UUIDs
- [ ] **[User]** Add `GRADLE_CACHE_ENCRYPTION_KEY`; back up keystore + passwords safely
- [ ] **[Validate]** `:androidApp:bundleRelease` produces a signed AAB (`keytool -printcert -jarfile` = your cert)

## 5. Store screenshots — `store-screenshots`
- [ ] **[Agent]** `@StoreScreenshot` previews exist for key screens
- [ ] **[Agent]** `./scripts/generate_store_screenshots.sh` → `distribution/store_screenshots/<locale>/<device>/`

## 6. Store URLs + contact in `Constants.kt`
- [ ] **[Agent]** Set `URL_PRIVACY_POLICY`, `URL_TERMS_CONDITIONS`, `CONTACT_EMAIL`
- [ ] **[User]** Privacy + terms pages are published and reachable (no placeholders)

## 7. App Store Connect app — `setup-appstore-connect`
- [ ] **[User]** Create app (bundle id, SKU, primary language) + 1.0.0 version
- [ ] **[User]** Categories, age rating, App Privacy / data usage, App Review info
- [ ] **[User]** Metadata (subtitle/description/keywords/what's-new/support+marketing URLs) + screenshots + 1024 icon
- [ ] **[User]** Copy numeric Apple ID → `Constants.APPSTORE_APP_ID`

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
- [ ] **[Validate]** Signed build uploads to Play internal track / TestFlight and appears in the console
- [ ] **[Validate]** Run the `run-quality-gates` skill

## Next
- [ ] Proceed to the `monetization` phase
