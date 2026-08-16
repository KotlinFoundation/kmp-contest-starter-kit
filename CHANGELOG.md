# Changelog

Template release notes, written for **derived apps syncing updates** (see
[`skills/sync-template/SKILL.md`](skills/sync-template/SKILL.md)). Newest first.

Entry format — one bullet per merged PR, tagged by scope:

- `[app]` touches shipped app code (`MobileApp/`, `Web/`) — a sync will merge it into your app.
- `[skills]` / `[docs]` agent skills, AGENTS.md, guides — merges cleanly unless you edited them.
- **Manual:** anything a derived app must do by hand after the merge.

Entries start with the first template change after the sync tooling landed — group them under a
`## <year>-<month>-<date>` heading, newest first.

## 2026-08-16

- `[app]` **iOS publishing works without a Mac** (#48): the iOS publish workflow had never run on a
  generated app. Signing now goes through fastlane `match`, which creates the certificate on the
  runner from the App Store Connect API key and stores it encrypted on a `match-certificates` branch
  of the same repo — no hand-exported `.p12`, no second repo, no personal access token. The
  `local.properties` written by CI was also writing two keys the build no longer reads while missing
  fourteen it does, so releases shipped green with dead sign-in, analytics and paywall; and archive
  and export disagreed about signing style. Signing settings are applied per target
  (Swift Package dependencies reject a provisioning profile), and the workflow drives the project's
  own Fastfile instead of a parallel xcodebuild. Manual: set `APPSTORE_ISSUER_ID`, `APPSTORE_KEY_ID`,
  `APPSTORE_PRIVATE_KEY` and `MATCH_PASSWORD` as repo secrets; the old
  `IOS_APP_CERTIFICATE_P12_BASE64` and friends are no longer needed.
- `[app]` **Android releases and PR checks write every build key** (#48): both workflows wrote the same
  stale three-key `local.properties` block, two of which the build no longer reads, so Play Store
  releases shipped with placeholder sign-in, ads, AI and paywall exactly like iOS did. All three
  workflows now derive the key list from `MobileApp/local.properties.example`, so adding a key there
  needs no workflow edit. Manual: set a repo secret for each key you use — the release workflows warn
  about any that are missing.
- `[docs]` **Publishing docs match the new secrets** (#48): `setup-signing`, the `publishing` guide and
  its progress template, the docs-site CI page and the iOS production page all still described the
  hand-exported `.p12` flow and two RevenueCat keys the build no longer reads. They now list the four
  iOS secrets that exist, say that `MATCH_PASSWORD` is a passphrase you invent, and state the rule the
  workflows rely on: every key in `local.properties.example` needs a repo secret of the same name.
- `[app]` **One certificates repo per Apple account, not per app** (#48): certificates were stored on a
  branch of each app's own repo, but an iOS distribution certificate belongs to the Apple account and
  Apple issues at most two — so every new app burned a slot and the account was locked out almost
  immediately. `match` also now runs read-only on releases, since with write access it mints a new
  certificate whenever it is unsatisfied; set `MATCH_READONLY=false` for the one bootstrap run.
  Signing material now lives in a private repo shared across the account. Manual: create an empty
  private certs repo plus a fine-grained PAT with write access to it, and set `MATCH_GIT_URL` and
  `MATCH_GIT_BASIC_AUTHORIZATION`; `APPSTORE_PRIVATE_KEY` is now the **base64** of the `.p8`, not its
  raw contents.

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
