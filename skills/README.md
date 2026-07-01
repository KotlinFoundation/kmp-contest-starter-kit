# Agent Skills

Vendor-neutral skills for AI coding agents (Claude Code, Codex, Gemini CLI, Cursor, …) working in this repo.
Each skill is a `<name>/SKILL.md` file in the open [Agent Skills](https://agentskills.io) format:
YAML frontmatter (`name`, `description`) followed by step-by-step instructions.

- Agents with native skill support discover these automatically (`.claude/skills` symlinks here for Claude Code).
- Agents without native support: read the skill file referenced from the **Skills** section of [AGENTS.md](../AGENTS.md) before doing the matching task.
- **No AI? Walk them by hand.** Every step is written to be followed manually — real commands, real file paths, real console URLs. The skills encode everything you need; you should not have to read external docs to get from a cloned template to a shipped, earning app.

## Two layers

**Guides** are phase-by-phase blueprints for the whole developer journey. Each owns an ordered
checklist whose steps are tagged **Agent Action** (an agent/dev does it: code, script, gradle),
**User Action** (human-only: a browser console, Xcode, a device), or **Validation** (a gate before
moving on). An agent following a guide **stops at each User Action** and waits for you. Copy the
guide's `progress-template.md` into your repo to track where you are.

**Task skills** do one job each and are usable standalone. The guides call them by name.

## The journey (5 phases)

| Phase | Guide | You end with |
|---|---|---|
| 1 · First Run | [getting-started](getting-started/SKILL.md) | The app **running on your own device**, rebranded, driven purely locally — no cloud. |
| 2 · Integrations | [integrations](integrations/SKILL.md) | Firebase + auth + the web-proxy backend wired; real remote calls work. |
| 3 · Publication | [publishing](publishing/SKILL.md) | Icons, release signing (keys in CI, not the app), store listings, first build in review. |
| 4 · Monetization | [monetization](monetization/SKILL.md) | Subscriptions + credit-pack IAPs + paywall + ads; a test purchase unlocks premium. |
| 5 · Growth | [growth](growth/SKILL.md) | Analytics/Crashlytics/RemoteConfig, push notifications, onboarding, virality loops. |

## Task skills by phase

**Phase 1 — First Run (local-only)**

| Skill | Use when |
|---|---|
| [run-the-app](run-the-app/SKILL.md) | Building/running the app on Android, Desktop, Web, or iOS for the first time |
| [new-screen](new-screen/SKILL.md) | Adding a screen (scaffolds UI + route + DI wiring) |
| [new-local-model](new-local-model/SKILL.md) | Storing a model locally (Room entity + DAO + DI) |
| [add-api-service](add-api-service/SKILL.md) | Making a network request (DTOs → API service → repository → ViewModel) |
| [save-preferences](save-preferences/SKILL.md) | Persisting a simple typed key/value setting (DataStore) |
| [add-permission](add-permission/SKILL.md) | Requesting a runtime permission (camera, notifications, …) |
| [new-module](new-module/SKILL.md) | Adding a new Gradle KMP library module |

**Phase 2 — Integrations**

| Skill | Use when |
|---|---|
| [configure-environment](configure-environment/SKILL.md) | Figuring out where a config value / API key lives (local.properties, gradle.properties, Constants.kt) |
| [setup-firebase](setup-firebase/SKILL.md) | Connecting the app to Firebase (project, apps, anonymous auth) |
| [enable-auth](enable-auth/SKILL.md) | Adding Google / Apple social sign-in |
| [integrate-web-proxy](integrate-web-proxy/SKILL.md) | Deploying the Cloud Functions AI proxy and calling it securely |

**Phase 3 — Publication**

| Skill | Use when |
|---|---|
| [refactor-package](refactor-package/SKILL.md) | Renaming the package / applicationId / bundle ID / display name (rebrand) |
| [generate-app-icons](generate-app-icons/SKILL.md) | Setting/replacing the app + launcher icons |
| [bump-version](bump-version/SKILL.md) | Bumping versionCode / versionName for a release |
| [setup-signing](setup-signing/SKILL.md) | Release signing + moving keys out of the app into CI secrets |
| [store-screenshots](store-screenshots/SKILL.md) | Generating App Store / Play Store screenshots |
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
| [run-quality-gates](run-quality-gates/SKILL.md) | Validating changes before commit/PR (lint, tests, build) |
