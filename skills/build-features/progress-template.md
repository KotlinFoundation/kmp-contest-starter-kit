# Features — <App name>

> [!NOTE]
> **Setup:** the `build-features` skill copies this template to the repository root as
> **`PROGRESS_FEATURES.md`** and keeps it up to date. It is the record of **what has actually been
> built** from the PRD — the phase files (`PROGRESS_P1…`) track the *guide's* steps; this one tracks
> *your app's features*.
>
> **Resume rule:** before building anything, read this file and continue from the first unchecked
> item. Never redo a checked item, and never re-derive the plan while unchecked items remain.
>
> **Commit it.** It travels with the repo so any session/machine can pick up where the last one stopped.

**Source of truth:** `AiGuidelines/project/prd.md` · `user_flow.md` · `ui_ux.md`
**Status key:** `[ ]` not started · `[~]` in progress · `[x]` done

---

## Models
<!-- one line per persisted entity from the PRD; scaffold with make_local.sh (run sequentially) -->

- [ ] `<Model>` — scaffolded ☐ · real columns + mappers ☐ · DAO queries ☐

## Screens
<!-- one line per screen in user_flow.md; scaffold with generate_screen.sh (run sequentially) -->

- [ ] `<Screen>` — scaffolded ☐ · UI per `ui_ux.md` ☐ · nav callbacks wired ☐

## Screens that ship with the kit (brand them to this product)

- [ ] **onboarding** — copy/steps rebuilt from `AiGuidelines/project/onboarding.md` (`design-onboarding`)
- [ ] **paywall** — primary model + benefit copy from `AiGuidelines/project/paywall.md`
      (`design-paywall`). Runs on the mock provider — no account needed.
      Prices / trial / pack sizes stay `TODO(monetization)` until Phase 4.

## Optional building blocks (only if a feature needs one)

- [ ] Remote call — `add-api-service`
- [ ] Stored setting — `save-preferences`
- [ ] Device permission — `add-permission`

## Validation

- [ ] `run-quality-gates` passes (spotless, tests, Android debug build)
- [ ] Developer ran the app and confirmed the features behave

---

## Later additions
<!-- append new features here as they're requested; add them to prd.md first -->
