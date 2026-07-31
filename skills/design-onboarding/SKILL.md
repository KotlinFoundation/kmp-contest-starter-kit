---
name: design-onboarding
description: Design and build the app's first-run onboarding — capture the user's goal, deliver a first taste of value, and prime permissions — grounded in the onboarding designer role and the project onboarding spec. Use when the developer wants to create, redesign, or polish onboarding / the first-run flow / activation.
---

# Design onboarding

Onboarding's job: **capture intent → build commitment → deliver first value → hand off to the
paywall** at the motivation peak. This is a designer-handoff skill — the strategy lives in the
guidelines, the screens are built with the `new-screen` skill.

## 1. Fill the product spec first

Open `AiGuidelines/project/onboarding.md` and fill its `TAILOR PER APP` markers for this app:
- **Pattern** — short emotional (3–4 screens) vs. questionnaire/quiz-led (9–20). Pick with a reason
  tied to the app, don't default to a length.
- **Goal-capture question** — the user's primary goal, asked early and echoed on the paywall (usually
  the biggest lever).
- **First-taste moment** — let the user *use the core mechanic once* (a real output) before any ask.
- **Permission priming** — a benefit-framed screen before every system permission; never open with a
  permission wall.

## 2. Author the flow with the designer role

Read `AiGuidelines/agents/onboarding_designer.md` and act in that role to produce the screen-by-screen
copy brief (headlines, subcopy, CTAs, step order). It reads the spec you just filled plus
`prd.md` / `user_flow.md`.

## 3. Build the screens

Existing onboarding lives at
`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/presentation/screens/onboarding/`
(`OnBoardingScreen.kt`, `OnBoardingScreenVariation1/2.kt`, `OnBoardingUiState.kt`,
`OnBoardingViewModel.kt`). Refine those or add new steps via the **`new-screen`** skill (Screen +
UiState + ViewModel, wired into navigation + DI).

- **Permission priming ties to `enable-notifications`.** The notification-permission priming step
  should precede `rememberNotificationPermissionState().request()` with a benefit-framed screen (e.g.
  "reminders for your goal").
- Persist "onboarding shown" with the `save-preferences` skill so it runs once.
- Instrument each step with `Analytics.logScreenView` (see `setup-analytics`) so drop-off is
  measurable.

## Done
Onboarding runs end to end, captures the goal, delivers a first taste, and primes permissions.

**When to run this:** the kit ships an onboarding flow already, so it shows boilerplate until you brand
it — do that in **Phase 1** (`build-features`), since it's the first thing every user sees. The
**`growth`** phase revisits it later to *optimize* activation (funnel events, priming, A/B copy) — not
to design it from scratch.

**See what it looks like:** use the **`verify-ui`** skill to render each onboarding page to a PNG
(`./gradlew :shared:recordRoborazziAndroidHostTest` → `snapshots/*OnBoarding*.png`) and look at it.
That's also how to answer "show me the onboarding screens" — no emulator needed.
