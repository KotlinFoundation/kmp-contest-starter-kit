---
name: configure-environment
description: Reference catalog (a lookup, not a setup action) of every environment configuration value — local.properties API keys, the gradle.properties subscription toggle, and AppConfiguration.kt / Constants.kt fields — with what each is for, where to get it, and which phase needs it. Use when you don't know where a config value or API key lives; then run the matching setup skill (setup-firebase, enable-auth, setup-subscriptions, enable-ads, setup-analytics, integrate-web-proxy) to actually wire it.
---

# Configure environment

Three files hold all app configuration. Nothing here is a code change to business logic — it's
credentials, IDs, and toggles.

| File | Holds | Committed? |
|------|-------|-----------|
| `MobileApp/local.properties` | SDK path + all secret API keys | **No — gitignored** |
| `MobileApp/gradle.properties` | subscription-provider toggle | Yes |
| `shared/src/commonMain/.../root/AppConfiguration.kt` | per-app config: URLs, contact email, AI routing (`CLOUD_FUNCTIONS_URL`, `USE_AI_PROXY_SERVER`), `AUTH_SOCIAL_LOGIN_ENABLED`, provider factories | Yes |
| `shared/src/commonMain/.../util/Constants.kt` | framework constants: paywall entitlement/placement ids, DB/prefs file names | Yes |

> `MobileApp/local.properties` is gitignored (`MobileApp/.gitignore`), so it never ships filled — copy
> it from the committed **`MobileApp/local.properties.example`** and fill what your phase needs. Keys
> are read at build time by `shared/build.gradle.kts` via `getRequiredProperty(...)`; missing keys with
> no default fail the build with `Make sure you added \`<KEY>\` in local.properties`. Most keys have a
> `defaultValue` ("testValue" or "") so the app still builds without them, but the corresponding
> feature won't work — which means a missing key is invisible in the build.
>
> **Make it visible:** run `./scripts/check_env.sh --phase <phase>` from `MobileApp/` to list which
> keys required by that phase are still placeholders (✅ / ⚠️ / ⚪). It's non-breaking and is wired into
> `run-quality-gates` and every phase's validation gate.

## `MobileApp/local.properties` — key catalog

| Key | For | Where to get it | Needed in phase |
|-----|-----|-----------------|-----------------|
| `sdk.dir` | Android SDK location | Your machine's SDK path (`/Users/<you>/Library/Android/sdk`) | getting-started |
| `GOOGLE_WEB_CLIENT_ID` | Google sign-in (Android + iOS) | Firebase → Auth → Google provider → **Web client ID** | integrations (`enable-auth`) |
| `FIREBASE_API_KEY` / `FIREBASE_PROJECT_ID` / `FIREBASE_APPLICATION_ID` | Firebase auth on **Desktop/Web only** (no native SDK there → KMPAuth runs on a REST engine that needs this config). **Leave empty for mobile.** | Firebase → Project settings → your **Web app** → SDK config | integrations (`setup-firebase`) |
| `SUBSCRIPTION_PROVIDER_ANDROID_API_KEY` | Billing SDK public key (Android) | Adapty or RevenueCat dashboard | integrations (opt-in) / monetization |
| `SUBSCRIPTION_PROVIDER_IOS_API_KEY` | Billing SDK public key (iOS) | Adapty or RevenueCat dashboard | integrations (opt-in) / monetization |
| `ADMOB_APP_ID_ANDROID` | AdMob app id (Android) | Google AdMob console | monetization (ads) |
| `ADMOB_BANNER_AD_ID_ANDROID` | Banner ad unit (Android) | AdMob console | monetization |
| `ADMOB_INTERSTITIAL_AD_ID_ANDROID` | Interstitial ad unit (Android) | AdMob console | monetization |
| `ADMOB_REWARDED_AD_ID_ANDROID` | Rewarded ad unit (Android) | AdMob console | monetization |
| `ADMOB_BANNER_AD_ID_IOS` | Banner ad unit (iOS) | AdMob console | monetization |
| `ADMOB_INTERSTITIAL_AD_ID_IOS` | Interstitial ad unit (iOS) | AdMob console | monetization |
| `ADMOB_REWARDED_AD_ID_IOS` | Rewarded ad unit (iOS) | AdMob console | monetization |
| `OPENAI_API_KEY` | Direct OpenAI calls (no proxy, no Firebase) | https://platform.openai.com/ | **phase 1** if the app uses AI (direct) · proxied in integrations — see note |
| `REPLICATE_API_KEY` | Direct Replicate calls (no proxy, no Firebase) | https://replicate.com/account/api-tokens | **phase 1** if the app uses AI (direct) · proxied in integrations — see note |

