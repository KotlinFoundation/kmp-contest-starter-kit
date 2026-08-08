---
name: setup-google-play
description: Manually create and configure the app in Google Play Console — store listing, graphics, data safety, content rating, an internal testing track, and the CI service account. Use when the user is setting up the Play Store listing before an Android release.
---

# Set up Google Play Console (manual)

Create and configure the Android app in Google Play Console. This is **User Action** work
in the web console — the agent guides and stages local assets; it cannot create listings.
Console: https://play.google.com/console

Prerequisite: a **Google Play Developer account** (one-time $25 registration).

## 1. Create the app

Play Console → **Create app**:

- **App name** (your public name)
- **Default language**
- **App or game**, **Free or paid**
- Accept the developer program + export-law declarations

The Android **package name** (`applicationId` in `androidApp/build.gradle.kts`) is bound
to the app on the **first AAB upload**, not at creation — it is permanent afterward, so make
sure the app id is final (use the `refactor-package` skill first if not).

## 2. Store listing (Grow → Store presence → Main store listing)

- **App name** (30 chars), **Short description** (80 chars), **Full description** (4000)
- **App icon** — 512×512 PNG (32-bit, from the `generate-app-icons` source)
- **Feature graphic** — **1024×500** PNG/JPG (required)
- **Phone screenshots** — 2–8. Plain captures of your screens come from the `capture-app-screens`
  skill (`distribution/store_screenshots/<locale>/<device>/`). Play accepts those as-is; if you want
  designed images with headlines and device frames, compose them in your own design tool using these
  PNGs. Add tablet screenshots if you ship tablet support.

Category, contact email (use `AppConfiguration.CONTACT_EMAIL`), and **Privacy Policy URL** (must
match `AppConfiguration.URL_PRIVACY_POLICY` and be reachable) are set under **Store settings** /
**App content**.

## 3. App content (Policy → App content)

Complete each required declaration:

- **Privacy policy** — the reachable URL above.
- **Data safety** — questionnaire: what data is collected/shared and why (Firebase
  Analytics/Crashlytics → device/usage identifiers; auth → account). Answer honestly.
- **Content rating** — IARC questionnaire (derives the rating).
- **Target audience**, **Ads** (declare if AdMob is enabled), **Government apps**, etc.

## 4. Internal testing track (Test and release → Testing → Internal testing)

- Create the track, add tester emails (or a tester Google Group).
- This is where the first AAB lands. Upload it via the `publish-release` skill / Fastlane
  `playstore_release` (default `track:internal`), or manually **Create new release → upload
  the AAB**. The **first upload must be done here in the console** — subsequent releases can
  be automated.

## 5. Service account for CI (automated releases)

To let Fastlane / `publish_android_playstore.yml` upload without manual steps:

1. In Play Console → **Users and permissions → Invite / API access**, or via Google Cloud
   Console, create a **service account** and grant it Play Console access (Release manager).
2. Download the service-account **JSON key**.
3. Store it as the `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` GitHub secret (see `setup-signing`)
   and, for local Fastlane, at `~/credentials/google-service-app-publisher.json`.

Full steps: the
[upload-google-play action docs](https://github.com/r0adkll/upload-google-play?tab=readme-ov-file#configure-access-via-service-account).

## Note — subscriptions / IAPs are deferred

Do **not** create subscription or in-app products here. Play billing products are set up in
the **`monetization`** phase. This skill only creates the app, its listing, and the internal
track so a first build can be uploaded.

## Validate

**App content** shows all required declarations complete (green), the store listing has no
missing-asset warnings, and the internal testing track is ready to receive an AAB.
