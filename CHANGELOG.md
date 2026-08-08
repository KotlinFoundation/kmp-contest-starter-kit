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
