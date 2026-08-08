---
name: build-features
description: Build the app's real features from prd.md / user_flow.md — derive the screens and local models, scaffold them with new-screen / new-local-model, and implement the UI per ui_ux.md. Use to implement the MVP after the product is defined, or any time later to add a feature described in the PRD ("add streaks to my habit tracker").
---

# Build the app's features from the PRD

Turns the product docs into **this app's actual screens and data** — not demo scaffolding.

**Prerequisite:** [`AiGuidelines/project/prd.md`](../../AiGuidelines/project/prd.md) and
[`user_flow.md`](../../AiGuidelines/project/user_flow.md) are filled. If they're still blank
(just a heading), run the **`new-app`** skill first — don't invent the product here.

All commands run from `MobileApp/`.

## 0. Resume before you build — **Agent Action, always first**

**`PROGRESS_FEATURES.md`** at the **git repository root** is the record of what has actually been built.
"Repo root" = the folder that contains `MobileApp/` — **not** inside `MobileApp/`. You're most likely
running from the MobileApp Window, so it's **one level up**: `../PROGRESS_FEATURES.md`. Confirm the root
with `git rev-parse --show-toplevel` before deciding the file is missing.

- **If it exists:** read it and **continue from the first unchecked item**. Do **not** re-derive the
  plan and do **not** redo checked work.
- **If it doesn't:** copy [`progress-template.md`](progress-template.md) (next to this file) to
  `PROGRESS_FEATURES.md` at the git repo root (`../PROGRESS_FEATURES.md` from the MobileApp Window — never
  inside `MobileApp/`), then derive the plan (step 1) and fill it in.

**Tick items off as you finish them, not at the end** — a session can stop at any point, and this file
is the only thing that tells the next one what's left. Commit it with the code.

## 1. Derive the build plan — **Agent Action**

Read `prd.md` (core features), `user_flow.md` (screen map + navigation), and `ui_ux.md` (visual
direction). From them produce:

- **Screens** — one per screen in the user flow.
- **Local models** — one per persisted entity in the PRD (a habit, a completion, …).
- **Navigation** — which screen leads where; which are bottom-nav tabs.

**Show the plan and get an explicit confirm before generating anything.** List it plainly:

```
Models:  Habit, HabitCompletion
Screens: HabitList (tab), HabitDetail, AddHabit, Stats (tab)
```

Once confirmed, **write the plan into `PROGRESS_FEATURES.md`** (one line per model and per screen)
before you generate anything — that's what makes the build resumable.

## 2. Scaffold — **Agent Action, and run these SEQUENTIALLY**

> [!IMPORTANT]
> Both scripts **patch shared files** (`Routes.kt`, `AppNavigation.kt`, `root/Di.kt`,
> `AppDatabase.kt`, `DatabaseModule.kt`) at their insertion-point markers. **Never run them in
> parallel** — concurrent runs corrupt those files. One at a time, models first.

```bash
./scripts/make_local.sh Habit          # → domain model + @Entity + @Dao + DB + Koin
./scripts/generate_screen.sh HabitList # → Screen + UiState + ViewModel + route + DI
```

Use the **`new-local-model`** and **`new-screen`** skills for the rules each one expects.

## 3. Implement — **Agent Action — fan out subagents in parallel**

Scaffolding was sequential; implementation must not be. The per-feature work lives in **separate
folders**, so launch **one subagent per screen and per model — all at once, not one after another**.
On a 4-screen app this is the single biggest time saving of the whole phase; only fall back to
sequential if your harness has no subagent support.

**File-ownership rule (what makes the fan-out safe):**

- A subagent edits **only its own feature's files**: `presentation/screens/<feature>/` for a screen,
  the model's `@Entity`/`@Dao`/domain files for a model.
- **Shared files stay with you**, the orchestrator: `Routes.kt`, `AppNavigation.kt`, `root/Di.kt`,
  `AppDatabase.kt`, `DatabaseModule.kt`, and `composeResources/values/strings.xml`. If a subagent
  needs new string resources, have it **return them** and merge them yourself — two subagents
  editing `strings.xml` concurrently is a lost update.
