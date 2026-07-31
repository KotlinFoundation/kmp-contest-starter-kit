#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# ASO Metadata Generator (Named Arguments)
# ============================================================

# ----------------------------
# CONFIG
# ----------------------------

# Set your OpenAI API key either in local.properties or
# in global credentials file or here with  OPENAI_API_KEY=ENTER_YOUR_KEY
OPENAI_API_KEY=""
LOCAL_PROPERTIES_FILE="./local.properties"
GLOBAL_CREDENTIALS_FILE="$HOME/credentials/credentials.txt"

MODEL="gpt-5-mini"
OUTPUT_DIRECTORY="./distribution"
DEFAULT_LOCALES=("en-US")
DEFAULT_STORE="both"
BASE_LOCALE=""
ANDROID_CONTEXT=""
IOS_CONTEXT=""



# ----------------------------
# HELP
# ----------------------------
usage() {
  cat <<EOF
Usage:
    ./generate_aso_metadata.sh [--keywords "<keywords>"] \
      (--idea "<text>" | --idea-file <file> | --base-locale <path>) \
      [--locales "<l1,l2>"] [--store ios|android|both]


Modes:
  Generation mode:
    --idea "<text>" OR --idea-file <file>

  Translation mode:
    --base-locale <path>
      Folder containing existing locale metadata

Options:
  --keywords     Comma-separated ASO target keywords that you want to be ranked for
  --idea         Short app idea or description
  --store ios|android|both   (default: both)
  --idea-file    Path to PRD / idea document (markdown or text)
  --locales      Comma-separated locales (default: ${DEFAULT_LOCALES[*]})
  --base-locale
  -h, --help     Show this help

Examples:
  ./generate_aso_metadata.sh --idea "AI manga translator"
  ./generate_aso_metadata.sh --idea-file prd.md --store ios
  ./generate_aso_metadata.sh --idea "OCR scanner" --keywords "document scanner,ocr app" --locales "en-US,es-ES"
  ./generate_aso_metadata.sh --base-locale "en-US" --locales "es-ES,fr-FR"
EOF
  exit 1
}


# ----------------------------
# VARIABLES
# ----------------------------
STORE="$DEFAULT_STORE"
KEYWORDS=""
IDEA_TEXT=""
IDEA_FILE=""
LOCALES_INPUT=""


# ----------------------------
# OPENAI API KEY RESOLUTION
# ----------------------------

read_key_from_file() {
  local file="$1"
  local key="$2"

  [[ ! -f "$file" ]] && return 0

  sed -nE "
    s/^[[:space:]]*(export[[:space:]]+)?${key}[[:space:]]*=[[:space:]]*['\"]?([^'\"]+)['\"]?.*/\2/p
  " "$file" | head -n 1
}


if [[ -z "$OPENAI_API_KEY" ]]; then
  # 1. Try local.properties
  OPENAI_API_KEY="$(read_key_from_file "$LOCAL_PROPERTIES_FILE" "OPENAI_API_KEY")"

  if [[ -n "$OPENAI_API_KEY" ]]; then
    echo "🔑 Using OPENAI_API_KEY from local.properties" >&2
  else
    # 2. Try global credentials
    OPENAI_API_KEY="$(read_key_from_file "$GLOBAL_CREDENTIALS_FILE" "OPENAI_API_KEY")"

    if [[ -n "$OPENAI_API_KEY" ]]; then
      echo "🌍 Using OPENAI_API_KEY from $GLOBAL_CREDENTIALS_FILE" >&2
    else
      echo "⚠️ OPENAI_API_KEY not found in local or global credentials, using default value" >&2
    fi
  fi
fi


# ----------------------------
# ARGUMENT PARSING
# ----------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --keywords)
      KEYWORDS="$2"
      shift 2
      ;;
    --idea)
      IDEA_TEXT="$2"
      shift 2
      ;;
    --idea-file)
      IDEA_FILE="$2"
      shift 2
      ;;
    --locales)
      LOCALES_INPUT="$2"
      shift 2
      ;;
    --store)
      STORE="$2"
      shift 2
      ;;
    --base-locale)
      BASE_LOCALE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "❌ Unknown argument: $1"
      usage
      ;;
  esac
done

# ----------------------------
# VALIDATION
# ----------------------------

if [[ -z "$BASE_LOCALE" && -z "$IDEA_TEXT" && -z "$IDEA_FILE" ]]; then
  echo "❌ One of --idea, --idea-file, or --base-locale is required"
  usage
fi

if [[ -n "$BASE_LOCALE" && ( -n "$IDEA_TEXT" || -n "$IDEA_FILE" ) ]]; then
  echo "❌ Use either translation mode OR idea mode, not both"
  usage
fi


if [[ "$STORE" != "ios" && "$STORE" != "android" && "$STORE" != "both" ]]; then
  echo "❌ Invalid --store value"
  usage
