---
name: design-paywall
description: >-
  Author the app's paywall offer — primary model, prices, trial framing, credit-pack sizes, and
  benefit-oriented copy — using the paywall_designer role prompt and the paywall.md template, then map
  the decisions onto PaywallUiState / PaywallMode and the paywall_* strings. Use when the user wants to
  design a paywall, define the offer/pricing/trial, or write paywall copy. Part of the `monetization`
  phase; run it before `setup-subscriptions`.
---

# Design the paywall

This is a **designer-handoff** skill, not a code generator. It produces the *offer decisions* that
`setup-subscriptions` and `enable-credits` then create products for — so run it **first**. The custom
paywall UI already exists; you're authoring what it sells and how it reads.

## 1. Author the offer

Read the two source docs and use them to make real decisions (honest framing only — no fake scarcity,
no buried cancel, no misleading "free"):

- **`AiGuidelines/agents/paywall_designer.md`** — the monetization-strategist role prompt: offer
  architecture (hook → anchor → discount → backup), trial-length tradeoffs, credit-pack anchoring, the
  A/B variation toolkit, and the events to measure.
- **`AiGuidelines/project/paywall.md`** — the fill-in template. Search for **`TAILOR PER APP`** markers
  and replace each with a real decision:
  - **Primary model** — subscription-first, credit-pack, or remote? (Don't weight two equally if one
    leads.)
  - **Prices & packages** — monthly $ / annual $ (save %) / lifetime $.
  - **Trial length + reasoning** — short (3-day, urgency) vs. longer (higher considered-purchase
    conversion). State terms honestly: "X days free, then $Y/period. Cancel anytime."
  - **Credit-pack sizes + the genuinely recommended pack.**

Also reuse the goal captured in onboarding (`AiGuidelines/project/onboarding.md`) so the paywall reads
"To help you *{goal}*, your plan includes…". Placement is the **post-onboarding motivation peak**, not
a cold launch wall.

## 2. Map decisions onto the code

The offer flows into the existing paywall layer at
`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/presentation/screens/paywall/`:

- **`PaywallMode`** (`SUBSCRIPTION` / `CREDIT_PACK`) selects the child screen. Your primary model picks
  which the paywall leads with; the credit-pack flow opens via
  `Constants.PAYWALL_PLACEMENT_CREDITS_PACK`.
- **`PaywallUiStateMapper`** is the only place formatting lives — it turns raw provider packages +
  selection + mode into a `MappedPaywall { packages, ctaText, aboveCtaText, belowCtaText }`. Screens
  stay display-only. You usually don't change the mapper; you change the **copy** it references.
- **Strings** live in `composeResources/values/strings.xml` under three prefixes — edit these to land
  your copy (benefit-oriented CTA, honest reassurance + disclosure):
  - `paywall_*` — shared chrome (toolbar, footer, BEST VALUE / SAVE N% badges).
  - `paywall_sub_*` — subscription flow (plan titles, subtitles, reassurance/disclosure templates, CTA).
  - `paywall_cp_*` — credit-pack flow (title, subtitle, per-credit unit, CTA).
  - `paywall_unit_*` — period units as **plurals** (`paywall_unit_day` bare vs. `paywall_unit_day_count`
    for durations).
  - `strings.xml` here is plain XML, not Android aapt — do **not** backslash-escape apostrophes; use the
    typographic `'` (U+2019) to match existing copy. Format args stay `%1$s` / `%1$d`.
- To add a **brand-new placement** (beyond subscription / credit-pack), see `AGENTS.md` → *Paywall Layer*:
  add a `Constants.PAYWALL_PLACEMENT_*`, a `PaywallMode` entry, mapper branches, a `PaywallScreen`
  route, and a new `paywall_<prefix>_*` string group.

## Preview your copy

Fixtures in `PaywallPreviewData` (`subscriptionState(...)`, `paidIntroSubscriptionState()`,
`creditPackState()`) drive the `@Preview` composables so you can eyeball copy without billing wired up.
Run the design-system desktop preview or use the `capture-app-screens` skill for pixel captures.

## Hand-off

Once `paywall.md` has **no** remaining `TAILOR PER APP` markers, the offer is decided — proceed to
**`setup-subscriptions`** (subscriptions) and **`enable-credits`** (credit packs) to create the matching
products.

## When to run this — split across two phases

The kit ships a working paywall, so it shows boilerplate until you brand it:

- **Phase 1 (`build-features`)** — the **primary model + benefit copy** (`paywall_*` strings). Fully
  testable now: the **mock provider** runs paywall → purchase → unlock with **no account, no keys**.
- **Phase 4 (`monetization`)** — the **numbers**: prices & packages, trial length, credit-pack sizes.
  Set them when you create the real store products (they're marked `TODO(monetization)` in
  `AiGuidelines/project/paywall.md` until then).

## Related skills

`verify-ui` (render the paywall to a PNG and look at it — also how to answer "show me the paywall") ·
`setup-subscriptions` · `enable-credits` · `capture-app-screens` (paywall PNGs for the storefront) ·
`monetization` (phase guide).
