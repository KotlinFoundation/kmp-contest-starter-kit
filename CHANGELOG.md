# Changelog

Template release notes, written for **derived apps syncing updates** (see
[`skills/sync-template/SKILL.md`](skills/sync-template/SKILL.md)). Newest first.

Entry format — one bullet per merged PR, tagged by scope:

- `[app]` touches shipped app code (`MobileApp/`, `Web/`) — a sync will merge it into your app.
- `[skills]` / `[docs]` agent skills, AGENTS.md, guides — merges cleanly unless you edited them.
- **Manual:** anything a derived app must do by hand after the merge.

## 2026-08-08

- `[app]` **Gradle configuration cache enabled** (#42): `org.gradle.configuration-cache=true` in
  `MobileApp/gradle.properties` — skips the configuration phase on repeat builds, the biggest
  lever for iterative iOS/Android/Wasm build speed. Verified against every quality gate, the Wasm
  compile, and the iOS framework link. Also documents the experimental `kotlin.incremental.native`
  opt-in (commented out). Manual: none — but if your app added a config-cache-incompatible plugin,
  builds will tell you; set the property back to `false` in that case.