> **Subscription mock:** while the two subscription keys are unset, the app runs a built-in mock
> provider so the paywall/purchase/unlock/cancel flow works with zero keys (a red "Demo paywall" banner
> marks it). Setting a real key auto-switches to the real provider — see `setup-subscriptions`.

> **Direct AI mode (prototyping):** `OPENAI_API_KEY` / `REPLICATE_API_KEY` in `local.properties` are used
> only for the **direct** (no-Firebase) AI path — the app calls the provider straight from the device
> whenever `AppConfiguration.CLOUD_FUNCTIONS_URL` is blank (`AppConfiguration.USE_AI_PROXY_SERVER` overrides). The key
> ships in the app binary, so this is prototyping only. In **production** the app calls the web-proxy and
> the keys live in **Google Cloud Secret Manager**, not on device — see `integrate-web-proxy`.

> The AdMob and `ADMOB_APP_ID_ANDROID` value is also consumed in `androidApp/build.gradle.kts`
> (manifest placeholder).

Full template with placeholders, where-to-get URLs, and per-phase comments lives in the committed
**`MobileApp/local.properties.example`** — copy it to `local.properties` and fill what you need:

```bash
cp MobileApp/local.properties.example MobileApp/local.properties   # then edit
```

For the easy first-run path you only need `sdk.dir`. Everything else is filled as you reach the phase
that needs it (and `check_env.sh` tells you when).

## `MobileApp/gradle.properties` — subscription provider toggle

One switch selects the billing backend module and drives `AppConfiguration.subscriptionProviderFactory`.
Default is `ADAPTY`; the alternative is `REVENUECAT`. Both must compile.

```properties
# Possible options for SUBSCRIPTION_PROVIDER: ADAPTY, REVENUECAT
SUBSCRIPTION_PROVIDER=ADAPTY
```

Switching providers = change this property **plus** the provider's API keys in `local.properties`.
Never hardcode a concrete provider in `AppConfiguration.kt`.

## `AppConfiguration.kt` — per-app code-level config

`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/root/AppConfiguration.kt` — the toggles/values a
developer sets per app:

| Field | Purpose |
|-------|---------|
| `PREMIUM_FEATURES_ENABLED` | Does the app have premium (paid/gated) features? `false` (default) = **no premium features**, everything free: no paywall/subscriptions/upgrade UI, billing never inits, credits off (generation free). `true` = yes — subscriptions + paywall + credits available to gate/limit features (app can still be free to download); set in the monetization phase. |
| `CLOUD_FUNCTIONS_URL` | Base URL of the deployed web proxy — `https://REGION-PROJECT_ID.cloudfunctions.net` (default region `us-central1`). Set in the `integrate-web-proxy` skill. |
| `USE_AI_PROXY_SERVER` | `null` (auto) / `true` (force proxy) / `false` (force direct on-device AI). See `integrate-web-proxy`. |
| `AUTH_SOCIAL_LOGIN_ENABLED` | `false` (default) = anonymous auth only, the easy path; `true` = also show Apple + Google sign-in (needs `GOOGLE_WEB_CLIENT_ID` + iOS config — see `enable-auth`). |
| `URL_PRIVACY_POLICY` / `URL_TERMS_CONDITIONS` | Legal URLs (needed before store publishing). |
| `CONTACT_EMAIL` | Support/contact email shown in-app. |
| `APPSTORE_APP_ID` | Numeric App Store app id (for rate/review deep links). |
| `subscriptionProviderFactory` / `authServiceProviderFactory` | Provider selectors (resolve the gradle-chosen backend). |

## `Constants.kt` — framework constants

`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/Constants.kt` (unlikely to change per app):
`PAYWALL_PREMIUM_ACCESS` (entitlement id), `PAYWALL_PLACEMENT_*` (placement ids), `CREDIT_PACK_PRODUCT_ID_PREFIX`,
DB/prefs file names.

Runtime **feature flags** live separately in
`shared/src/commonMain/.../data/source/featureflag/FeatureFlagManager.kt`.

## Next

With the catalog understood, continue the `integrations` guide: `setup-firebase` →
`enable-auth` → `integrate-web-proxy`.
