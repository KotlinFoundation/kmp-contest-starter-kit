#!/usr/bin/env bash
#
# Refactor the app package name / applicationId / bundle ID / display name.
# Bash port of the former `./gradlew refactorPackage` task.
#
# Usage (run from MobileApp/):
#   ./scripts/refactor_package.sh --app-id <id> --app-name <name> [options]
#
# Examples:
#   # Full refactor — Kotlin packages + applicationId + bundle ID + app name:
#   ./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp
#
#   # IDs + app name only, keep Kotlin packages/dirs (fewer merge conflicts when
#   # spinning multiple apps off one base):
#   ./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp --skip-package-rename
#
# Options:
#   --app-id <id>           New applicationId / bundle ID (required).
#   --app-name <name>       New app display name (required).
#   --skip-package-rename   Keep Kotlin packages/dirs. Only update applicationId,
#                           bundle ID, Firebase/Google-services refs, and app name
#                           (default: off — packages ARE renamed).
#   --old-app-id <id>       Current app id to replace. Default: auto-detected from the
#                           androidApp `namespace`. Required if detection fails.
#   --old-app-name <name>   Current app name to replace. Default: auto-detected from
#                           settings.gradle.kts `rootProject.name`. Required if detection
#                           fails.
#   -y, --yes               Skip the confirmation prompt.
#   -h, --help              Show this help.
#
# (Positional `<newAppId> <newAppName>` are still accepted as a fallback.)
#
# Notes:
#   - Edits files in place and is irreversible — commit or back up first.
#   - The old app id / name are auto-detected from the project, so re-refactoring an
#     already-renamed project just works. If detection can't find them the script aborts
#     rather than guessing — pass --old-app-id / --old-app-name to proceed.
#   - Idempotent: re-running with the same args is a no-op once applied.
#   - Requires `perl` (preinstalled on macOS and CI Linux).

set -euo pipefail

# ----------------------------------------------------------------------------- paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"   # MobileApp/
REPO_ROOT="$(cd "$MOBILE_DIR/.." && pwd)"    # repo root (holds .github/)

# ----------------------------------------------------------------------------- defaults
OLD_APP_ID=""        # auto-detected from the project if not passed via --old-app-id
OLD_APP_NAME=""      # auto-detected from the project if not passed via --old-app-name
NEW_APP_ID=""
NEW_APP_NAME=""
SKIP_PACKAGE_RENAME=false
ASSUME_YES=false

# Print the leading comment block (everything after the shebang up to the first code line).
usage() { awk 'NR>1 && /^#/{sub(/^# ?/,""); print; next} NR>1{exit}' "${BASH_SOURCE[0]}"; }

# Current package id = the android module `namespace` (tracks the Kotlin package even after a
# previous --skip-package-rename, which only changes applicationId).
detect_old_app_id() {
  local f file ns
  for f in androidApp/build.gradle.kts composeApp/build.gradle.kts; do
    file="$MOBILE_DIR/$f"
    [ -f "$file" ] || continue
    ns="$(grep -oE 'namespace[[:space:]]*=[[:space:]]*"[^"]+"' "$file" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
    [ -n "$ns" ] && { printf '%s' "$ns"; return 0; }
  done
  return 1   # not found — caller errors out rather than guessing
}

# Current display/app name = settings.gradle.kts `rootProject.name`.
detect_old_app_name() {
  local file="$MOBILE_DIR/settings.gradle.kts" n
  if [ -f "$file" ]; then
    n="$(grep -oE 'rootProject\.name[[:space:]]*=[[:space:]]*"[^"]+"' "$file" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
    [ -n "$n" ] && { printf '%s' "$n"; return 0; }
  fi
  return 1   # not found — caller errors out rather than guessing
}

# ----------------------------------------------------------------------------- arg parsing
POSITIONAL=()
while [ $# -gt 0 ]; do
  case "$1" in
    --app-id) NEW_APP_ID="${2:?--app-id needs a value}"; shift 2 ;;
    --app-name) NEW_APP_NAME="${2:?--app-name needs a value}"; shift 2 ;;
    --skip-package-rename) SKIP_PACKAGE_RENAME=true; shift ;;
    --old-app-id) OLD_APP_ID="${2:?--old-app-id needs a value}"; shift 2 ;;
    --old-app-name) OLD_APP_NAME="${2:?--old-app-name needs a value}"; shift 2 ;;
    -y|--yes) ASSUME_YES=true; shift ;;
    -h|--help) usage; exit 0 ;;
    -*) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    *) POSITIONAL+=("$1"); shift ;;
  esac
