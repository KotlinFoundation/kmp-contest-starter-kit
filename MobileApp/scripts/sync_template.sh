#!/usr/bin/env bash
#
# sync_template.sh — pull KMPStarterKit template updates into a derived (renamed) app.
#
# Usage (run from anywhere inside the repo):
#   ./scripts/sync_template.sh [options]
#
# Options:
#   --remote <name>      Git remote pointing at the template repo. Default: template
#   --branch <name>      Template branch to sync from. Default: main
#   --bootstrap <sha>    Template commit your app was originally created from. Required the
#                        FIRST time only (when no template-base branch exists) and only if it
#                        cannot be derived via git merge-base.
#   --app-id <id>        Override the derived app id. Default: auto-detected from the
#                        androidApp `namespace`.
#   --app-name <name>    Override the derived app name. Default: auto-detected from
#                        settings.gradle.kts `rootProject.name`.
#   -y, --yes            Skip the confirmation prompt.
#   -h, --help           Show this help.
#
# What it does (the "vendor branch" pattern):
#   The package rename (`refactor_package.sh`) normally destroys the merge base with the
#   template — every file differs, so `git merge` drowns in rename noise. This script fixes
#   that by renaming the INCOMING template tree instead:
#
#   1. Keeps a `template-base` branch whose commits are "template@<sha>, rendered as your
#      app" — i.e. the template tree with `refactor_package.sh --app-id <yours>` applied.
#   2. Each sync runs on a fresh work branch `template-sync/<sha>` cut from your current
#      branch — your branch is never touched. It adds a new render commit on `template-base`
#      (tree = new template release, renamed), then merges `template-base` into the work
#      branch. Git three-way-merges with the PREVIOUS render as the base, so both sides speak
#      your package name and only real feature changes (and your own edits) remain. Resolve /
#      validate on the work branch, then merge it into your branch when green.
#   3. Records the synced template commit in `.template-version` at the repo root.
#
#   First run bootstraps ancestry: it renders the template commit your app started from and
#   merges it with `-s ours` (no tree change) so later merges have a proper base.
#
# Requirements: clean working tree, a `template` remote
#   (git remote add template https://github.com/KotlinFoundation/kmp-contest-starter-kit.git),
#   and the bootstrap commit must already contain scripts/refactor_package.sh.
#
# On merge conflicts the script stops with the merge in progress — resolve and commit
# (see skills/sync-template/SKILL.md for the resolution rules), then run the quality gates.

set -euo pipefail

REMOTE="template"
BRANCH="main"
BOOTSTRAP=""
APP_ID=""
APP_NAME=""
ASSUME_YES=false
BASE_BRANCH="template-base"
VERSION_FILE=".template-version"

usage() { awk 'NR>1 && /^#/{sub(/^# ?/,""); print; next} NR>1{exit}' "${BASH_SOURCE[0]}"; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --remote) REMOTE="$2"; shift 2 ;;
    --branch) BRANCH="$2"; shift 2 ;;
    --bootstrap) BOOTSTRAP="$2"; shift 2 ;;
    --app-id) APP_ID="$2"; shift 2 ;;
    --app-name) APP_NAME="$2"; shift 2 ;;
    -y|--yes) ASSUME_YES=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

fail() { echo "Error: $*" >&2; exit 1; }

# --- Preconditions -------------------------------------------------------------------------

git rev-parse -q --verify MERGE_HEAD >/dev/null && fail "a merge is already in progress — resolve or abort it first."
[[ -z "$(git status --porcelain -uno)" ]] || fail "working tree has uncommitted changes — commit or stash them first."

CUR_BRANCH="$(git branch --show-current)"
[[ -n "$CUR_BRANCH" ]] || fail "detached HEAD — check out the branch you want to sync into."

git remote get-url "$REMOTE" >/dev/null 2>&1 || fail "no '$REMOTE' remote. Add it first:
  git remote add $REMOTE https://github.com/KotlinFoundation/kmp-contest-starter-kit.git"

echo "Fetching $REMOTE…"
git fetch "$REMOTE" "$BRANCH"
TARGET="$(git rev-parse "$REMOTE/$BRANCH")"
TARGET_SHORT="$(git rev-parse --short "$TARGET")"

# --- Detect the derived app's identity (before we start switching trees) -------------------

if [[ -z "$APP_ID" ]]; then
  APP_ID="$(grep -oE 'namespace[[:space:]]*=[[:space:]]*"[^"]+"' MobileApp/androidApp/build.gradle.kts \
    | head -1 | sed -E 's/.*"([^"]+)".*/\1/')" || true
fi
[[ -n "$APP_ID" ]] || fail "could not detect the app id (androidApp namespace). Pass --app-id."

if [[ -z "$APP_NAME" ]]; then
  APP_NAME="$(grep -oE 'rootProject\.name[[:space:]]*=[[:space:]]*"[^"]+"' MobileApp/settings.gradle.kts \
    | head -1 | sed -E 's/.*"([^"]+)".*/\1/')" || true
fi
[[ -n "$APP_NAME" ]] || fail "could not detect the app name (rootProject.name). Pass --app-name."

if [[ -f "$VERSION_FILE" && "$(cat "$VERSION_FILE")" == "$TARGET" ]]; then
  echo "Already up to date with $REMOTE/$BRANCH ($TARGET_SHORT)."
  exit 0
fi

