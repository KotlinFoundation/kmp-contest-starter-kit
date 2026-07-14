#!/usr/bin/env bash
#
# check_env.sh — report which environment keys/secrets are still placeholders for a given phase.
#
# The build deliberately does NOT fail on missing keys (getRequiredProperty substitutes "testValue"
# so the local app keeps building fast). That means a missing cloud key is invisible to an agent
# driving the build. This script makes it VISIBLE: it reads local.properties + gradle.properties +
# the relevant AppConfiguration.kt / FeatureFlagManager.kt flags and reports, per phase, which required keys
# are still placeholders — so the agent knows to stop and ask the developer.
#
# Usage (run from MobileApp/):
#   ./scripts/check_env.sh [--phase <name>] [--strict]
#
#   --phase <name>   getting-started | integrations | publishing | monetization | growth | all
#                    (default: all). A key is flagged ⚠️ only if REQUIRED by that phase.
#   --strict         exit non-zero when any required key is still a placeholder (default: exit 0).
#   -h, --help       show this help.
#
# Classification per key:
#   ✅ set            — a real value is present
#   ⚠️  MISSING       — required for this phase but still a placeholder ("testValue"/empty/boilerplate)
#   ⚪ optional/n-a   — not required for this phase (or only needed if you opt into a feature)
#
# Exit code is 0 unless --strict is passed and at least one ⚠️ was printed.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

LOCAL_PROPS="$MOBILE_DIR/local.properties"
GRADLE_PROPS="$MOBILE_DIR/gradle.properties"
# Per-app config (URLs, contact, AI routing, auth toggle) lives in root/AppConfiguration.kt.
APP_CONFIG="$MOBILE_DIR/shared/src/commonMain/kotlin/com/kotlinfoundation/koko/root/AppConfiguration.kt"
FEATURE_FLAGS="$MOBILE_DIR/shared/src/commonMain/kotlin/com/kotlinfoundation/koko/data/source/featureflag/FeatureFlagManager.kt"
GOOGLE_SERVICES="$MOBILE_DIR/androidApp/google-services.json"
GOOGLE_PLIST="$MOBILE_DIR/iosApp/iosApp/GoogleService-Info.plist"

PHASE="all"
STRICT=false

usage() { awk 'NR>1 && /^#/{sub(/^# ?/,""); print; next} NR>1{exit}' "${BASH_SOURCE[0]}"; }

while [ $# -gt 0 ]; do
  case "$1" in
    --phase) PHASE="${2:-}"; shift 2 ;;
    --strict) STRICT=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 2 ;;
  esac
done

case "$PHASE" in
  getting-started|integrations|publishing|monetization|growth|all) ;;
  *) echo "Invalid --phase '$PHASE'. Use: getting-started|integrations|publishing|monetization|growth|all" >&2; exit 2 ;;
esac

# ---------------------------------------------------------------------------- readers
# Read a key's value from a properties file (last assignment wins, trims surrounding whitespace).
prop() {
  local file="$1" key="$2" line
  [ -f "$file" ] || return 0
  line="$(grep -E "^[[:space:]]*$key[[:space:]]*=" "$file" | tail -1 || true)"
  [ -n "$line" ] || return 0
  printf '%s' "${line#*=}" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//'
}

# A local.properties value counts as a placeholder if it is empty or the sentinel "testValue".
is_placeholder() {
  local v="$1"
  [ -z "$v" ] || [ "$v" = "testValue" ]
}

# Read a `const val NAME = "..."` string literal from AppConfiguration.kt.
const_string() {
  local name="$1"
  [ -f "$APP_CONFIG" ] || return 0
  grep -E "val[[:space:]]+$name[[:space:]]*=" "$APP_CONFIG" | tail -1 | sed -E 's/.*=[[:space:]]*"([^"]*)".*/\1/' || true
}

# Read a `const val NAME = true|false` boolean from AppConfiguration.kt.
const_bool() {
  local name="$1"
  [ -f "$APP_CONFIG" ] || { echo "false"; return 0; }
  grep -E "val[[:space:]]+$name[[:space:]]*=" "$APP_CONFIG" | tail -1 | grep -qE '=[[:space:]]*true' && echo "true" || echo "false"
}

