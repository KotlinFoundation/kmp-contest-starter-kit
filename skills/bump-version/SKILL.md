---
name: bump-version
description: Bump Android and iOS versionCode/versionName together for a release. Use when the user asks to bump, increment, or set the app version, or as part of preparing a release.
---

# Bump app version

Run from `MobileApp/`:

```bash
# Increment versionCode on both platforms + bump the patch version (x.y.Z+1):
./scripts/update_version.sh

# Or set an explicit version name:
./scripts/update_version.sh -v 1.2.3
```

The script updates the Android `versionCode`/`versionName` (in `androidApp/build.gradle.kts`) and the iOS build number/marketing version together, so the platforms never drift.

## After running

1. Show the user the resulting versions (the script prints them).
2. Releases are tag-driven: pushing a `*-android` tag triggers `.github/workflows/publish_android_playstore.yml`, a `*-ios` tag triggers `publish_ios_appstore.yml`. Don't push tags unless the user asks for a release.

---

*Phase 3 · Publication — part of the [publishing](../publishing/SKILL.md) guide.*