fi


# ----------------------------
# LOAD IDEA CONTENT
# ----------------------------

if [[ -n "$BASE_LOCALE" ]]; then
  echo "🌍 Translation mode using base locale: $BASE_LOCALE"

  # ---------- ANDROID BASE ----------
  if [[ "$STORE" == "android" || "$STORE" == "both" ]]; then
    ANDROID_BASE="$OUTPUT_DIRECTORY/android/playstore_metadata/$BASE_LOCALE"

    ANDROID_CONTEXT=$(cat <<EOF
BASE TITLE:
$(cat "$ANDROID_BASE/title.txt")

BASE SHORT DESCRIPTION:
$(cat "$ANDROID_BASE/short_description.txt")

BASE FULL DESCRIPTION:
$(cat "$ANDROID_BASE/full_description.txt")
EOF
)
  fi

  # ---------- IOS BASE ----------
  if [[ "$STORE" == "ios" || "$STORE" == "both" ]]; then
    IOS_BASE="$OUTPUT_DIRECTORY/ios/appstore_metadata/texts/$BASE_LOCALE"

    IOS_CONTEXT=$(cat <<EOF
BASE NAME:
$(cat "$IOS_BASE/name.txt")

BASE SUBTITLE:
$(cat "$IOS_BASE/subtitle.txt")

BASE KEYWORDS:
$(cat "$IOS_BASE/keywords.txt")

BASE DESCRIPTION:
$(cat "$IOS_BASE/description.txt")
EOF
)
  fi

else
  # Idea mode (original behavior)

  if [[ -n "$IDEA_FILE" ]]; then
    IDEA_CONTENT="$(cat "$IDEA_FILE")"
  else
    IDEA_CONTENT="$IDEA_TEXT"
  fi

  ANDROID_CONTEXT="$IDEA_CONTENT"
  IOS_CONTEXT="$IDEA_CONTENT"
fi

ANDROID_CONTEXT=$(echo "$ANDROID_CONTEXT" | perl -pe 's/[\x00-\x08\x0B\x0C\x0E-\x1F]//g')
IOS_CONTEXT=$(echo "$IOS_CONTEXT" | perl -pe 's/[\x00-\x08\x0B\x0C\x0E-\x1F]//g')





# ----------------------------
# LOCALES
# ----------------------------
if [[ -z "$LOCALES_INPUT" ]]; then
  LOCALES_INPUT=$(IFS=','; echo "${DEFAULT_LOCALES[*]}")
fi

IFS=',' read -ra LOCALES <<< "$LOCALES_INPUT"

# ----------------------------
# PROMPT BUILDER
# ----------------------------
build_ios_prompt() {
  local locale="$1"

  cat <<EOF
You are a professional iOS App Store ASO expert.

Your goal is to maximize keyword ranking according to Apple's App Store rules.

IMPORTANT ASO FACTS (follow strictly):
- App Name has the HIGHEST ranking weight
- Words earlier (left-most) rank stronger
- Subtitle has the SECOND highest ranking weight
- Keyword field has LOWER weight
- Description has NO ranking impact (conversion only)


APP CONTEXT (or BASE LOCALE CONTENT if provided):
$IOS_CONTEXT

TARGET KEYWORDS (OPTIONAL):
${KEYWORDS:-"(not provided – derive from app context)"}

LOCALE:
$locale

TASK:
Generate optimized iOS App Store metadata following these rules:

1. APP NAME (Title)
- Put the SINGLE most important keyword FIRST
- Front-load it (left-most position)
- Can include a short brand or descriptor after
- No fluff, no marketing phrases
- Maximize ranking first, branding second

2. SUBTITLE
- Use 1–2 SECONDARY keywords
- Front-load important words
- Do NOT repeat words from the title
- Keep it concise and descriptive

3. KEYWORDS FIELD
- Comma-separated
- Max 100 characters total
- No spaces
- NO words used in title or subtitle
- Include synonyms, plural/singular, long-tail variations

4. DESCRIPTION
- Written ONLY for conversion
- Explain features and benefits clearly
- No keyword stuffing

OUTPUT FORMAT:
- Return ONLY valid JSON
- No explanations
- No markdown
- Keys exactly: name, subtitle, keywords, description

STRICT JSON REQUIREMENTS:
- All keys must exist.
- All string values must be properly escaped.
- Use \\n for newlines if needed.
- Do NOT include extra explanations, markdown, or comments.
- Avoid control characters (U+0000 to U+001F), except tab, LF, CR.
EOF
}