# Read a FeatureFlagManager DEFAULT_VALUES boolean (e.g. Keys.IS_ADS_ENABLED to true).
flag_default_bool() {
  local key="$1"
  [ -f "$FEATURE_FLAGS" ] || { echo "false"; return 0; }
  grep -E "Keys\.$key[[:space:]]+to[[:space:]]+" "$FEATURE_FLAGS" | tail -1 | grep -qE 'to[[:space:]]+true' && echo "true" || echo "false"
}

# Read a nullable Boolean const from AppConfiguration.kt → prints "true" | "false" | "null".
const_tristate() {
  local name="$1" line
  [ -f "$APP_CONFIG" ] || { echo "null"; return 0; }
  line="$(grep -E "val[[:space:]]+$name\b" "$APP_CONFIG" | tail -1)"
  echo "$line" | grep -qE '=[[:space:]]*true' && { echo "true"; return 0; }
  echo "$line" | grep -qE '=[[:space:]]*false' && { echo "false"; return 0; }
  echo "null"
}

# A local.properties value is a real (usable) key: non-empty and not the "testValue" placeholder.
key_is_set() { local v="$1"; [ -n "$v" ] && [ "$v" != "testValue" ]; }

# ---------------------------------------------------------------------------- state
SOCIAL_AUTH="$(const_bool AUTH_SOCIAL_LOGIN_ENABLED)"
ADS_ENABLED="$(flag_default_bool IS_ADS_ENABLED)"
CLOUD_URL="$(const_string CLOUD_FUNCTIONS_URL)"
PRIVACY_URL="$(const_string URL_PRIVACY_POLICY)"
TERMS_URL="$(const_string URL_TERMS_CONDITIONS)"
CONTACT_EMAIL="$(const_string CONTACT_EMAIL)"
APPSTORE_ID="$(const_string APPSTORE_APP_ID)"
PROVIDER="$(prop "$GRADLE_PROPS" SUBSCRIPTION_PROVIDER)"; PROVIDER="${PROVIDER:-ADAPTY}"
USE_AI_PROXY="$(const_tristate USE_AI_PROXY_SERVER)"
PREMIUM_ENABLED="$(const_bool PREMIUM_FEATURES_ENABLED)"
OPENAI_KEY="$(prop "$LOCAL_PROPS" OPENAI_API_KEY)"
REPLICATE_KEY="$(prop "$LOCAL_PROPS" REPLICATE_API_KEY)"
SUB_ANDROID_KEY="$(prop "$LOCAL_PROPS" SUBSCRIPTION_PROVIDER_ANDROID_API_KEY)"
SUB_IOS_KEY="$(prop "$LOCAL_PROPS" SUBSCRIPTION_PROVIDER_IOS_API_KEY)"

WARN_COUNT=0
phase_active() { [ "$PHASE" = "all" ] || [ "$PHASE" = "$1" ]; }

# Emit one line. $1 = tier (required|optional|na), $2 = label, $3 = state text, $4 = hint
row() {
  local tier="$1" label="$2" state="$3" hint="${4:-}"
  case "$tier" in
    required) printf '  ⚠️  %-34s %s\n' "$label" "$state"; [ -n "$hint" ] && printf '        ↳ %s\n' "$hint"; WARN_COUNT=$((WARN_COUNT+1)) ;;
    ok)       printf '  ✅ %-34s %s\n' "$label" "$state" ;;
    optional) printf '  ⚪ %-34s %s\n' "$label" "$state"; [ -n "$hint" ] && printf '        ↳ %s\n' "$hint" ;;
  esac
}

# Check a local.properties key that is REQUIRED for the active phase.
check_required_prop() {
  local key="$1" where="$2" v; v="$(prop "$LOCAL_PROPS" "$key")"
  if is_placeholder "$v"; then row required "$key" "MISSING (placeholder)" "get it from: $where"
  else row ok "$key" "set"; fi
}

# Check a local.properties key that is OPTIONAL for the active phase.
check_optional_prop() {
  local key="$1" note="$2" v; v="$(prop "$LOCAL_PROPS" "$key")"
  if is_placeholder "$v"; then row optional "$key" "not set" "$note"
  else row ok "$key" "set"; fi
}

