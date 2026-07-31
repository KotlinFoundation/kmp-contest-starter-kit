---
name: refactor-package
description: Rename the app's package / applicationId / iOS bundle ID and display name across the whole project. Use when the user asks to rebrand, change the app id / bundle id / package name, set the app display name, or spin a new app off the boilerplate.
---

# Refactor package / app id

Run from `MobileApp/`:

```bash
# Full refactor — renames Kotlin packages + applicationId + bundle ID + app name:
./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp

# IDs + display name only, keep Kotlin packages/dirs (fewer merge conflicts when
# spinning multiple apps off one base):
./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp --skip-package-rename
```

`--app-id` and `--app-name` are required. The script rewrites Kotlin packages/dirs, Gradle files (including the custom task-group label, derived from the package's last segment), Firebase/Google-services configs, iOS `Info.plist` / `project.pbxproj`, the repo-root GitHub publish workflows, the helper scripts, and the package references in docs/guidelines (READMEs, `AGENTS.md`, `skills/`, `AiGuidelines/`) — covering both the dotted id (`com.x.y`) and the slashed path (`com/x/y`) forms. It also renames Roborazzi snapshot goldens.

## Rules

- **Commit or back up first** — it edits files in place and is irreversible. It prints a plan and asks for confirmation; pass `-y` only when the user has confirmed or for non-interactive use.
- `--app-id` must be a **lowercase**, dot-separated package id (e.g. `com.example.myapp`). The display name (`--app-name`) may have capitals/spaces.
- The values being replaced are **auto-detected** (old id ← androidApp `namespace`; old name ← `rootProject.name` in `settings.gradle.kts`), so re-refactoring an already-renamed project just works. If detection can't find them the script **aborts** rather than guessing — only then pass `--old-app-id` / `--old-app-name`.
- Idempotent: re-running with the same args is a no-op once applied.
- Requires `perl` (preinstalled on macOS / CI Linux).

## After running

1. Re-sync Gradle and rebuild (`./gradlew :androidApp:assembleDebug`) — see [run-quality-gates](../run-quality-gates/SKILL.md).
2. If the app uses Firebase, replace `androidApp/google-services.json` and `iosApp/iosApp/GoogleService-Info.plist` with configs downloaded for the **new** app id (the script only rewrites the old id string inside the existing placeholders).

---

*Used for the Phase 1 rebrand in the [getting-started](../getting-started/SKILL.md) guide, and again in the Phase 3 [publishing](../publishing/SKILL.md) guide.*
