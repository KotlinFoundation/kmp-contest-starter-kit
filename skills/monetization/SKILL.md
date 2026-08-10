---
name: monetization
description: >-
  PHASE 4 GUIDE — turn the published KMPStarterKit app into a revenue app: subscriptions +
  credit-pack IAPs (Adapty default, or RevenueCat), a wired paywall, and AdMob ads. Orchestrates
  the atomic skills design-paywall, setup-subscriptions, enable-credits, and enable-ads.
  Use when the developer wants to monetize / add monetization / get to revenue / set up
  subscriptions, in-app purchases, paywall, credits, or ads on the KMP contest starter kit
  (aliases: monetization / monetize the KMP contest starter kit).
---

# Phase 4 — Monetization (get to revenue)

This is the **blueprint orchestrator** for turning a published app into a revenue app. It composes
four atomic skills into one ordered flow. Work the checklist top-to-bottom; do not skip ahead.

## Goal

A shipping app that can take money: subscriptions **and** credit-pack in-app purchases (through
**Adapty** — the default — or **RevenueCat**), a paywall wired to a real offer, and AdMob ads
enabled where they fit. The exit gate is a **completed sandbox/test purchase that unlocks premium**.

> **First step of this phase: turn premium on.** `AppConfiguration.PREMIUM_FEATURES_ENABLED`
> defaults to `false` — no paywall, no subscriptions/upgrade UI, credits off / generation free — so
> the app runs with zero monetization config until you get here. Set it to `true` to bring the
> monetization surface back; `check_env --phase publishing` then expects subscription keys.
> **No premium features? Skip this phase** and leave the flag `false`. (Ads are independent — see
> `enable-ads`.)

## Prerequisite

Store presence must already exist — the **App Store Connect** app record and the **Google Play
Console** app must be created (that's the `publishing` phase). Real subscription/IAP products are
created **inside those store consoles**, so you cannot finish this phase without them. If the stores
don't exist yet, send the developer back to `publishing` first.

## STOP rule

When the next unchecked item is a **User Action**, stop and wait for the developer to confirm
they've done it before continuing. Never fabricate product IDs, entitlements, or dashboard state.

## Progress tracking

Copy `skills/monetization/progress-template.md` into the working area at the start and tick items as
you go. Each line is labelled with who owns it (Agent / User).

## Keys / secrets you'll need

- **Subscription SDK keys** (required) — `SUBSCRIPTION_PROVIDER_ANDROID_API_KEY` / `_IOS_API_KEY`
  from the Adapty (default) or RevenueCat dashboard.
- **AdMob ids** (opt-in) — only if you enable ads (`FeatureFlagManager` `IS_ADS_ENABLED`).

Verify: `./scripts/check_env.sh --phase monetization`.

> Until the subscription keys are set, the paywall runs a built-in **mock** provider (demo packages,
> simulated purchases, a "Demo paywall" banner) so you can build/test the flow before wiring a billing
> account. It auto-switches to the real provider once you add a key. See `setup-subscriptions`.

## Ordered checklist

### 1. Complete the offer — the numbers

The **primary model + benefit copy** were already decided in Phase 1 (`new-app` / `build-features`), and
the paywall has been running on the **mock provider** since then. What's missing are the numbers, which
shape the store products — so settle them before creating any product.

- [ ] **Agent Action** — Run the **`design-paywall`** skill and open `AiGuidelines/project/paywall.md`.
      The primary model should already be filled; the remaining blanks are marked `TODO(monetization)`.
- [ ] **User Action** — Fill the remaining `TAILOR PER APP` blanks: **prices & packages**
      (monthly/annual/lifetime), **trial length**, **credit-pack sizes**.
- [ ] **Validation** — `AiGuidelines/project/paywall.md` has no remaining `TAILOR PER APP` /
      `TODO(monetization)` markers.

### 2. Set up subscriptions

- [ ] **Agent Action** — Run the **`setup-subscriptions`** skill: confirm/select the provider via
      `SUBSCRIPTION_PROVIDER` in `MobileApp/gradle.properties`, and confirm the paywall entitlement
      key (`Constants.PAYWALL_PREMIUM_ACCESS`, default `"Premium"`).
- [ ] **User Action** — Put the provider's Android + iOS SDK keys in `MobileApp/local.properties`
      (`SUBSCRIPTION_PROVIDER_ANDROID_API_KEY`, `SUBSCRIPTION_PROVIDER_IOS_API_KEY`).
- [ ] **User Action** — Create the subscription products in **App Store Connect** and **Google Play
      Console** with cross-platform-aligned product IDs (the prices from step 1).
- [ ] **User Action** — In the **provider dashboard** (Adapty or RevenueCat), link those store
      products, map them to the `Premium` entitlement / access level, and configure the paywall
      placement(s).
- [ ] **Validation** — `./gradlew :androidApp:assembleDebug` builds; the subscription paywall shows
      real products fetched from the provider.

### 3. Enable credit packs

- [ ] **Agent Action** — Run the **`enable-credits`** skill: configure the credit DSL in
      `root/Di.kt` (`initializeCreditSystem`) and wire credit-pack purchases to credit grants.
- [ ] **User Action** — Create the credit-pack products as **consumable** IAPs in ASC + Play, and
      register them under the credit-pack placement in the provider dashboard.
- [ ] **Validation** — The credit-pack paywall (`PaywallMode.CREDIT_PACK`) shows the packs and a
      successful purchase calls `creditRepository.addCredits(...)`.

### 4. Enable ads

- [ ] **Agent Action** — Run the **`enable-ads`** skill: flip `IS_ADS_ENABLED` and place
      banner/interstitial/rewarded ads where they fit the UX.
- [ ] **User Action** — Create the AdMob app + ad units and paste the IDs into
      `MobileApp/local.properties` (+ iOS app id into `BaseConfig.xcconfig`).
- [ ] **User Action** — Update the store **data-safety / privacy** declarations for advertising IDs.
- [ ] **Validation** — Test ads render in a debug build (Google test ad IDs during development).

## Exit gate

**A sandbox / test purchase completes and unlocks premium** — the paywall dismisses (subscription)
or credits are added (credit pack). Validate this on at least one platform before declaring the
phase done.

## Next phase

Once revenue plumbing works end-to-end, **trigger the next phase explicitly** — tell your agent
**"start the growth phase"** (or run the `growth` skill) for analytics, retention, and virality
(see `AiGuidelines/project/virality_loops.md`).
