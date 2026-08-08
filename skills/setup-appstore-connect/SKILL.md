---
name: setup-appstore-connect
description: Manually create and configure the app in App Store Connect — bundle id, version, categories, age rating, privacy, review info, and localized metadata. Use when the user is setting up the App Store listing before an iOS release.
---

# Set up App Store Connect (manual)

Create and configure the iOS app record in App Store Connect. This is **User Action**
work in the web console — the agent guides and stages local metadata files; it cannot
create listings. Console: https://appstoreconnect.apple.com

Prerequisite: an active **Apple Developer Program** membership and the app's **bundle id**
registered at https://developer.apple.com/account/resources/identifiers/list (must match
the iOS `PRODUCT_BUNDLE_IDENTIFIER` in `iosApp/iosApp.xcodeproj/project.pbxproj`).

## 1. Create the app

App Store Connect → **My Apps → + → New App**:

- **Platform:** iOS
- **Name:** your public app name (unique across the App Store)
- **Primary language**
- **Bundle ID:** pick the registered identifier
- **SKU:** any internal unique string (e.g. `yourapp-ios-001`)
- **User access:** Full Access

## 2. Create the 1.0.0 version

In the app → **iOS App** sidebar, a **1.0.0 Prepare for Submission** version exists. Fill:

- **Promotional text** (optional), **Description**, **Keywords** (100-char comma list),
  **Support URL**, **Marketing URL** (optional)
- **Subtitle** (30 chars, shown under the name)
- **What's New in This Version** (release notes)
- **Screenshots** — required per device size. Plain captures of your screens come from the
  `capture-app-screens` skill (`distribution/store_screenshots/<locale>/<device>/`). Apple accepts
  those as-is; if you want designed images with headlines and device frames, compose them in your
  own design tool using these PNGs. Drag the result into the 6.9"/6.5" iPhone and 13" iPad slots.
- **App Icon** — the 1024×1024 marketing icon (from the `generate-app-icons` skill).

## 3. Categories, age rating, and pricing

- **General → App Information:** Primary + Secondary **Category**, Content Rights, and the
  **Age Rating** questionnaire (answer honestly — it derives the rating).
- **Pricing and Availability:** set price tier (Free for most starter apps) and territories.

## 4. App Privacy (data usage)

**App Privacy → Get Started:** declare what data the app collects and how it's used (e.g.
Firebase Analytics/Crashlytics → identifiers/usage data; auth → account). Provide your
**Privacy Policy URL** — this must match `AppConfiguration.URL_PRIVACY_POLICY` and be a real,
reachable page (Apple rejects placeholders). A terms/EULA URL goes in
`AppConfiguration.URL_TERMS_CONDITIONS`.

## 5. App Review Information

At the bottom of the version page:

- **Contact info** (first name, last name, phone, email — use `AppConfiguration.CONTACT_EMAIL`)
- **Sign-in required?** The starter kit signs in **anonymously** by default, so usually no
  demo account is needed; if you enabled gated social login, provide a demo account.
- **Notes** to the reviewer if any feature needs explanation.

## 6. Grab the numeric App ID → `AppConfiguration.kt`

Once the app exists, its **Apple ID** (a numeric id, shown under *App Information → General
Information → Apple ID*) goes into:

```
shared/src/commonMain/.../root/AppConfiguration.kt → APPSTORE_APP_ID = "1234567890"
```

Used by in-app "Rate us" / store deep-links.

## Note — subscriptions / IAPs are deferred

Do **not** create subscription groups or in-app purchase products here. Storefront
monetization products are set up in the **`monetization`** phase (paywall + Adapty/
RevenueCat + IAPs). This skill only creates the app record and its metadata so a first
build can be submitted.

## Validate

The 1.0.0 version page shows no red "missing" indicators for metadata, screenshots, icon,
age rating, and privacy. The app is ready to receive a build (via `publish-release`) and be
submitted for review.
