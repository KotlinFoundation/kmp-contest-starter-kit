# Onboarding

This app's onboarding strategy. Onboarding exists to **capture intent, build commitment, and deliver
a first taste of value** before any ask — then hand off to the paywall at the motivation peak
(see `paywall.md`). Fill the `TAILOR PER APP` blanks for your product; the rest are strong defaults.

> Code lives in
> `MobileApp/shared/src/commonMain/kotlin/com/kotlinfoundation/koko/presentation/screens/onboarding/`
> (`OnBoardingScreen.kt` + `OnBoardingScreenVariation1/2.kt`, `OnBoardingUiState.kt`).

## Pattern choice

Pick the shape that fits *this* app — not "more screens" by default:

- **Short emotional (3–4 screens):** hook → problem → transformation → soft offer. Best for a simple,
  self-evident value prop and warm traffic. This is the default `OnBoardingScreen` shape.
- **Questionnaire / quiz-led (9–20 screens):** promise → trust → goal → profile → micro-progress →
  personalized plan → first taste of value → recap → paywall. Best when personalization meaningfully
  changes the experience and traffic is colder/paid.

**`TAILOR PER APP` — chosen pattern + why:** _e.g. "questionnaire-led, because the plan is
personalized to the user's goal and most installs come from paid social."_

## The levers (apply in priority order)

1. **Capture the goal, surface it on the paywall.** Ask the user's primary goal early and echo it at
   the ask. Usually the single biggest lever.
   - **`TAILOR PER APP` — goal-capture question(s):** _e.g. "What's your main goal? Lose weight /
     Build muscle / Eat healthier."_
2. **Value before the ask.** Let the user *use the core mechanic once* (a real sample/output), not a
   passive tour, before signup or paywall.
   - **`TAILOR PER APP` — the first-taste moment:** _e.g. "Generate one free result from their input."_
3. **Permission priming.** Precede every system permission with a benefit-framed screen; never open
   with a permission wall.
   - **`TAILOR PER APP` — permissions & their benefit framing:** _e.g. "Notifications → streak
     reminders."_
4. **Micro-progress & low friction.** Step indicator, light positive feedback; no forced account
   creation before value; offer a skip/guest path; only collect fields that change the experience.

## Copy & consistency

- Keep the promise consistent from ad → onboarding → paywall.
- Headlines short and benefit-led; conversational, not corporate (see
  `AiGuidelines/agents/onboarding_designer.md` for the screen-by-screen copy brief).
- **`TAILOR PER APP` — top 3 pain points this app resolves:** _…_

## Measure

Instrument: onboarding completion rate, per-step drop-off, and which step loses the most users.
A change with no measurable funnel is a change you can't judge — add the events first.

> Conversion principles, the pattern toolkit, and the reviewer's rubric live in
> `AiGuidelines/loop/CONVERSION_PLAYBOOK.md` when the self-improve loop is installed.
