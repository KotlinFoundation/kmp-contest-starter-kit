---
name: setup-subscriptions
description: >-
  Wire up in-app subscriptions in KMPStarterKit — pick the provider (Adapty default or RevenueCat)
  via one gradle property, add the two SDK keys, create store products with aligned IDs, and map
  them to the Premium entitlement + paywall placement in the provider dashboard. Use when the user
  wants to add subscriptions / recurring in-app purchases / a premium tier, or configure Adapty or
  RevenueCat. Part of the `monetization` phase.
---

# Set up subscriptions

KMPStarterKit ships two interchangeable billing backends behind the `subscription-api` contracts:
**Adapty (default)** and **RevenueCat**. Exactly one is linked per build. Everything below is manual —
provider dashboards + store consoles + a few config edits. All Gradle commands run from `MobileApp/`.

## 1. Pick the provider (one switch)

Set `SUBSCRIPTION_PROVIDER` in `MobileApp/gradle.properties` — `ADAPTY` (default) or `REVENUECAT`:

```properties
# Possible options for SUBSCRIPTION_PROVIDER: ADAPTY, REVENUECAT
SUBSCRIPTION_PROVIDER=ADAPTY
```

That property does two things: (a) `shared/build.gradle.kts` puts only the matching provider module
on the classpath (`libs/subscription/subscription-adapty` or `subscription-revenuecat`), and (b)
`AppConfiguration.subscriptionProviderFactory` delegates to `activeSubscriptionProviderFactory` — the single
symbol each provider module exposes in package `com.kotlinfoundation.koko.subscription.config`.

- **Never hardcode a provider in `AppConfiguration`.** `AppConfiguration.subscriptionProviderFactory` must
  stay provider-agnostic; the gradle property resolves it so build + app code can't drift.
- **Both `ADAPTY` and `REVENUECAT` builds must compile.** If you touch subscription code, flip the
  property to the other value and rebuild before declaring done.

## Mock mode — default until keys are set

Until real SDK keys are configured, the app runs a built-in **mock** subscription provider
(`MockSubscriptionProvider`, auto-selected by `isSubscriptionMockActive()` in `root/Di.kt`). The paywall
shows demo packages and a "purchase" simulates success — unlocking Premium or adding credits — so the
whole flow is explorable on every platform with zero keys. A red **"Demo paywall" banner** marks it, buys
skip the sign-in requirement, and you can **cancel** the simulated subscription from the Subscriptions
screen's "manage here" link. It **auto-switches to the real provider** the moment you set a key below.
It's client-only (no receipt), for exploration; do the steps below before shipping.

## 2. SDK API keys → `local.properties` (User Action)

Ask the developer to log into the provider dashboard and copy the two **public SDK keys**, then paste
them into `MobileApp/local.properties` (not committed). Same property names for both providers:

```properties
SUBSCRIPTION_PROVIDER_ANDROID_API_KEY=<provider Android SDK key>
SUBSCRIPTION_PROVIDER_IOS_API_KEY=<provider iOS SDK key>
```

- Adapty keys: [app.adapty.io](https://app.adapty.io/) → App settings → API keys (public SDK keys).
- RevenueCat keys: [app.revenuecat.com](https://app.revenuecat.com/) → Project → API keys (public
  Android / iOS app-specific keys).

> **STOP — these keys can't be generated locally.** Without them the paywall silently falls back to
> `"testValue"` and fetches no products (the build still passes). Wait for the developer to paste both
> keys and confirm. Verify with `./scripts/check_env.sh --phase monetization`.

## 3. Create products in the stores (User Action)

Products are created in the store consoles — **the store app records must already exist** (from the
`publishing` phase).

- **App Store Connect** → your app → **Features → In-App Purchases / Subscriptions**. iOS also needs
  the **In-App Purchase** capability enabled on the target in Xcode (Signing & Capabilities → +).
- **Google Play Console** → your app → **Monetize → Products → Subscriptions**.

**Align product IDs across platforms** (e.g. `premium_monthly`, `premium_annual`) so the provider can
treat them as one offering. Use the prices you locked in `design-paywall` /
`AiGuidelines/project/paywall.md`. Enable **purchasing-power parity (PPP) / regional pricing** in each
store so prices adjust per region — the `PaywallUiStateMapper` renders whatever localized price the
provider returns.

## 4. Map to entitlements + placements (User Action)

In the provider dashboard (Adapty or RevenueCat):

1. Link the store products you just created.
2. Map them to an **entitlement / access level** whose key matches
   `Constants.PAYWALL_PREMIUM_ACCESS` (default `"Premium"`). To use a different key or multiple tiers,
   edit `PAYWALL_PREMIUM_ACCESS` in
   `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/Constants.kt`; the repo checks
   access via `subscriptionRepository.hasPremiumAccess()` (and `hasEntitlementAccess(key)` for
   multi-tier).
3. Configure the **paywall placement(s)** the app requests. The custom subscription paywall uses the
   default placement.

## Custom vs. remote paywall

By default the app renders its **own** custom paywall (`SHOW_REMOTE_PAYWALL = false` in
`data/source/featureflag/FeatureFlagManager.kt`) — full design control, purchase + restore handled per
store guidelines. Flip `SHOW_REMOTE_PAYWALL` to `true` (or via Firebase Remote Config, key
`show_remote_paywall`) to render the provider's remotely-managed paywall instead. Author the custom
paywall's offer/copy via the **`design-paywall`** skill.

## Validation

- `./gradlew :androidApp:assembleDebug` builds.
- Run the app, open the paywall — real products fetched from the provider appear.
- A **sandbox** purchase (App Store sandbox tester / Play internal-test / license tester) completes and
  `hasPremiumAccess()` becomes true (paywall dismisses).
- Run the `run-quality-gates` skill before committing.

## Related skills

`design-paywall` (author the offer first) · `enable-credits` (credit-pack IAPs) ·
`enable-ads` · `monetization` (the phase guide that orders these).
