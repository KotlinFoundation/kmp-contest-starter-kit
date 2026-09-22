---
name: enable-ads
description: >-
  Turn on Google AdMob in KMPStarterKit — flip the ads feature flag, drop banner/interstitial/rewarded
  ad composables in, add the AdMob app + ad-unit IDs to local.properties (+ iOS BaseConfig.xcconfig),
  and update store data-safety declarations. Use when the user wants ads, AdMob, banner/interstitial/
  rewarded ads, or ad-based monetization. Part of the `monetization` phase.
---

# Enable ads (AdMob)

Ads are **off by default**. This skill turns them on and places the three supported ad types. All
manual — AdMob console + a few config + code edits. Gradle commands run from `MobileApp/`.

## 1. Flip the feature flag

Set `IS_ADS_ENABLED` to `true` in `data/source/featureflag/FeatureFlagManager.kt`. It's a feature
flag, so you can also toggle it remotely via **Firebase Remote Config** (key `is_ads_enabled`) without
shipping a build. While ads are disabled, `rememberInterstitialAdDisplayer()` /
`rememberRewardedAdDisplayer()` return `null` — that's expected.

## 2. Ad-unit IDs → `local.properties` (User Action)

These come from the AdMob console — ask the developer to create the app + ad units and paste the IDs;
they can't be generated locally. **Stop and confirm** (or use Google's test ad unit IDs to try the UI
first). Add the IDs for the platforms + ad types you want (`MobileApp/local.properties`):

```properties
# Android
ADMOB_APP_ID_ANDROID=
ADMOB_BANNER_AD_ID_ANDROID=
ADMOB_INTERSTITIAL_AD_ID_ANDROID=
ADMOB_REWARDED_AD_ID_ANDROID=

# iOS
ADMOB_BANNER_AD_ID_IOS=
ADMOB_INTERSTITIAL_AD_ID_IOS=
ADMOB_REWARDED_AD_ID_IOS=
```

**iOS app id** goes in `iosApp`'s `BaseConfig.xcconfig` as `ADMOB_APP_ID=YOUR_ADMOB_IOS_APP_ID_HERE`
(not in `local.properties`).

Create the app + ad units in the [AdMob console](https://apps.admob.com/) (Apps → Add app → Ad units).

## 3. Use test ad IDs during development

**Do not** hammer your real ad units while developing — clicking your own live ads violates AdMob
policy and can get the account suspended. Use Google's official
[sample ad unit IDs](https://developers.google.com/admob/android/test-ads) in `local.properties`
during dev, and swap in the real IDs only for release builds.

## 4. Place ads

**Banner** — drop the composable into any screen:

```kotlin
AdmobBanner(modifier = Modifier.fillMaxWidth())
```

**Interstitial** — full-screen, show on a natural break (level end, screen exit):

```kotlin
val interstitialAdDisplayer = rememberInterstitialAdDisplayer()
// on some action:
interstitialAdDisplayer?.show()
```

**Rewarded** — user opts in for a reward (great with the credit system — grant credits on reward):

```kotlin
val rewardedAdDisplayer = rememberRewardedAdDisplayer(onRewarded = { rewardItem ->
    // reward the user here, e.g. creditRepository.addCredits(...)
})
rewardedAdDisplayer?.show()
```

Preload to avoid a wait — inject `AdsManager` (e.g. `koinInject<AdsManager>()`) and call
`adsManager.rewardedAdLoader.load()` / `adsManager.interstitialAdLoader.load()` ahead of time; both
loaders are singletons so preloading is safe.

## 5. Store data-safety implications (User Action)

AdMob collects the **advertising ID** and device/usage data. You must disclose this in store consoles:

### Google Play Console (Policy and programs → App content)

1. **Ads Declaration**:
   - Navigate to **App content → Ads**.
   - Select **"Yes, my app contains ads"** and save.

2. **Advertising ID Declaration**:
   - Navigate to **App content → Advertising ID**.
   - Select **"Yes"** (declaring your app uses the advertising ID for advertising/marketing and analytics).

3. **Data Safety Questionnaire**:
   - Navigate to **App content → Data safety**.
   - **Step 2 (Data collection and security)**:
     - *Does your app collect or share user data?* → Select **Yes**.
     - *Is all user data encrypted in transit?* → Select **Yes** (all AdMob/Firebase traffic uses HTTPS/TLS).
     - *Methods of account creation?* → Select your app's method (e.g. OAuth, or "My app does not allow users to create an account").
   - **Step 3 (Data types)**:
     - Scroll to **Device or other IDs**, expand the dropdown, and check **Device or other IDs** (AdMob collects the Advertising ID under this category).
   - **Step 4 (Data usage and handling)**:
     - Click **Device or other IDs** to configure:
       - *Collected or shared?* → Check both **Collected** and **Shared** (AdMob transfers identifiers to Google/ad partners).
       - *Processed ephemerally?* → Select **No**.
       - *Required or optional?* → Select **Data collection is required**.
       - *Purposes?* → Check **Advertising or marketing** (and **Analytics** if using Firebase Analytics).
   - **Step 5 (Preview)** → Review and **Save**.

### App Store Connect (App Privacy)

- Declare **Device ID** under Identifiers:
  - Used for: **Third-Party Advertising** and **Analytics**.
  - Linked to user / Used for tracking: Yes (if displaying personalized ads; prompt via **App Tracking Transparency** on iOS).

Skipping these declarations can cause app rejection or store policy warnings.

## Validation

- `./gradlew :androidApp:assembleDebug` builds; run the app and confirm a **test** banner/interstitial/
  rewarded ad renders.
- Run the `run-quality-gates` skill before committing.

## Related skills

`enable-credits` (rewarded ads → credits) · `setup-subscriptions` · `monetization` (phase guide).
