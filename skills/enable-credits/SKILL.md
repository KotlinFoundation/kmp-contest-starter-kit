---
name: enable-credits
description: >-
  Configure KMPStarterKit's local credit system — the earn/spend DSL (one-time bonuses, recurring
  daily/weekly/monthly rewards, credit-source ordering) in root/Di.kt, and wire credit-pack IAPs
  (consumables) to credit grants. Use when the user wants credits, tokens, a credit balance,
  credit packs, or a consumable in-app purchase that adds credits. Part of the `monetization` phase.
---

# Enable credits

KMPStarterKit has a fully local, configurable credit system: users **earn, buy, and spend** credits,
balances + transactions are stored on-device (mix of `UserPreferences` + Room), and behavior is
defined with a small DSL. A `CreditBalanceScreen` and a Home-toolbar balance badge already exist.
This skill is about configuring the rules and wiring credit-pack purchases. Everything is manual.

## 1. Configure the credit DSL

The config lives in **`root/Di.kt`**, in the `initializeCreditSystem()` method, as a
`creditSystemConfig { ... }` block passed to `CreditRepository`:

```kotlin
val appCreditSystemConfig = creditSystemConfig {
    // One-time bonus for new users
    oneTimeBonus("welcome_bonus_credit", 1)

    // Optional: conditional one-time bonus
    // oneTimeBonus(
    //     id = "referral_bonus",
    //     amount = 1,
    //     condition = { userPreferences.getBoolean(UserPreferences.KEY_REFERRAL_COMPLETED) },
    // )

    // Free plan → 2 credits weekly
    // recurringWeekly(
    //     id = "free_plan_weekly",
    //     amount = 2,
    //     condition = { !subscriptionRepository.hasPremiumAccess() },
    // )

    // Premium → 10 credits weekly
    recurringWeekly(
        id = "premium_plan_weekly",
        amount = 10,
        condition = { subscriptionRepository.hasPremiumAccess() },
    )
}
```

Available rule builders:

- `oneTimeBonus(id, amount, condition?)` — granted once (optionally gated by a condition).
- `recurringDaily(id, amount, condition?)` / `recurringWeekly(...)` / `recurringMonthly(...)` —
  time-based grants; `condition` typically checks `subscriptionRepository.hasPremiumAccess()` or the
  subscription duration type.

Each rule declares its own **credit source**; the order you list rules defines the **deduction
ordering** (which source is spent first). Give every rule a stable unique `id` — it keys the
per-source balance and the once-only accounting.

## 2. Spend and grant credits in code

```kotlin
// Deduct (default 1). Throws CreditRequiredException if balance is insufficient.
creditRepository.useCredits()                        // 1 credit
creditRepository.useCredits(3)                        // 3 credits
creditRepository.useCredits(1, "AI Room Generation")  // with description

// Add credits (from a purchase, a rewarded ad, a promo code)
creditRepository.addCredits(
    amount = 10,
    type = CreditTransaction.Type.PURCHASE,
    description = "10-credit pack",
)
```

Gate a premium feature by catching `CreditRequiredException` and routing to the credit-pack paywall.

## 3. Credit-pack IAPs (consumables)

Credit packs are sold through the **same subscription provider** as `setup-subscriptions`, as a
separate paywall placement:

- The provider/entitlement plumbing is shared — see the **`setup-subscriptions`** skill first.
- The credit-pack placement id is `Constants.PAYWALL_PLACEMENT_CREDITS_PACK` (`"credits_pack"`). Open
  it with `navigator.navigate(PaywallScreenRoute(placementId = Constants.PAYWALL_PLACEMENT_CREDITS_PACK))`;
  `PaywallViewModel` derives `PaywallMode.CREDIT_PACK` and renders `CreditPackPaywallScreen`.
- **Store side (User Action):** create the credit packs as **consumable** in-app products (NOT
  subscriptions) in **App Store Connect** and **Google Play Console**, with aligned cross-platform IDs.
  Register them under the credit-pack placement in the provider dashboard
  ([Adapty](https://app.adapty.io/) / [RevenueCat](https://app.revenuecat.com/)).
- **Grant on success:** on a successful credit-pack purchase, call `creditRepository.addCredits(amount,
  CreditTransaction.Type.PURCHASE, ...)` with the pack's credit count. The paywall success handling in
  `PaywallViewModel` is where the pack → credit-count mapping is applied.

## Storage note

Credits are **local-only** by default (on-device). That's fine for most early-stage apps. Only add
backend/server-verified credit sync (e.g. Firestore) if you see abuse, need cross-device sync, or need
server-verified purchases — it adds real cost and complexity.

When that time comes, use the **`sync-data-firebase`** skill; it has a worked credit-balance example.
Don't design the sync ad hoc: no Firebase client SDK supports the `wasmJs` target, so the approach is a
trade-off against the web build that the developer has to decide first.

## Validation

- App builds (`./gradlew :androidApp:assembleDebug`) and the `CreditBalanceScreen` / toolbar badge
  reflect configured bonuses.
- The credit-pack paywall lists your packs; a sandbox purchase calls `addCredits(...)` and the balance
  increases.
- Run the `run-quality-gates` skill before committing.

## Related skills

`setup-subscriptions` (provider + placement plumbing) · `design-paywall` (credit-pack offer copy) ·
`enable-ads` (rewarded ads can grant credits) · `monetization` (phase guide).
