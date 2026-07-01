---
name: publish-release
description: Build the signed release artifacts and ship them — Android AAB to a Play track and iOS archive to TestFlight/App Store, via Fastlane lanes or the tag-driven CI workflows, then submit for review. Use when the user wants to build a release, upload to Play/TestFlight, cut a release, or submit the app for review.
---

# Build and publish a release

Produce the signed release artifacts and upload them. Prereqs (do these first): release
signing wired (`setup-signing`), store apps created (`setup-google-play`,
`setup-appstore-connect`), version bumped (`bump-version`), release notes updated in
`distribution/whatsnew/whatsnew-en-US`.

All commands run from `MobileApp/`. Fastlane is the pre-wired path
(`MobileApp/fastlane/Fastfile`); the tag-driven GitHub workflows do the same in CI.

## Android

### Build the signed AAB

```bash
./gradlew :androidApp:bundleRelease
# → androidApp/build/outputs/bundle/release/androidApp-release.aab
```

Signed automatically when `distribution/android/keystore/keystore.properties` exists
(see `setup-signing`).

### First upload (manual — required by Google)

The **very first** upload to a new Play app must go through the console. Use the Fastlane
helper to build + stage the AAB, then upload it by hand:

```bash
fastlane android first_time_build
# copies the AAB to distribution/android/app-release.aab for manual upload
```

Upload it in Play Console → **Internal testing → Create new release** (see
`setup-google-play`).

### Subsequent releases (automated)

```bash
fastlane android playstore_release                    # internal track (default)
fastlane android playstore_release track:production    # promote to production
# useful options: upload_metadata:true upload_screenshots:true submit_for_review:true
```

Uses the Fastlane `supply` action + the Play service-account JSON
(`~/credentials/google-service-app-publisher.json`, or `service_account:` option).

## iOS

### Easiest — Xcode

Open the workspace → **Product → Archive** → in Organizer, **Distribute App** → App Store
Connect → upload to TestFlight / App Store.

### Fastlane

```bash
fastlane ios appstore_release                       # build + upload IPA to App Store Connect
fastlane ios appstore_release submit_for_review:true # auto-submit for review
# also: upload_metadata:true upload_screenshots:true
```

It archives the `iosApp` scheme (Release), exports to `distribution/ios/iosApp.ipa`, and
uploads via the ASC API key (Fastlane `pilot`/`deliver` under the hood). If Xcode warns
about SwiftPM linkage, follow the linkage-package steps in
`Documentation/docs/production/iOS.md` (and `AGENTS.md` → iOS SwiftPM) before archiving.

## CI path (tag-driven)

The GitHub workflows do the above in CI when you push a version tag (after the CI secrets
from `setup-signing` are set):

- push a `*-android` tag → `.github/workflows/publish_android_playstore.yml` → Play internal
  track
- push a `*-ios` tag → `.github/workflows/publish_ios_appstore.yml` → App Store Connect

Only push tags when the user asks to release.

## Submit for review

- **Android:** in Play Console, promote the internal-track build to a review-eligible track
  (closed/open testing or production) → **Send for review**. Or Fastlane
  `playstore_release track:production submit_for_review:true`.
- **iOS:** in App Store Connect, attach the uploaded build to the 1.0.0 version → **Add for
  Review → Submit**. Or `appstore_release submit_for_review:true`.

## Validate

The signed release build **uploads and appears in the console**: on the Play **internal
track** (Play Console → Internal testing) and in **TestFlight** (App Store Connect →
TestFlight). Once verified there, submit for review.
