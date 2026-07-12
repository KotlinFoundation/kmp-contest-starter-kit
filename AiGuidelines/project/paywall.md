# Paywall

This app's monetization strategy. The paywall converts the motivation built in onboarding into a
trial/purchase — **by honest means only** (no fake scarcity, no buried cancel, no misleading "free").
Fill the `TAILOR PER APP` blanks; the rest are strong defaults.

> Code lives in
> `MobileApp/shared/src/commonMain/kotlin/com/kotlinfoundation/koko/presentation/screens/paywall/`
> — `SubscriptionPaywallScreen.kt`, `creditpack/CreditPackPaywallScreen.kt`,
> `remotepaywall/RemotePaywallScreen.kt`, and `PaywallUiStateMapper`.

## Primary model

KMPStarterKit ships **subscription** + **credit-pack** + a remote (Adapty/RevenueCat) paywall. Decide
which the first paywall leads with — don't weight both equally if one is primary.

**`TAILOR PER APP` — primary model + why:** _e.g. "Subscription-first; credit packs offered to users
who decline the trial."_

## Placement

Show the primary paywall at the **post-onboarding motivation peak** (after value is demonstrated),
not as a cold launch wall. Reuse the goal captured in onboarding ("To help you *{goal}*, your plan
includes…").

## Offer architecture

- **Hook** — risk-free framing (free trial).
- **Anchor** — the monthly price.
- **Discount** — annual presented against the monthly anchor, savings made explicit.
- **Backup** — a smaller fallback offer for users who decline, where appropriate.

**`TAILOR PER APP` — prices & packages:** _monthly $__ / annual $__ (save __%) / lifetime $__ ._

## Trial framing

A real tradeoff: short trials (3-day) create urgency for impulse/utility apps; longer trials
(~17–32 days) tend to convert higher for considered purchases. State terms honestly: "X days free,
then $Y/period. Cancel anytime."

**`TAILOR PER APP` — trial length + reasoning:** _…_

## Credit packs

Clear per-unit value; **honest** anchoring (highlight the genuine best-value pack, never a fake one);
correct PPP (purchasing-power) pricing per region via `PaywallUiStateMapper`.

**`TAILOR PER APP` — pack sizes & the recommended pack:** _…_

## CTA & trust

One primary action with a benefit-oriented label (never a bare "Subscribe"). Renewal/cancel terms
visible and truthful; restore accessible. See `AiGuidelines/agents/paywall_designer.md` for the
variation copy brief.

## Measure

Instrument: paywall impressions, trial starts, trial→paid, restore, cancel. Also consider
multi-surface prompts at genuine high-intent moments (see `virality_loops.md`).

> Paywall architecture, the offer/trial science, and the reviewer's rubric live in
> `AiGuidelines/loop/CONVERSION_PLAYBOOK.md` when the self-improve loop is installed.