done

# Positional fallback: only used when the matching named flag was not given.
[ -z "$NEW_APP_ID" ] && NEW_APP_ID="${POSITIONAL[0]:-}"
[ -z "$NEW_APP_NAME" ] && NEW_APP_NAME="${POSITIONAL[1]:-}"

# Auto-detect the values being replaced from the project unless overridden by --old-*.
# If detection fails, error out instead of guessing a default.
[ -z "$OLD_APP_ID" ] && OLD_APP_ID="$(detect_old_app_id || true)"
[ -z "$OLD_APP_NAME" ] && OLD_APP_NAME="$(detect_old_app_name || true)"

if [ -z "$OLD_APP_ID" ]; then
  echo "Error: could not detect the current app id (no 'namespace' in androidApp/build.gradle.kts)." >&2
  echo "       Pass it explicitly: --old-app-id <id>" >&2
  exit 1
fi
if [ -z "$OLD_APP_NAME" ]; then
  echo "Error: could not detect the current app name (no 'rootProject.name' in settings.gradle.kts)." >&2
  echo "       Pass it explicitly: --old-app-name <name>" >&2
  exit 1
fi

# ----------------------------------------------------------------------------- validation
if [ -z "$NEW_APP_ID" ] || [ -z "$NEW_APP_NAME" ]; then
  echo "Error: --app-id and --app-name are required." >&2
  usage
  exit 1
fi

id_re='^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$'
if ! [[ "$NEW_APP_ID" =~ $id_re ]]; then
  echo "Error: newAppId '$NEW_APP_ID' is not a valid package id." >&2
  echo "       Expected lowercase, dot-separated segments, e.g. com.example.myapp" >&2
  exit 1
fi

if ! command -v perl >/dev/null 2>&1; then
  echo "Error: 'perl' is required but not found on PATH." >&2
  exit 1
fi

if [ "$SKIP_PACKAGE_RENAME" = false ] && [ "$NEW_APP_ID" = "$OLD_APP_ID" ]; then
  echo "Error: newAppId equals the current app id ($OLD_APP_ID) — nothing to rename." >&2
  echo "       Use --old-app-id if re-refactoring an already-renamed project." >&2
  exit 1
fi

# ----------------------------------------------------------------------------- confirm
echo "Refactor plan:"
echo "  app id:   $OLD_APP_ID  ->  $NEW_APP_ID"
echo "  app name: $OLD_APP_NAME  ->  $NEW_APP_NAME"
echo "  mode:     $([ "$SKIP_PACKAGE_RENAME" = true ] && echo 'IDs + app name only (keep Kotlin packages)' || echo 'full (rename Kotlin packages too)')"
echo "  target:   $MOBILE_DIR"
echo "  This edits files in place and is irreversible — make sure your work is committed."
if [ "$ASSUME_YES" = false ]; then
  if [ ! -t 0 ]; then
    echo "Error: non-interactive shell — re-run with -y/--yes to proceed." >&2
    exit 1
  fi
  printf "Proceed? [y/N] "
  read -r reply
  case "$reply" in y|Y|yes|YES) ;; *) echo "Aborted."; exit 0 ;; esac
fi

# ----------------------------------------------------------------------------- config
SOURCE_DIRS=(
  "src/commonMain/kotlin"
  "src/commonTest/kotlin"
  "src/androidMain/kotlin"
  "src/androidHostTest/kotlin"
  "src/iosMain/kotlin"
  "src/jvmMain/kotlin"
  "src/jvmTest/kotlin"
  "src/nonMobileMain/kotlin"
  "src/mobileMain/kotlin"
  "src/webMain/kotlin"
  "src/nonWebMain/kotlin"
  "src/jsMain/kotlin"
  "src/wasmJsMain/kotlin"
  "src/main/kotlin"           # non-KMP modules (e.g. :androidApp standard layout)
)

