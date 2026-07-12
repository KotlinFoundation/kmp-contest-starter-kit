---
name: configure-environment
description: Lay out the full catalog of environment configuration — local.properties API keys, the gradle.properties subscription toggle, and Constants.kt fields — with what each is for, where to get it, and which phase needs it. Use when setting up a new app's keys, wiring a service, or figuring out where a config value lives.
---

# Configure environment

Three files hold all app configuration. Nothing here is a code change to business logic — it's
credentials, IDs, and toggles.

| File | Holds | Committed? |
|------|-------|-----------|
| `MobileApp/local.properties` | SDK path + all secret API keys | **No — gitignored** |
| `MobileApp/gradle.properties` | subscription-provider toggle | Yes |
| `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/Constants.kt` | URLs, emails, feature flags, Cloud Functions URL | Yes |

> `MobileApp/local.properties` is gitignored (`MobileApp/.gitignore`), so it never ships in the
> template — you always create/fill it yourself. Keys are read at build time by
> `shared/build.gradle.kts` via `getRequiredProperty(...)`; missing keys with no default fail the
> build with `Make sure you added \`<KEY>\` in local.properties`. Most keys have a `defaultValue`
> ("testValue" or "") so the app still builds without them, but the corresponding feature won't work.

## `MobileApp/local.properties` — key catalog

| Key | For | Where to get it | Needed in phase |
|-----|-----|-----------------|-----------------|
| `sdk.dir` | Android SDK location | Your machine's SDK path (`/Users/<you>/Library/Android/sdk`) | getting-started |
| `GOOGLE_WEB_CLIENT_ID` | Google sign-in (Android + iOS) | Firebase → Auth → Google provider → **Web client ID** | integrations (`enable-auth`) |
| `SUBSCRIPTION_PROVIDER_ANDROID_API_KEY` | Billing SDK public key (Android) | Adapty or RevenueCat dashboard | publishing / monetization |
| `SUBSCRIPTION_PROVIDER_IOS_API_KEY` | Billing SDK public key (iOS) | Adapty or RevenueCat dashboard | publishing / monetization |
| `ADMOB_APP_ID_ANDROID` | AdMob app id (Android) | Google AdMob console | monetization (ads) |
| `ADMOB_BANNER_AD_ID_ANDROID` | Banner ad unit (Android) | AdMob console | monetization |
| `ADMOB_INTERSTITIAL_AD_ID_ANDROID` | Interstitial ad unit (Android) | AdMob console | monetization |
| `ADMOB_REWARDED_AD_ID_ANDROID` | Rewarded ad unit (Android) | AdMob console | monetization |
| `ADMOB_BANNER_AD_ID_IOS` | Banner ad unit (iOS) | AdMob console | monetization |
| `ADMOB_INTERSTITIAL_AD_ID_IOS` | Interstitial ad unit (iOS) | AdMob console | monetization |
| `ADMOB_REWARDED_AD_ID_IOS` | Rewarded ad unit (iOS) | AdMob console | monetization |
| `IMGBB_TOKEN` | Image hosting/upload token | https://api.imgbb.com/ account | when using image upload |
| `OPENAI_API_KEY` | Local/dev OpenAI calls | https://platform.openai.com/ | integrations (dev only — prod keys live in Secret Manager, see `integrate-web-proxy`) |

> The AdMob and `ADMOB_APP_ID_ANDROID` value is also consumed in `androidApp/build.gradle.kts`
> (manifest placeholder). Production Cloud Functions read `OPENAI_API_KEY` / `REPLICATE_API_KEY`
> from **Google Cloud Secret Manager**, not from `local.properties` — see the `integrate-web-proxy`
> skill.

Example `MobileApp/local.properties`:

```properties
sdk.dir=/Users/you/Library/Android/sdk
GOOGLE_WEB_CLIENT_ID=1234567890-abcdef.apps.googleusercontent.com
SUBSCRIPTION_PROVIDER_ANDROID_API_KEY=
SUBSCRIPTION_PROVIDER_IOS_API_KEY=
ADMOB_APP_ID_ANDROID=
IMGBB_TOKEN=
```

## `MobileApp/gradle.properties` — subscription provider toggle

One switch selects the billing backend module and drives `Constants.subscriptionProviderFactory`.
Default is `ADAPTY`; the alternative is `REVENUECAT`. Both must compile.

```properties
# Possible options for SUBSCRIPTION_PROVIDER: ADAPTY, REVENUECAT
SUBSCRIPTION_PROVIDER=ADAPTY
```

Switching providers = change this property **plus** the provider's API keys in `local.properties`.
Never hardcode a concrete provider in `Constants.kt`.

## `Constants.kt` — code-level config

`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/Constants.kt`:

| Field | Purpose |
|-------|---------|
| `CLOUD_FUNCTIONS_URL` | Base URL of the deployed web proxy — `https://REGION-PROJECT_ID.cloudfunctions.net` (default region `us-central1`). Set in the `integrate-web-proxy` skill. |
| `AUTH_SOCIAL_LOGIN_ENABLED` | `true` = show Apple + Google sign-in; `false` = anonymous only. |
| `URL_PRIVACY_POLICY` | Privacy policy URL (needed before store publishing). |
| `URL_TERMS_CONDITIONS` | Terms & conditions URL. |
| `CONTACT_EMAIL` | Support/contact email shown in-app. |
| `APPSTORE_APP_ID` | Numeric App Store app id (for rate/review deep links). |
| `PAYWALL_PREMIUM_ACCESS` | Entitlement / access-level id for Premium. |
| `PAYWALL_PLACEMENT_*` | Paywall placement identifiers (credits pack, default, onboarding). |

Runtime **feature flags** live separately in
`shared/src/commonMain/.../data/source/featureflag/FeatureFlagManager.kt`.

## Next

With the catalog understood, continue the `integrations` guide: `setup-firebase` →
`enable-auth` → `integrate-web-proxy`.
