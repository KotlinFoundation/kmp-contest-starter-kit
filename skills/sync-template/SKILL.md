---
name: sync-template
description: Pull KMPStarterKit template updates into a derived (renamed) app — vendor-branch sync via sync_template.sh, conflict resolution rules, and the CHANGELOG as context. Use when the developer wants template updates, to "sync with the starter kit / base repo / upstream", or asks how to get a new template feature into an app created from this kit.
---

# Sync a derived app with the template

Apps created from this kit run `refactor_package.sh`, which renames the package in ~every file —
after that a plain `git merge` against the template drowns in rename noise. This skill restores a
real merge base by **renaming the incoming template tree instead** (the vendor-branch pattern),
so a sync only surfaces genuine feature changes plus the places the app itself edited.

Everything is driven by **`MobileApp/scripts/sync_template.sh`**; this skill is the operating
manual around it. Read [`CHANGELOG.md`](../../CHANGELOG.md) (repo root) first — it tells you what
changed since the version recorded in the app's `.template-version`, which is exactly the context
you need to resolve conflicts well.

## How it works (so you can debug it)

- A local **`template-base`** branch holds "renders": the template tree at a given commit with
  `refactor_package.sh --app-id <this app>` applied. Renders are chained commits, so git always
  has the previous render as the merge base.
- Each sync runs on a fresh **`template-sync/<sha>` work branch** cut from the current branch —
  the app's own branch is never touched until the developer merges the work branch back. On it:
  new render on `template-base` → `git merge template-base`. Both sides use the app's package
  name; rename noise cancels out.
- `.template-version` at the repo root records the template commit last synced.
- The first run **bootstraps** ancestry: renders the template commit the app was created from and
  merges it with `-s ours` (tree unchanged — it only links history).

## 1. One-time setup — **Agent Action**

```bash
git remote add template https://github.com/KotlinFoundation/kmp-contest-starter-kit.git
```

## 2. Run the sync — **Agent Action**

From the repo root (any directory works — the script cds itself):

```bash
./MobileApp/scripts/sync_template.sh
```

Requirements: clean working tree, on the branch that should receive the update.

**First run only:** if the script can't derive the starting template commit (histories are
unrelated when the app came from "Use this template" or a squashed clone), it asks for
`--bootstrap <sha>` — the template commit the app was created from. Find it by creation date:

```bash
git log template/main --until="<date the app was created>" -1 --format='%H %s'
```

An approximate **older** commit is fine — a slightly-off base only means a few more conflicts in
the first merge, nothing is lost or corrupted.

## 3. Resolve conflicts — **Agent Action**

Conflicts appear only where the app edited the same code the template changed. Resolution rules:

- **App's product code wins, template's infrastructure wins.** The developer's screens, models,
  and copy stay; template changes to scripts, gradle, CI, skills, designsystem internals come in.
  When a hunk mixes both, apply the template's *intent* to the app's version by hand — the
  CHANGELOG entry for that release says what the intent was.
- **`composeResources/values/strings.xml`** — merge both sides (union); the app's reworded strings
  beat the template's copy for the same key.
- **Shared wiring files** (`Routes.kt`, `AppNavigation.kt`, `root/Di.kt`, `AppDatabase.kt`,
  `DatabaseModule.kt`) — keep the app's entries AND add the template's new ones; these files are
  append-mostly around the insertion markers.
- **`Documentation` submodule gitlink** — if the app repointed the submodule to its own docs repo,
  keep the app's side.
- **Demo screens the app deleted or fully replaced** (Home, onboarding variants) — keep the
  deletion/replacement; don't resurrect template demo code.

Then:

```bash
git add -A && git commit
```

## 4. Validate, then merge the work branch — **Validation → Agent Action**

Still on the `template-sync/<sha>` branch, run the **`run-quality-gates`** skill from `MobileApp/`
(spotless, the scoped tests, `assembleDebug`). A sync is not done until the gates pass — template
changes can compile against demo code the app no longer has.

Only when green, land it:

```bash
git checkout <your-branch> && git merge template-sync/<sha> && git branch -d template-sync/<sha>
```

If the sync goes wrong at any point, the app's branch is untouched — just delete the work branch
(`git branch -D template-sync/<sha>`) and re-run later. Keep `template-base`; it's reusable.

## For template maintainers

Every feature PR on the template should add a `CHANGELOG.md` entry (see its header for the
format) — one or two lines: what changed, which files, and anything a derived app must do by hand.
That entry is what makes step 3 nearly automatic for the agents syncing the ~N apps downstream.

## Limits

- The sync is **whole-template**: it brings everything since the last sync. To take a single
  feature only, use the CHANGELOG entry + `git diff` of that PR against `template/main` and apply
  it manually (agent-cherry-pick); the next full sync will then merge cleanly over it.
- The bootstrap commit must already contain `scripts/refactor_package.sh` (any 2025+ template
  version does).

## Heavily diverged apps (rewrote navigation / DI / storage / structure)

Merging degrades with architectural divergence — it stays *safe* (work branch; abort with
`git branch -D template-sync/<sha>` and nothing happened), but the output shifts from auto-merge
to porting work: wholesale-rewritten files conflict end-to-end, deleted subsystems come back as
modify/delete conflicts (keep the deletion), and template features wired to the old architecture
can merge cleanly yet fail to compile — which is exactly what the mandatory quality-gates step
catches before anything reaches the app's branch.

Past that point, stop merging and **port instead**: the render diff

```bash
git diff template-base~1 template-base
```

is precisely what the template changed, already expressed in the app's package names — apply it
(with the CHANGELOG entry as intent) to the app's architecture by hand, then delete the work
branch. Judge per sync: lightly diverged → merge and resolve; heavily diverged → rendered diff +
CHANGELOG as a port source.