# `composeApp` is the pre-AGP-9 module name; kept so the script also works on legacy templates.
REFACTOR_MODULES=(
  "shared"
  "composeApp"
  "androidApp"
  "desktopApp"
  "webApp"
  "designsystem"
  "libs/auth/auth-api"
  "libs/auth/auth-firebase"
  "libs/subscription/subscription-api"
  "libs/subscription/subscription-adapty"
  "libs/subscription/subscription-revenuecat"
)

OLD_PKG_PATH="${OLD_APP_ID//.//}"
NEW_PKG_PATH="${NEW_APP_ID//.//}"
OLD_LAST="${OLD_APP_ID##*.}"   # last package segment, used as the Gradle task group label
NEW_LAST="${NEW_APP_ID##*.}"

# ----------------------------------------------------------------------------- helpers

# replace_in_file <file> <old> <new> — literal (non-regex) replace of every occurrence.
replace_in_file() {
  local file="$1" old="$2" new="$3"
  [ -f "$file" ] || return 0
  if grep -qF -- "$old" "$file"; then
    OLD="$old" NEW="$new" perl -i -pe 'BEGIN{$o=$ENV{OLD};$n=$ENV{NEW}} s/\Q$o\E/$n/g' "$file"
    echo "  updated: ${file#"$REPO_ROOT"/}"
  fi
}

update_package_names_in_files() {
  local module_dir="$1" sd dir
  for sd in "${SOURCE_DIRS[@]}"; do
    dir="$module_dir/$sd"
    [ -d "$dir" ] || continue
    while IFS= read -r -d '' f; do
      replace_in_file "$f" "$OLD_APP_ID" "$NEW_APP_ID"
    done < <(find "$dir" -type f \( -name '*.kt' -o -name '*.kts' -o -name '*.gradle' -o -name '*.xml' -o -name '*.json' \) -print0)
  done
}

move_package_directories() {
  local module_dir="$1" sd src old_path new_path
  for sd in "${SOURCE_DIRS[@]}"; do
    src="$module_dir/$sd"
    [ -d "$src" ] || continue
    old_path="$src/$OLD_PKG_PATH"
    new_path="$src/$NEW_PKG_PATH"
    [ -d "$old_path" ] || continue
    if [ -e "$new_path" ]; then
      cp -R "$old_path/." "$new_path/"
      rm -rf "$old_path"
      echo "  merged: ${old_path#"$REPO_ROOT"/} -> ${new_path#"$REPO_ROOT"/}"
    else
      mkdir -p "$(dirname "$new_path")"
      mv "$old_path" "$new_path"
      echo "  moved: ${old_path#"$REPO_ROOT"/} -> ${new_path#"$REPO_ROOT"/}"
    fi
    # Prune empty leftover ancestor dirs from the old package path (e.g. com/measify).
    find "$src" -type d -empty -delete 2>/dev/null || true
  done
}

