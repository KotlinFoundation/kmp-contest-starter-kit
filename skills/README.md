# Agent Skills

Vendor-neutral skills for AI coding agents (Claude Code, Codex, Gemini CLI, Cursor, …) working in this repo.
Each skill is a `<name>/SKILL.md` file in the open [Agent Skills](https://agentskills.io) format:
YAML frontmatter (`name`, `description`) followed by step-by-step instructions.

The skills are **vendor-neutral** — plain Markdown with real commands, file paths, and console
URLs; no assistant-specific syntax. Any agent that reads them can run them. Only *discovery* differs
per tool, so the repo ships a pointer file for each:

| Agent | How it finds the skills |
|---|---|
| Claude Code | Auto-discovered — `.claude/skills` symlinks to this folder |
| Codex | Reads [`AGENTS.md`](../AGENTS.md) (Skills section → this index) |
| Gemini CLI | Reads `GEMINI.md` (symlinked to `AGENTS.md`) |
| Cursor | Reads `.cursorrules` (points here) |
| GitHub Copilot | Reads `.github/copilot-instructions.md` (points here) |
| Any other / no AI | Open this index and read the matching `SKILL.md` — every step is followable by hand |

**No AI? Walk them manually.** The skills encode everything you need; you should not have to read
external docs to get from a cloned template to a shipped, earning app.

## Two layers

**Guides** are phase-by-phase blueprints for the whole developer journey. Each owns an ordered
checklist whose steps are tagged **Agent Action** (an agent/dev does it: code, script, gradle),
**User Action** (human-only: a browser console, Xcode, a device), or **Validation** (a gate before
moving on). An agent following a guide **stops at each User Action** and waits for you. Copy the
guide's `progress-template.md` into your repo to track where you are.

**Progress files** live at your **git repo root** — the folder that contains `MobileApp/`, **not** inside
it (if you opened `MobileApp/` in the IDE, they're one level up at `../PROGRESS_*.md`) — and get committed,
so any session resumes where the last one stopped: **`PROGRESS_FEATURES.md`** (what's actually built — each
model/screen from the PRD, written by [build-features](build-features/SKILL.md)) and **`PROGRESS_P1…P5`**
(each phase guide's steps). The rule: read the file first, continue from the first unchecked item, tick
items off as you go. If an agent stalls mid-guide, tell it **"continue @koko-mobileapp-getting-started"**
(or the relevant guide) — it re-reads the progress file and picks up at the first unchecked item.

**Task skills** do one job each and are usable standalone. The guides call them by name.

## Environment keys (don't get silently skipped)

Service keys (Firebase, social sign-in, Adapty/RevenueCat, AdMob) live in the gitignored
`MobileApp/local.properties`. The build defaults missing keys to `"testValue"`/empty and stays green,
so a forgotten key is invisible in the build. Two tools make it visible:

- **`MobileApp/local.properties.example`** — committed template of every key, with placeholder,
  where-to-get URL, and owning phase. Copy it to `local.properties` and fill what your phase needs.
- **`MobileApp/scripts/check_env.sh --phase <phase>`** — reports which keys required by that phase are
  still placeholders (✅ / ⚠️ / ⚪). Non-breaking; wired into `run-quality-gates` and every guide's
  validation gate. Feature-aware, so the easy first-run path (anonymous auth, no ads) is never nagged.

## The journey (5 phases)

**Starting from an idea?** Run **[new-app](new-app/SKILL.md)** first — it interviews you, writes the
PRD / user flow / UI direction, and records the decisions you can defer. It then hands straight to the
Phase 1 guide. No accounts needed to get going: the kit ships a mock subscription provider and a
no-Firebase AI path, so Firebase / Adapty / store accounts wait until the phase that needs them.

| Phase | Guide | You end with |
|---|---|---|
| 1 · First Run | [getting-started](getting-started/SKILL.md) | The app **running on your own device**, rebranded, with **your MVP features** built — no cloud. |
| 2 · Integrations | [integrations](integrations/SKILL.md) | Firebase + auth + the web-proxy backend wired; real remote calls work. |
| 3 · Publication | [publishing](publishing/SKILL.md) | Icons, release signing (keys in CI, not the app), store listings, first build in review. |
| 4 · Monetization | [monetization](monetization/SKILL.md) | Subscriptions + credit-pack IAPs + paywall + ads; a test purchase unlocks premium. |
| 5 · Growth | [growth](growth/SKILL.md) | Analytics/Crashlytics/RemoteConfig, push notifications, onboarding, virality loops. |

## Task skills by phase

**Phase 1 — First Run (local-only)**

| Skill | Use when |
|---|---|
| [new-app](new-app/SKILL.md) | Starting from an idea — interview, write the PRD/user-flow/UI docs, pick name + id, record deferred decisions |
| [build-features](build-features/SKILL.md) | Building your app's real features from the PRD (also later: "add streaks to my habit tracker") |
| [run-the-app](run-the-app/SKILL.md) | Building/running the app on Android, Desktop, Web, or iOS for the first time |
| [refactor-package](refactor-package/SKILL.md) | Renaming the package / applicationId / bundle ID / display name (rebrand) |
| [new-screen](new-screen/SKILL.md) | Adding a screen (scaffolds UI + route + DI wiring) |
| [new-local-model](new-local-model/SKILL.md) | Storing a model locally (Room entity + DAO + DI) |
| [add-api-service](add-api-service/SKILL.md) | Making a network request (DTOs → API service → repository → ViewModel) |
| [save-preferences](save-preferences/SKILL.md) | Persisting a simple typed key/value setting (DataStore) |
| [add-permission](add-permission/SKILL.md) | Requesting a runtime permission (camera, notifications, …) |
| [new-module](new-module/SKILL.md) | Adding a new Gradle KMP library module |

**Phase 2 — Integrations**

| Skill | Use when |
|---|---|
| [configure-environment](configure-environment/SKILL.md) | Figuring out where a config value / API key lives (local.properties, gradle.properties, AppConfiguration.kt, Constants.kt) |
| [setup-firebase](setup-firebase/SKILL.md) | Connecting the app to Firebase (project, apps, anonymous auth) — and the wasmJs question that must be asked before any Firebase client SDK enters shared code |
| [enable-auth](enable-auth/SKILL.md) | Adding Google / Apple social sign-in |
| [integrate-web-proxy](integrate-web-proxy/SKILL.md) | Deploying the Cloud Functions AI proxy and calling it securely |
| [sync-data-firebase](sync-data-firebase/SKILL.md) | Putting data in Firestore / syncing across devices / making state server-authoritative (asks the wasmJs question first) |

**Phase 3 — Publication**

| Skill | Use when |
|---|---|
| [generate-app-icons](generate-app-icons/SKILL.md) | Setting/replacing the app + launcher icons |
| [bump-version](bump-version/SKILL.md) | Bumping versionCode / versionName for a release |
| [setup-signing](setup-signing/SKILL.md) | Release signing + moving keys out of the app into CI secrets |
| [capture-app-screens](capture-app-screens/SKILL.md) | Capturing your real screens as plain PNGs at store pixel sizes (no headlines / device frames — this kit has no design step) |
| [setup-appstore-connect](setup-appstore-connect/SKILL.md) | Creating + configuring the App Store Connect listing |
| [setup-google-play](setup-google-play/SKILL.md) | Creating + configuring the Google Play Console listing |
| [publish-release](publish-release/SKILL.md) | Building signed artifacts and submitting for review |

**Phase 4 — Monetization**

| Skill | Use when |
|---|---|
| [design-paywall](design-paywall/SKILL.md) | Authoring the offer / pricing / trial / paywall copy |
| [setup-subscriptions](setup-subscriptions/SKILL.md) | Adding subscriptions (Adapty default / RevenueCat) |
| [enable-credits](enable-credits/SKILL.md) | Adding a credit balance + credit-pack IAPs |
| [enable-ads](enable-ads/SKILL.md) | Turning on AdMob banner / interstitial / rewarded ads |

**Phase 5 — Growth**

| Skill | Use when |
|---|---|
| [setup-analytics](setup-analytics/SKILL.md) | Analytics, Crashlytics, Remote Config feature flags |
| [enable-notifications](enable-notifications/SKILL.md) | Push (FCM) + local notifications |
| [design-onboarding](design-onboarding/SKILL.md) | Designing/polishing the first-run onboarding |
| [add-virality-loop](add-virality-loop/SKILL.md) | Referral / share / win-back / in-app review prompts |

**Cross-phase**

| Skill | Use when |
|---|---|
| [verify-ui](verify-ui/SKILL.md) | Confirming a screen behaves (headless Compose test, ~2s) and looks right (render a PNG and look at it) |
| [run-quality-gates](run-quality-gates/SKILL.md) | Validating changes before commit/PR (lint, tests, build) |
| [sync-template](sync-template/SKILL.md) | Pulling template updates into an app created from this kit (survives the package rename; see `CHANGELOG.md`) |
