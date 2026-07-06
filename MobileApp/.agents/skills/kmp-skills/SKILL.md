---
name: kmp-skills
description: Index and router for ALL KMPStarterKit developer skills — running the app, adding screens/models/API calls/preferences/permissions, Firebase, auth, web-proxy, publishing, subscriptions, credits, ads, analytics, notifications, onboarding, growth. Consult this FIRST for any KMPStarterKit task; it maps the request to the right skill in the repository-root `skills/` folder and tells you how to load it.
---

# KMPStarterKit — Skills Index & Router

The full skill library for this project is **not** duplicated here. It lives once, canonically, in the
**`skills/` directory at the repository root** — one level **above** this `MobileApp/` project.

This file exists so an agent working inside the `MobileApp/` window (which is scoped to the Gradle
project and does not auto-discover skills above it) can still find and use the whole library.

## How to use a skill

1. Find the task in the index below.
2. **Read the matching file** `skills/<name>/SKILL.md` in the **repository root** (the parent folder of
   this `MobileApp/` project). If your tool prompts to grant access to the parent directory, allow it —
   the skills are plain Markdown, read-only.
   - From this `MobileApp/` project that path is `../skills/<name>/SKILL.md` relative to the project root
     (i.e. `../../../../skills/<name>/SKILL.md` relative to *this* file).
3. Follow that skill's steps. **Run any `./gradlew` or `./scripts/` command from the `MobileApp/`
   directory** — every mobile skill assumes that working directory.

> Prefer the **phase guides** for end-to-end journeys; drop to a **task skill** for one specific job.
> Names below match the folder names under repo-root `skills/`.

## The developer journey — 5 phase guides

| Guide | Read | Goal |
|---|---|---|
| **getting-started** | `skills/getting-started/SKILL.md` | Phase 1 · run the app locally, rebrand, and drive it with a screen, Room model, preference, network call, and permission — LOCAL only |
| **integrations** | `skills/integrations/SKILL.md` | Phase 2 · Firebase + anonymous/social auth + web-proxy Cloud Functions; real remote calls |
| **publishing** | `skills/publishing/SKILL.md` | Phase 3 · icons, release signing (keys → CI), store listings, first build in review |
| **monetization** | `skills/monetization/SKILL.md` | Phase 4 · subscriptions + credit-pack IAPs + paywall + ads |
| **growth** | `skills/growth/SKILL.md` | Phase 5 · analytics/Crashlytics/RemoteConfig, push, onboarding, virality loops |

## Task skills by phase

### Phase 1 — First Run (local-only)
- **run-the-app** — build/run on Android, Desktop, Web, or iOS. → `skills/run-the-app/SKILL.md`
- **refactor-package** — rebrand: rename package / applicationId / bundle ID / display name. → `skills/refactor-package/SKILL.md`
- **new-screen** — scaffold a Screen + UiState + ViewModel and wire nav + DI. → `skills/new-screen/SKILL.md`
- **new-local-model** — scaffold a Room 3 model (entity + DAO + DI). → `skills/new-local-model/SKILL.md`
- **add-api-service** — Ktor network request: DTOs → API service → repository → ViewModel. → `skills/add-api-service/SKILL.md`
- **save-preferences** — persist a simple typed setting via DataStore. → `skills/save-preferences/SKILL.md`
- **add-permission** — request a runtime permission via AppPermissionState. → `skills/add-permission/SKILL.md`
- **new-module** — add a Gradle KMP library module. → `skills/new-module/SKILL.md`

### Phase 2 — Integrations
- **configure-environment** — where every key/config value lives (local.properties, gradle.properties, Constants.kt). → `skills/configure-environment/SKILL.md`
- **setup-firebase** — create project, register apps, config files, anonymous auth. → `skills/setup-firebase/SKILL.md`
- **enable-auth** — Google / Apple social sign-in. → `skills/enable-auth/SKILL.md`
- **integrate-web-proxy** — deploy the Cloud Functions AI proxy + call it securely. → `skills/integrate-web-proxy/SKILL.md`

### Phase 3 — Publication
- **generate-app-icons** — iOS + Android launcher icons from one source logo. → `skills/generate-app-icons/SKILL.md`
- **bump-version** — bump Android + iOS versions together. → `skills/bump-version/SKILL.md`
- **setup-signing** — release keystore + move signing keys into CI secrets. → `skills/setup-signing/SKILL.md`
- **store-screenshots** — generate store screenshots from @StoreScreenshot previews. → `skills/store-screenshots/SKILL.md`
- **setup-appstore-connect** — create/configure the App Store Connect listing. → `skills/setup-appstore-connect/SKILL.md`
- **setup-google-play** — create/configure the Google Play Console listing. → `skills/setup-google-play/SKILL.md`
- **publish-release** — build signed artifacts + submit for review. → `skills/publish-release/SKILL.md`

### Phase 4 — Monetization
- **design-paywall** — author the offer / pricing / trial / paywall copy. → `skills/design-paywall/SKILL.md`
- **setup-subscriptions** — subscriptions via Adapty (default) or RevenueCat. → `skills/setup-subscriptions/SKILL.md`
- **enable-credits** — credit balance + credit-pack IAPs. → `skills/enable-credits/SKILL.md`
- **enable-ads** — AdMob banner / interstitial / rewarded ads. → `skills/enable-ads/SKILL.md`

### Phase 5 — Growth
- **setup-analytics** — Firebase Analytics + Crashlytics + Remote Config feature flags. → `skills/setup-analytics/SKILL.md`
- **enable-notifications** — push (FCM) + local notifications + iOS APNs. → `skills/enable-notifications/SKILL.md`
- **design-onboarding** — design/build the first-run onboarding. → `skills/design-onboarding/SKILL.md`
- **add-virality-loop** — share / referral / win-back / in-app review. → `skills/add-virality-loop/SKILL.md`

### Cross-phase
- **run-quality-gates** — lint + tests + build, the same checks as CI. → `skills/run-quality-gates/SKILL.md`

---

*One source of truth: edit the real skills under repo-root `skills/`. This index only points to them.*