HAVE_BASE=false
git rev-parse -q --verify "$BASE_BRANCH" >/dev/null && HAVE_BASE=true

# The whole sync happens on a work branch, never on the user's branch — conflicts get resolved
# and validated there, and the user merges it back (or deletes it) when green.
SYNC_BRANCH="template-sync/$TARGET_SHORT"
git rev-parse -q --verify "$SYNC_BRANCH" >/dev/null \
  && fail "branch '$SYNC_BRANCH' already exists — finish that sync (merge it into $CUR_BRANCH) or delete it first."

echo
echo "Sync plan:"
echo "  App:            $APP_ID ($APP_NAME)"
echo "  Template:       $REMOTE/$BRANCH @ $TARGET_SHORT"
echo "  Work branch:    $SYNC_BRANCH (from $CUR_BRANCH; merge back when green)"
if ! $HAVE_BASE; then
  echo "  Bootstrap:      yes (no '$BASE_BRANCH' branch yet)"
fi
echo
if ! $ASSUME_YES; then
  read -r -p "Continue? [y/N] " reply
  [[ "$reply" == "y" || "$reply" == "Y" ]] || { echo "Aborted."; exit 1; }
fi

git checkout -q -b "$SYNC_BRANCH"

# --- Render helper: make the current worktree = <template sha> renamed as this app ---------

render() {
  local sha="$1"
  git read-tree -u --reset "$sha"

  # Skip the rename when the tree already uses this app id (template repo itself, or an
  # app that never ran the refactor).
  local tree_id
  tree_id="$(grep -oE 'namespace[[:space:]]*=[[:space:]]*"[^"]+"' MobileApp/androidApp/build.gradle.kts \
    | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
  if [[ "$tree_id" != "$APP_ID" ]]; then
    (cd MobileApp && ./scripts/refactor_package.sh --app-id "$APP_ID" --app-name "$APP_NAME" -y)
  fi

  git rev-parse "$sha" > "$VERSION_FILE"
  git add -A
}

# --- Bootstrap: create template-base at the commit the app was created from ----------------

if ! $HAVE_BASE; then
  if [[ -z "$BOOTSTRAP" ]]; then
    BOOTSTRAP="$(git merge-base HEAD "$TARGET" 2>/dev/null || true)"
  fi
  [[ -n "$BOOTSTRAP" ]] || fail "first sync needs --bootstrap <template-sha> (the template commit your app
was created from — histories are unrelated so it can't be derived). Find it e.g. by clone date:
  git log $REMOTE/$BRANCH --until='<when you created the app>' -1 --format='%H %s'
An approximate (older) commit works too — it only affects how many conflicts the first merge shows."
  git rev-parse -q --verify "$BOOTSTRAP^{commit}" >/dev/null || fail "--bootstrap '$BOOTSTRAP' is not a commit."

  echo "Bootstrapping $BASE_BRANCH from $(git rev-parse --short "$BOOTSTRAP")…"
  git checkout -b "$BASE_BRANCH" "$BOOTSTRAP"
  render "$BOOTSTRAP"
  git commit -q -m "Template render: $(git rev-parse --short "$BOOTSTRAP") as $APP_ID"
  git checkout -q "$SYNC_BRANCH"
  git merge -q -s ours --allow-unrelated-histories "$BASE_BRANCH" \
    -m "Link template ancestry (render of $(git rev-parse --short "$BOOTSTRAP"))"
  # Carry .template-version onto the app branch too — without it, the first real merge sees a
  # modify/delete conflict on the file ('-s ours' keeps the app tree, which doesn't have it yet).
  git rev-parse "$BOOTSTRAP" > "$VERSION_FILE"
  git add "$VERSION_FILE"
  git commit -q --amend --no-edit
  echo "Ancestry linked (your tree is unchanged apart from $VERSION_FILE)."
fi

# --- Update: new render on template-base, then a real merge --------------------------------

echo "Rendering $REMOTE/$BRANCH @ $TARGET_SHORT as $APP_ID…"
git checkout -q "$BASE_BRANCH"
render "$TARGET"
git commit -q -m "Template render: $TARGET_SHORT as $APP_ID"
git checkout -q "$SYNC_BRANCH"

echo "Merging into $SYNC_BRANCH…"
if git merge --no-ff "$BASE_BRANCH" -m "Sync template $TARGET_SHORT"; then
  echo
  echo "Done. '$SYNC_BRANCH' now has $REMOTE/$BRANCH @ $TARGET_SHORT."
  echo "Next:"
  echo "  1. Validate from MobileApp/: spotlessApply + the scoped quality gates (run-quality-gates)."
  echo "  2. When green:  git checkout $CUR_BRANCH && git merge $SYNC_BRANCH"
  echo "     (then delete the work branch: git branch -d $SYNC_BRANCH)"
  echo "Your '$CUR_BRANCH' branch is untouched until you merge."
else
  echo
  echo "Merge stopped on conflicts (on '$SYNC_BRANCH' — your '$CUR_BRANCH' branch is untouched)."
  echo "This is expected where your app edited the same code the template changed."
  echo "Resolve them (rules + agent guidance: skills/sync-template/SKILL.md), then:"
  echo "  git add -A && git commit"
  echo "Validate, and when green merge the work branch: git checkout $CUR_BRANCH && git merge $SYNC_BRANCH"
  exit 1
fi
