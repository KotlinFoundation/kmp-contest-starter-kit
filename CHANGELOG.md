# Changelog

Template release notes, written for **derived apps syncing updates** (see
[`skills/sync-template/SKILL.md`](skills/sync-template/SKILL.md)). Newest first.

Entry format — one bullet per merged PR, tagged by scope:

- `[app]` touches shipped app code (`MobileApp/`, `Web/`) — a sync will merge it into your app.
- `[skills]` / `[docs]` agent skills, AGENTS.md, guides — merges cleanly unless you edited them.
- **Manual:** anything a derived app must do by hand after the merge.

## Unreleased

- `[skills]` **Template sync tooling** (#41): `MobileApp/scripts/sync_template.sh` (vendor-branch
  sync that renames the incoming template tree), the `sync-template` skill, and this changelog.
  Manual: add the `template` remote once —
  `git remote add template https://github.com/KotlinFoundation/kmp-contest-starter-kit.git`.

## 2026-08

- `[docs]` **Agents parallelize by default** (#40): "Agent Working Style" section in `AGENTS.md`,
  concrete subagent fan-out in `build-features` step 3, pointers in `getting-started` and the
  `MobileApp/` context files.
- `[app]` **Replicate polling** (#39): `ReplicateGenerationProvider` polls prediction status
  instead of failing on a `null` output; `Web/functions/api/replicate.js` matches. Re-deploy the
  Cloud Functions if you use the proxy (`integrate-web-proxy`).
- `[docs]` **Template review** (#38): security pass, skill discoverability, Firestore/Replicate
  docs; `capture-app-screens` skill replaces `store-screenshots`.
- `[skills]` **`sync-data-firebase` skill** (#37): Firestore/cross-device sync guidance with the
  wasmJs compatibility question up front; Wasm/Firebase rules in `AGENTS.md`.
- `[skills]` **Firebase + Wasm guidance** (#36): `setup-firebase` documents the Wasm-safe
  Firestore-via-Cloud-Functions architecture instead of telling agents to drop the web target.
- `[app]` **UI hotfixes** (#35): iOS splash background matches the theme (light `#F9F7FF` /
  dark `#12101A`, both appearances in `SplashBackground.colorset`); the mock-paywall demo notice
  is a warning dialog on each open instead of a banner; purchase-success view no longer flashes
  the paywall on continue.
- `[app]` **KMPAuth 3.0 migration** (#34): in-repo `libs/auth` module deleted; the `KMPAuth`
  facade is used directly (Android, iOS, desktop, web incl. session persistence). Typed
  exceptions (`KMPAuthUserCollisionException`, `KMPAuthRecentLoginRequiredException`) replace the
  old domain ones. Manual: set `FIREBASE_API_KEY` / `FIREBASE_PROJECT_ID` /
  `FIREBASE_APPLICATION_ID` in `local.properties` for desktop/web auth (blank is fine for
  mobile-only); in Xcode, Reset Package Caches after the SwiftPM linkage rename.

## Baseline

- **Initial commit** (`05eca27`, 2026-07-31): history squashed to a single root. Derived apps
  created before this need `--bootstrap` on their first `sync_template.sh` run.