build_android_prompt() {
  local locale="$1"

  cat <<EOF
You are a professional Google Play ASO expert.

CRITICAL GOOGLE PLAY CONSTRAINTS (MUST FOLLOW):
- App TITLE: max 30 characters (HARD LIMIT, including spaces & symbols)
- Short description: max 80 characters (HARD LIMIT)
- Full description: max 4000 characters
- Google Play REJECTS listings that exceed limits
- Locale-specific text MUST respect the same limits

GOOGLE PLAY ASO RULES:
- Title has strong branding & ranking impact
- Short description has VERY strong conversion impact
- Full description IS indexed for keywords
- No keyword field exists
- Natural keyword usage matters (no stuffing)

APP CONTEXT (or BASE LOCALE CONTENT if provided):
$ANDROID_CONTEXT

TARGET KEYWORDS (optional):
${KEYWORDS:-"(not provided – derive from app context)"}

INSTRUCTIONS:
- If keywords are not provided, derive them from app context
- Choose a PRIMARY keyword and several SECONDARY keywords
- Use keywords naturally in the given locale, not spammy

LOCALE:
$locale

FIELDS:

1. TITLE
- MAX 30 CHARACTERS (STRICT)
- Prefer brand + primary keyword
- Clear, readable, Play Store safe
- If localized wording risks exceeding 30 chars, KEEP IT SHORT OR USE ENGLISH

2. SHORT DESCRIPTION
- MAX 80 CHARACTERS (STRICT)
- Include PRIMARY + at least one SECONDARY keyword
- Strong value proposition
- Clear benefit in one sentence
- No filler words

3. FULL DESCRIPTION
- Mention PRIMARY keyword 3–5 times
- Natural use of secondary keywords
- Explain:
  - What the app does
  - Key features
  - Main benefits
  - Typical use cases
- Use short paragraphs or bullet points
- Avoid keyword stuffing
- Write naturally for native speakers of the locale

OUTPUT:
Return ONLY valid JSON:
title, short_description, full_description

STRICT JSON REQUIREMENTS:
- All keys must exist.
- All string values must be properly escaped.
- Use \\n for newlines if needed.
- Do NOT include extra explanations, markdown, or comments.
- Avoid control characters (U+0000 to U+001F), except tab, LF, CR.
EOF
}



# ----------------------------
# OPENAI CALL
# ----------------------------
call_openai() {

  local prompt="$1"

  prompt=$(echo "$prompt" | perl -CS -pe 's/[\x00-\x08\x0B\x0C\x0E-\x1F]//g')

  local payload


    payload=$(jq -n \
      --arg model "$MODEL" \
      --arg content "$prompt" \
      '{
        model: $model,
        messages: [
          {
            role: "user",
            content: $content
          }
        ]
      }'
    )



  response=$(curl -s https://api.openai.com/v1/chat/completions \
    -H "Authorization: Bearer $OPENAI_API_KEY" \
    -H "Content-Type: application/json" \
    -d "$payload"
  )

  # sanitize response to remove any invalid control characters
  response=$(echo "$response" | perl -CS -pe 's/[\x00-\x08\x0B\x0C\x0E-\x1F]//g')

  if echo "$response" | jq -e '.error' >/dev/null; then
    echo "❌ OpenAI error:" >&2
    echo "$response" >&2
    exit 1
  fi

  # Now parse safely
  echo "$response" | jq -r '.choices[0].message.content'
}

# ----------------------------
# GENERATE (PARALLEL)
# ----------------------------
PIDS=()

for locale in "${LOCALES[@]}"; do
  (
    echo "🚀 $locale → $STORE" >&2

    if [[ "$STORE" == "ios" || "$STORE" == "both" ]]; then
      ios_prompt="$(build_ios_prompt "$locale")"
      ios_json="$(call_openai "$ios_prompt")"

      out="$OUTPUT_DIRECTORY/ios/appstore_metadata/texts/$locale"
      mkdir -p "$out"

      echo "$ios_json" | jq -r '.name'        > "$out/name.txt"
      echo "$ios_json" | jq -r '.subtitle'    > "$out/subtitle.txt"
      echo "$ios_json" | jq -r '.keywords'    > "$out/keywords.txt"
      echo "$ios_json" | jq -r '.description' > "$out/description.txt"
    fi

    if [[ "$STORE" == "android" || "$STORE" == "both" ]]; then
      android_prompt="$(build_android_prompt "$locale")"
      android_json="$(call_openai "$android_prompt")"

      out="$OUTPUT_DIRECTORY/android/playstore_metadata/$locale"
      mkdir -p "$out"

      echo "$android_json" | jq -r '.title'             > "$out/title.txt"
      echo "$android_json" | jq -r '.short_description' > "$out/short_description.txt"
      echo "$android_json" | jq -r '.full_description'  > "$out/full_description.txt"
    fi

    echo "✅ $locale done" >&2
  ) &

  PIDS+=($!)
done

for pid in "${PIDS[@]}"; do
  wait "$pid"
done

echo "🎉 ASO metadata generated successfully in $OUTPUT_DIRECTORY !"
