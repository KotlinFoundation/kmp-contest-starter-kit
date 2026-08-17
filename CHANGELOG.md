# Changelog

Template release notes, written for **derived apps syncing updates** (see
[`skills/sync-template/SKILL.md`](skills/sync-template/SKILL.md)). Newest first.

Entry format — one bullet per merged PR, tagged by scope:

- `[app]` touches shipped app code (`MobileApp/`, `Web/`) — a sync will merge it into your app.
- `[skills]` / `[docs]` agent skills, AGENTS.md, guides — merges cleanly unless you edited them.
- **Manual:** anything a derived app must do by hand after the merge.

Entries start with the first template change after the sync tooling landed — group them under a
`## <year>-<month>-<date>` heading, newest first.

## 2026-08-17

- `[app]` **Info.plist declares orientations and export compliance** (#53): the target builds for
  iPhone and iPad but declared no orientations, so App Store Connect rejects the upload with
  ITMS-90474. All four orientations are now declared for both device families.
  `ITSAppUsesNonExemptEncryption=false` answers the export-compliance question once instead of on
  every upload. Manual: nothing, unless you ship non-exempt cryptography — then set it `true`.
- `[app]` **Only the permission modules the app uses are linked** (#52): the kit depended on Calf's
  umbrella `calf-permissions` artifact, which links every permission API — location, bluetooth,
  contacts and the rest. App Store review scans for those symbols, so uploads came back with
  **ITMS-90683: Missing purpose string in Info.plist** demanding `NSLocationWhenInUseUsageDescription`
  for a permission the app never requests. Calf 0.12.0 → 0.13.0 and the dependency is now
  `calf-permissions-core` plus one module per permission actually used: camera, gallery,
  notifications. Manual: `rememberLocationPermissionState()` and `rememberMicrophonePermissionState()`
  are gone — if you used them, add `calf-permissions-location` / `-microphone` in
  `libs.versions.toml` and `shared/build.gradle.kts` and call
  `rememberAppPermissionState(Permission.FineLocation)` instead. Do not switch back to the umbrella
  module.

## 2026-08-16

- `[app]` **iOS publishing works without a Mac** (#48): the iOS publish workflow had never run on a
  generated app — it wanted a hand-exported `.p12`, which needs the Mac the workflow exists to avoid.
  Signing now goes through fastlane `match`, which creates the certificate on the runner from the App
  Store Connect API key and stores it in a **private certificates repo shared across your Apple
  account**. It is a separate repo on purpose: a distribution certificate belongs to the account and
  Apple issues at most two, so storing them per app exhausts the account almost immediately. Releases
  run `match` read-only so no build can mint one. Archive and export also disagreed about signing
  style, and signing settings had to move per target because Swift Package dependencies reject a
  provisioning profile; the workflow now drives the project's own Fastfile instead of a parallel
  xcodebuild. Manual: set `APPSTORE_KEY_ID`, `APPSTORE_ISSUER_ID`, `APPSTORE_PRIVATE_KEY` (the
  **base64** of the `.p8`, not its raw contents), `MATCH_PASSWORD`, `MATCH_GIT_URL` and
  `MATCH_GIT_BASIC_AUTHORIZATION` (base64 of `x-access-token:<PAT>` with access to the certs repo).
  `IOS_APP_CERTIFICATE_P12_BASE64`, `APPSTORE_TEAM_ID` and the provisioning-profile UUIDs are no
  longer used — delete them.
- `[app]` **Android releases and PR checks write every build key** (#48): both workflows wrote the same
  stale three-key `local.properties` block, two of which the build no longer reads, so Play Store
  releases shipped with placeholder sign-in, ads, AI and paywall exactly like iOS did. All three
  workflows now derive the key list from `MobileApp/local.properties.example`, so adding a key there
  needs no workflow edit. Manual: set a repo secret for each key you use — the release workflows warn
  about any that are missing.
- `[docs]` **Publishing docs match the new secrets** (#48): `setup-signing`, the `publishing` guide and
  its progress template, the docs-site CI page and the iOS production page all still described the
  hand-exported `.p12` flow and two RevenueCat keys the build no longer reads. They now list the six
  iOS secrets that exist and where each value comes from, explain why the certificates repo is shared
  across the account, and state the rule the workflows rely on: every key in
  `local.properties.example` needs a repo secret of the same name.
- `[app]` **The certificate-store bootstrap is actually reachable** (#49): populating an empty
  certificates repo needs `match` to run with write access exactly once, and that escape hatch never
  worked. The workflow did not forward `MATCH_READONLY` into the build step, and the lane read it
  *after* fastlane's `setup_ci`, which sets `MATCH_READONLY=true` itself — so the log printed `false`
  while match still refused with "cannot create a new one because you enabled `readonly`". The value
  is now read before `setup_ci`, passed to match explicitly, and sourced from a repository
  **variable**, since a secret valued `false` masks that word throughout the log. Manual: bootstrap an
  empty certs repo with `gh variable set MATCH_READONLY --body false`, run the release once, then
  `gh variable delete MATCH_READONLY`. `match` is also pinned to the `main` branch of the certs repo:
  it defaults to `master`, which would leave the certificate on a second branch of a repo whose
  default is `main`.

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