echo "Environment check — phase: $PHASE"
echo

# ============================================================================ P1 getting-started
if phase_active getting-started; then
  echo "[P1] getting-started"
  check_required_prop sdk.dir "your machine's Android SDK path (~/Library/Android/sdk)"
  echo
fi

# ============================================================================ P2 integrations
if phase_active integrations; then
  echo "[P2] integrations"
  # Firebase config files — required (the committed ones are boilerplate: project_id == PROJECT_ID).
  if [ ! -f "$GOOGLE_SERVICES" ] || grep -q '"project_id": *"PROJECT_ID"' "$GOOGLE_SERVICES" 2>/dev/null; then
    row required "google-services.json" "MISSING/boilerplate" "download YOUR Firebase Android config -> androidApp/google-services.json"
  else
    row ok "google-services.json" "set"
  fi
  if [ ! -f "$GOOGLE_PLIST" ] || grep -q "PROJECT_ID" "$GOOGLE_PLIST" 2>/dev/null; then
    row required "GoogleService-Info.plist" "MISSING/boilerplate" "download YOUR Firebase iOS config -> iosApp/iosApp/GoogleService-Info.plist"
  else
    row ok "GoogleService-Info.plist" "set"
  fi
  # Social sign-in is opt-in. Required only when AUTH_SOCIAL_LOGIN_ENABLED = true.
  if [ "$SOCIAL_AUTH" = "true" ]; then
    check_required_prop GOOGLE_WEB_CLIENT_ID "Firebase -> Auth -> Google provider -> Web client ID"
    echo "        ↳ social auth is ON — also verify iOS Info.plist client IDs + the Xcode 'Sign In with Apple' capability"
  else
    row optional "GOOGLE_WEB_CLIENT_ID" "not needed" "anonymous auth is the default; set AUTH_SOCIAL_LOGIN_ENABLED = true to add Google/Apple"
  fi
  # AI web-proxy — optional unless you use it.
  if [ -z "$CLOUD_URL" ]; then
    row optional "CLOUD_FUNCTIONS_URL" "not set" "only if you use the AI web-proxy; set in AppConfiguration.kt after 'firebase deploy' (integrate-web-proxy)"
  else
    row ok "CLOUD_FUNCTIONS_URL" "set"
  fi
  # Subscriptions — optional commercial step in P2 (mandatory in P4).
  check_optional_prop SUBSCRIPTION_PROVIDER_ANDROID_API_KEY "optional now; required in monetization ($PROVIDER dashboard)"
  check_optional_prop SUBSCRIPTION_PROVIDER_IOS_API_KEY "optional now; required in monetization ($PROVIDER dashboard)"
  echo
fi

