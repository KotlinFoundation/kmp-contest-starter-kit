# Changelog

Template release notes, written for **derived apps syncing updates** (see
[`skills/sync-template/SKILL.md`](skills/sync-template/SKILL.md)). Newest first.

Entry format — one bullet per merged PR, tagged by scope:

- `[app]` touches shipped app code (`MobileApp/`, `Web/`) — a sync will merge it into your app.
- `[skills]` / `[docs]` agent skills, AGENTS.md, guides — merges cleanly unless you edited them.
- **Manual:** anything a derived app must do by hand after the merge.

Entries start with the first template change after the sync tooling landed — group them under a
`## <year>-<month>-<date>` heading, newest first.

## 2026-08-14

- `[app]` **Aligned Firebase library versions** (#47): KMPAuth 3.0.3 → 3.0.5, KMPNotifier 2.0.0 → 2.0.1,
  Firebase BOM 34.14.1 → 34.17.0. KMPNotifier now declares firebase-ios-sdk as a range instead of an
  exact pin, and KMPAuth moved to GitLive firebase 3.0.0-alpha01, so the three no longer fight over
  firebase-ios-sdk on iOS. Manual: the iOS project pin moves 12.14.0 → **12.17.0** (the new floor) and
  the linkage package is regenerated — in Xcode run Reset Package Caches → Resolve Package Versions
  after pulling. Also fixes offline Google sign-in on Android.
- `[app]` **Web builds work with the configuration cache again** (#47): the `commonWebpackConfig { }`
  block in `webApp/build.gradle.kts` is a Gradle script object reference and cannot be serialized, so
  every `:webApp:wasmJs*` task failed once the configuration cache was enabled. The dev-server
  settings (the AI CORS proxies for OpenAI, Replicate and replicate.delivery) moved to
  `webApp/webpack.config.d/dev-server-proxy.js`. Manual: none, unless you added your own
  `commonWebpackConfig` block — move it to `webpack.config.d/` too.
