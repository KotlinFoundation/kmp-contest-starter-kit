---
name: add-virality-loop
description: Add growth loops beyond the onboarding paywall — share/referral surfaces, high-intent conversion prompts, win-back re-engagement, and in-app review prompts. Use when the developer wants virality, referral, sharing, re-engagement / win-back, or to ask users for an app-store rating.
---

# Add a virality loop

Growth surfaces *beyond* the first onboarding paywall — high-intent moments and shareable value that
create organic acquisition. Keep it honest and genuinely useful, never nagging. This is a
designer-handoff skill.

## 1. Fill the spec

Open `AiGuidelines/project/virality_loops.md` and fill its `TAILOR PER APP` markers:
- **Share / referral loop** — the shareable output the user is proud of (one-tap share), and the
  referral incentive if any (e.g. "both sides get bonus credits").
- **High-intent prompts** — a tailored conversion prompt at moments where intent is genuinely high
  (feature/usage limit hit, post-value win, streak/achievement) — not on a timer.
- **Win-back** — re-engage lapsed or declined users gracefully; tie messages to the captured goal
  (re-engagement pushes ride on the `enable-notifications` skill).

Build the surfaces as screens/components via the `new-screen` skill; instrument them with
`Analytics.logScreenView` / custom events (see `setup-analytics`).

## 2. In-app review prompts

In-app review uses the **native OS APIs** (Play In-App Review / StoreKit) — **no console setup, no
Firebase**. Reference `Documentation/docs/features/inapp-review.md`.

Trigger from a genuine post-value moment via `rememberInAppReviewTrigger()`
(`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/util/inappreview/InAppReviewManager.kt`):
```kotlin
val reviewTrigger = rememberInAppReviewTrigger()
LaunchedEffect(Unit) {
    reviewTrigger.triggerAfterSuccessfulPurchase()   // or triggerWhileGenerationIsInProgress()
}
```
- `InAppReviewTrigger` applies a **7-day cooldown** (`COOLDOWN_DURATION`) plus usage conditions — add
  your own `trigger*` method for your primary action rather than spamming.
- Respect the store quotas (Android/iOS limit how often the prompt shows); the OS may silently no-op.
- For the raw prompt without cooldown, use `rememberInAppReviewManager().requestReview()`.

## Done
A share/referral surface works and the review prompt fires at a real post-value moment. This is the
`add-virality-loop` step of the `growth` phase.