# ============================================================================ P3 publishing
if phase_active publishing; then
  echo "[P3] publishing"
  if [ -z "$PRIVACY_URL" ]; then row required "AppConfiguration.URL_PRIVACY_POLICY" "empty" "publish a privacy policy URL and set it in AppConfiguration.kt"; else row ok "AppConfiguration.URL_PRIVACY_POLICY" "set"; fi
  if [ -z "$TERMS_URL" ]; then row required "AppConfiguration.URL_TERMS_CONDITIONS" "empty" "publish a terms URL and set it in AppConfiguration.kt"; else row ok "AppConfiguration.URL_TERMS_CONDITIONS" "set"; fi
  # CONTACT_EMAIL ships as the boilerplate support@example.com — flag until it's your own.
  if [ -z "$CONTACT_EMAIL" ] || [ "$CONTACT_EMAIL" = "support@example.com" ]; then
    row required "AppConfiguration.CONTACT_EMAIL" "boilerplate/empty" "set your own support email in AppConfiguration.kt (still $CONTACT_EMAIL)"
  else row ok "AppConfiguration.CONTACT_EMAIL" "set"; fi
  # APPSTORE_APP_ID is assigned by App Store Connect — only knowable once the iOS app record exists.
  if [ -z "$APPSTORE_ID" ]; then
    row optional "AppConfiguration.APPSTORE_APP_ID" "not set" "numeric App Store id for rate/review + manage-subscription deep links; set it after App Store Connect creates the app"
  else row ok "AppConfiguration.APPSTORE_APP_ID" "set"; fi

  # AI backend — production must use the Firebase proxy so provider keys aren't compiled into the binary.
  ai_direct_key_set=false
  { key_is_set "$OPENAI_KEY" || key_is_set "$REPLICATE_KEY"; } && ai_direct_key_set=true
  if [ "$USE_AI_PROXY" = "false" ]; then
    row required "AI backend" "forced DIRECT" "AppConfiguration.USE_AI_PROXY_SERVER=false ships the API key in the app — set it to null/true and use the web-proxy for production (integrate-web-proxy)"
  elif [ -z "$CLOUD_URL" ] && [ "$ai_direct_key_set" = true ]; then
    row required "AI backend" "DIRECT (key on device)" "no CLOUD_FUNCTIONS_URL + a direct key set → providers are called directly with the key in the binary. Deploy the proxy + set CLOUD_FUNCTIONS_URL"
  elif [ "$ai_direct_key_set" = true ]; then
    row optional "AI backend" "direct key present" "proxy is active, but OPENAI/REPLICATE_API_KEY still compiles into the binary — clear it from local.properties for release builds"
  else
    row ok "AI backend" "proxy / none"
  fi

  # Subscriptions — real provider keys (not the demo mock) are needed to process purchases.
  # N/A when the app has no premium features (PREMIUM_FEATURES_ENABLED = false — everything is free).
  if [ "$PREMIUM_ENABLED" != "true" ]; then
    row optional "subscription keys" "n/a (no premium)" "PREMIUM_FEATURES_ENABLED = false — no premium features (all free); no subscriptions/paywall/credits"
  elif key_is_set "$SUB_ANDROID_KEY" || key_is_set "$SUB_IOS_KEY"; then
    row ok "subscription keys" "set"
  else
    row optional "subscription keys" "mock (unset)" "$PROVIDER SDK keys not set → the paywall runs the demo mock; set real keys before selling subscriptions/IAPs (setup-subscriptions)"
  fi

  row optional "signing" "not checked here" "keystore + store credentials stay local (gitignored) + backed up; add to GitHub Actions secrets only if publishing via CI — see the setup-signing skill"
  echo
fi

# ============================================================================ P4 monetization
if phase_active monetization; then
  echo "[P4] monetization (provider: $PROVIDER)"
  check_required_prop SUBSCRIPTION_PROVIDER_ANDROID_API_KEY "$PROVIDER dashboard -> API keys (public SDK key)"
  check_required_prop SUBSCRIPTION_PROVIDER_IOS_API_KEY "$PROVIDER dashboard -> API keys (public SDK key)"
  if [ "$ADS_ENABLED" = "true" ]; then
    check_required_prop ADMOB_APP_ID_ANDROID "Google AdMob console (https://apps.admob.com/)"
    for k in ADMOB_BANNER_AD_ID_ANDROID ADMOB_INTERSTITIAL_AD_ID_ANDROID ADMOB_REWARDED_AD_ID_ANDROID \
             ADMOB_BANNER_AD_ID_IOS ADMOB_INTERSTITIAL_AD_ID_IOS ADMOB_REWARDED_AD_ID_IOS; do
      check_required_prop "$k" "Google AdMob console"
    done
  else
    row optional "ADMOB_* ad ids" "ads off" "enable via FeatureFlagManager IS_ADS_ENABLED = true, then add AdMob ids (enable-ads)"
  fi
  echo
fi

# ============================================================================ P5 growth
if phase_active growth; then
  echo "[P5] growth"
  row optional "analytics / push" "no local keys" "Analytics/Crashlytics/RemoteConfig reuse the Firebase project; push needs APNs .p8 in the Firebase console (enable-notifications)"
  echo
fi

# ---------------------------------------------------------------------------- summary
if [ "$WARN_COUNT" -gt 0 ]; then
  echo "⚠️  $WARN_COUNT required key(s) still placeholder for phase '$PHASE' — ask the developer to supply them before continuing."
  $STRICT && exit 1 || exit 0
else
  echo "✅ All keys required for phase '$PHASE' are set."
fi