# Roborazzi PNGs embed the FQCN in their filename; rename them so verify finds its goldens.
rename_roborazzi_snapshots() {
  local module_dir="$1" snap_dir="$1/src/androidHostTest/snapshots" f base new
  [ -d "$snap_dir" ] || return 0
  for f in "$snap_dir"/*; do
    [ -f "$f" ] || continue
    base="$(basename "$f")"
    case "$base" in
      *"$OLD_APP_ID"*)
        new="${base//$OLD_APP_ID/$NEW_APP_ID}"
        mv "$f" "$snap_dir/$new"
        echo "  renamed snapshot: $base -> $new"
        ;;
    esac
  done
}

# Room exports each @Database schema to schemas/<database-FQCN>/<version>.json. The directory name
# is the database class' fully-qualified name (dotted, e.g.
# com.kotlinfoundation.kmpstarterkit.data.source.local.AppDatabase), so move_package_directories —
# which only handles the slashed source path com/x/y — misses it. Rename the schema dir(s) so the
# exported FQCN matches the renamed @Database (otherwise the build regenerates a new dir and the old
# one lingers, tracked).
rename_room_schemas() {
  local module_dir="$1" schemas_dir="$1/schemas" d base new
  [ -d "$schemas_dir" ] || return 0
  for d in "$schemas_dir"/*; do
    [ -d "$d" ] || continue
    base="$(basename "$d")"
    case "$base" in
      *"$OLD_APP_ID"*)
        new="${base//$OLD_APP_ID/$NEW_APP_ID}"
        [ -e "$schemas_dir/$new" ] && { echo "  target exists, skipping schema move: $new"; continue; }
        mv "$d" "$schemas_dir/$new"
        echo "  renamed room schema: $base -> $new"
        ;;
    esac
  done
}

update_gradle_files() {
  local f
  for m in "${REFACTOR_MODULES[@]}"; do
    replace_in_file "$MOBILE_DIR/$m/build.gradle.kts" "$OLD_APP_ID" "$NEW_APP_ID"
  done
  for f in scripts/make_local.sh scripts/create_module.sh scripts/generate_screen.sh scripts/generate_store_screenshots.sh; do
    replace_in_file "$MOBILE_DIR/$f" "$OLD_APP_ID" "$NEW_APP_ID"
  done
}

# Custom Gradle tasks are grouped under the package's last segment (e.g. group = "kotlinfoundation").
# Rename that label to the new last segment. Only the exact `group = "<old-last>"` assignment is
# touched — the bare segment is too generic (also the brand name) to replace globally.
update_task_group() {
  [ "$OLD_LAST" = "$NEW_LAST" ] && return 0
  local m file
  for m in "${REFACTOR_MODULES[@]}"; do
    file="$MOBILE_DIR/$m/build.gradle.kts"
    [ -f "$file" ] || continue
    if grep -qE "group[[:space:]]*=[[:space:]]*\"$OLD_LAST\"" "$file"; then
      OLD="$OLD_LAST" NEW="$NEW_LAST" perl -i -pe 'BEGIN{$o=$ENV{OLD};$n=$ENV{NEW}} s/group(\s*=\s*)"\Q$o\E"/group$1"$n"/g' "$file"
      echo "  updated task group: ${file#"$REPO_ROOT"/} (\"$OLD_LAST\" -> \"$NEW_LAST\")"
    fi
  done
}

update_application_id_only() {
  local f
  for f in androidApp/build.gradle.kts composeApp/build.gradle.kts; do
    local file="$MOBILE_DIR/$f"
    [ -f "$file" ] || continue
    if grep -qE 'applicationId[[:space:]]*=' "$file"; then
      NEW="$NEW_APP_ID" perl -i -pe 'BEGIN{$n=$ENV{NEW}} s/applicationId\s*=\s*(["\x27])[^"\x27]+\1/applicationId = "$n"/g' "$file"
      echo "  updated applicationId: ${file#"$REPO_ROOT"/}"
    fi
  done
}

update_firebase_configs() {
  local f
  for f in androidApp/google-services.json composeApp/google-services.json iosApp/iosApp/GoogleService-Info.plist; do
    replace_in_file "$MOBILE_DIR/$f" "$OLD_APP_ID" "$NEW_APP_ID"
  done
}

update_ios_files() {
  replace_in_file "$MOBILE_DIR/iosApp/iosApp/Info.plist" "$OLD_APP_ID" "$NEW_APP_ID"
  replace_in_file "$MOBILE_DIR/iosApp/iosApp.xcodeproj/project.pbxproj" "$OLD_APP_ID" "$NEW_APP_ID"
}

# .github/ lives at the repo root (one level above MobileApp/); legacy templates nested it inside.
update_github_workflows() {
  local root f
  for root in "$REPO_ROOT" "$MOBILE_DIR"; do
    for f in .github/workflows/publish_android_playstore.yml .github/workflows/publish_ios_appstore.yml; do
      replace_in_file "$root/$f" "$OLD_APP_ID" "$NEW_APP_ID"
    done
  done
}

# update_app_name <current_pkg_id> — current_pkg_id is the id on disk (newAppId after a
# rename, oldAppId in skip mode) so package-path-dependent files resolve correctly.
update_app_name() {
  local current_pkg_id="$1" pkg_path="${1//.//}" f root
  local files=(
    "androidApp/src/main/AndroidManifest.xml"
    "desktopApp/src/main/kotlin/$pkg_path/Main.kt"
    "desktopApp/src/main/kotlin/$pkg_path/main.kt"
    "webApp/src/wasmJsMain/resources/index.html"
    "webApp/src/webMain/resources/index.html"
    "shared/src/jvmMain/kotlin/$pkg_path/util/AppUtilImpl.jvm.kt"
    "shared/src/webMain/kotlin/$pkg_path/util/AppUtilImpl.web.kt"
    "settings.gradle.kts"
    "iosApp/iosApp.xcodeproj/project.pbxproj"
  )
  for f in "${files[@]}"; do
    replace_in_file "$MOBILE_DIR/$f" "$OLD_APP_NAME" "$NEW_APP_NAME"
  done
  for root in "$REPO_ROOT" "$MOBILE_DIR"; do
    replace_in_file "$root/.github/workflows/publish_ios_appstore.yml" "$OLD_APP_NAME" "$NEW_APP_NAME"
  done
}

# Docs, agent guidelines, and script header comments reference the package both as a dotted id
# (`com.kotlinfoundation.kmpstarterkit`) and as a slashed path (`com/kotlinfoundation/kmpstarterkit`, e.g. example file
# locations). Source files only use the dotted form; these prose/comment files use both, so update
# both. Full-rename mode only — in skip mode the package (and its paths) stay put.
update_doc_references() {
  local f dir self
  self="$(cd "$SCRIPT_DIR" && pwd)/$(basename "${BASH_SOURCE[0]}")"
  local files=(
    "$REPO_ROOT/README.md"
    "$REPO_ROOT/AGENTS.md"
    "$MOBILE_DIR/README.md"
  )
  for f in "$MOBILE_DIR"/scripts/*.sh; do files+=("$f"); done
  for dir in "$REPO_ROOT/skills" "$REPO_ROOT/AiGuidelines"; do
    [ -d "$dir" ] || continue
    while IFS= read -r -d '' f; do files+=("$f"); done < <(find "$dir" -type f -name '*.md' -print0)
  done
  for f in "${files[@]}"; do
    [ "$f" = "$self" ] && continue   # never rewrite this script's own examples/defaults
    replace_in_file "$f" "$OLD_APP_ID" "$NEW_APP_ID"        # dotted id (com.x.y)
    replace_in_file "$f" "$OLD_PKG_PATH" "$NEW_PKG_PATH"    # slashed package path (com/x/y)
  done
}

# ----------------------------------------------------------------------------- run
echo
echo "Starting package refactor from $OLD_APP_ID to $NEW_APP_ID ..."

if [ "$SKIP_PACKAGE_RENAME" = false ]; then
  for m in "${REFACTOR_MODULES[@]}"; do
    module_dir="$MOBILE_DIR/$m"
    [ -d "$module_dir" ] || continue
    echo "Module: $m"
    update_package_names_in_files "$module_dir"
    move_package_directories "$module_dir"
    rename_roborazzi_snapshots "$module_dir"
    rename_room_schemas "$module_dir"
  done
  update_gradle_files
  update_task_group
  update_firebase_configs
  update_ios_files
  update_github_workflows
  update_app_name "$NEW_APP_ID"
  update_doc_references
else
  echo "Skip-package-rename mode: updating IDs + app name only."
  update_application_id_only
  update_firebase_configs
  update_ios_files
  update_github_workflows
  update_app_name "$OLD_APP_ID"
fi

echo
echo "✅ Package refactoring completed."
echo "   Tip: re-sync Gradle and rebuild. If you use Firebase, replace google-services.json /"
echo "   GoogleService-Info.plist with configs for the new app id."