- Subagents **don't run Gradle** — no per-screen builds or tests. Concurrent Gradle runs in one
  checkout just serialize on the daemon lock. You validate everything once in step 5.

What each subagent does:

- **Models** — real columns on the `@Entity`, update both mappers, DAO queries the feature needs.
- **Screens** — build the UI in the pure composable overload per `ui_ux.md`; keep logic in the
  ViewModel; use `designsystem` components (`AppButton`, `AppCard`, …).
- **Navigation** — edit the generated `entry<…>` blocks in `AppNavigation.kt` to add the callbacks
  the flow needs. *(This file is shared — do this yourself, not in a subagent.)*

The step-4 branding work below is independent of the feature screens — put it **in the same
fan-out** rather than doing it afterwards.

Reach for these only if the feature actually needs them:
- **`add-api-service`** — a remote call (any public URL; no backend needed).
- **`save-preferences`** — a simple setting/flag.
- **`add-permission`** — camera/notifications/etc.
- **AI (OpenAI / Replicate)** — works in this phase with **no Firebase**: put `OPENAI_API_KEY` and/or
  `REPLICATE_API_KEY` in `MobileApp/local.properties` and leave `AppConfiguration.CLOUD_FUNCTIONS_URL`
  blank — `AiTransport` then calls the provider straight from the device. The key ships in the binary,
  so it's **prototyping only**: `integrate-web-proxy` moves it off-device before you publish.
- **`new-module`** — only if the code genuinely belongs outside `shared/` (a reusable library, or a
  second implementation behind an API). Most features do **not** need this.

## 4. Brand the screens that already ship — **Agent Action**

The kit **already includes an onboarding flow and a paywall** (`presentation/screens/onboarding/`,
`presentation/screens/paywall/`). Out of the box they show boilerplate copy about the wrong product —
the developer will hit them the moment they run the app, so fix them now, not at publish time.
Both jobs are independent of your feature screens — run them as **two more subagents in the step-3
fan-out** (same ownership rule: they return their `strings.xml` entries; you merge them):

- **Onboarding** — rebuild the copy/steps from
  [`AiGuidelines/project/onboarding.md`](../../AiGuidelines/project/onboarding.md): the chosen pattern,
  the goal-capture question, the first-taste moment, permission benefit framing. Use the
  **`design-onboarding`** skill.
- **Paywall** — apply the **primary model + benefit copy** from
  [`AiGuidelines/project/paywall.md`](../../AiGuidelines/project/paywall.md) to the `paywall_*` strings
  in `composeResources/values/strings.xml`. Use the **`design-paywall`** skill.
  - It's fully testable right now: the **mock provider** runs paywall → purchase → unlock with **no
    account and no keys** (a red demo banner shows while mocked).
  - Leave **prices / trial length / pack sizes** as `TODO(monetization)` — those are Phase 4, set when
    the real store products exist.

## 5. Validate — **Agent Action → User confirms**

Run this **once, after all subagents are merged** — not per screen. One
`recordRoborazziAndroidHostTest` run snapshots **every** `@Preview` in a single Gradle invocation,
so N screens still cost one build.

Verify each screen you built with the **`verify-ui`** skill *before* handing back:
- **Behaviour** — a headless `runComposeUiTest` against the screen's pure `(uiState, onUiEvent)`
  overload (~2s): state renders, clicks emit the right events.
- **Appearance** — add a `@Preview`, run `./gradlew :shared:recordRoborazziAndroidHostTest`, and
  **look at the PNG**. This is what catches layout/spacing/theming mistakes that tests pass right over.

```bash
./gradlew spotlessApply
```
Then run the **`run-quality-gates`** skill, and have the developer run the app
(`./gradlew :desktopApp:run` is fastest) to confirm the features behave.

---

## Adding features later

Same skill, smaller scope: describe the feature (or point at the PRD section), confirm the plan, scaffold
sequentially, implement, validate. If the feature isn't in `prd.md` yet, add it there first so the docs
stay the source of truth — then append it under **Later additions** in `PROGRESS_FEATURES.md` and work
it the same way.
