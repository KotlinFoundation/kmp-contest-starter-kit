#!/bin/bash

########################################
# USAGE:
#   This script will generate android keystore file for publishing in PlayStore
#  ./generate_keystore.sh FIRST_NAME ORGANIZATION [VALIDITY_DAYS]
#
# Examples:
#  Using person identity only:
#     ./generate_keystore.sh "Mirzamehdi Karimov" ""
#
#  Using organization only:
#     ./generate_keystore.sh "" "KMPStarterKit"
#
#  Using both:
#     ./generate_keystore.sh "Mirzamehdi Karimov" "KMPStarterKit"
#
########################################

set -e

FIRST_NAME="$1"
ORGANIZATION="$2"

# --- REQUIRED ARGS ---
# Validation: at least FIRST_NAME or ORGANIZATION must be provided.
if [ -z "$FIRST_NAME" ] && [ -z "$ORGANIZATION" ]; then
    echo "❌ ERROR: You must provide EITHER a first name OR an organization."
    echo ""
    echo "Correct usage:"
    echo "  ./generate_keystore.sh FIRST_NAME ORGANIZATION"
    echo ""
    echo "Examples:"
    echo "  ./generate_keystore.sh \"Mirzamehdi Karimov\" \"\""
    echo "  ./generate_keystore.sh \"\" \"KMPStarterKit\""
    echo "  ./generate_keystore.sh \"Mirzamehdi Karimov\" \"KMPStarterKit\" "
    exit 1
fi


# defaults
# Resolve root dir based on script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

OUTPUT_DIR="$ROOT_DIR/distribution/android/keystore"

KEY_ALIAS="aliasKey"
KEY_VALIDITY="${3:-10000}"

# Paths
STORE_FILE="$OUTPUT_DIR/keystore.jks"
PROP_FILE="$OUTPUT_DIR/keystore.properties"

# Generate secure passwords
STORE_PASSWORD=$(LC_ALL=C tr -dc 'A-Za-z0-9!@#$%^&*()-_=+[]{}<>?,.' < /dev/urandom | head -c 24)

mkdir -p "$OUTPUT_DIR"

echo "🔑 Generating keystore..."

keytool -genkeypair \
  -v \
  -keystore "$STORE_FILE" \
  -storepass "$STORE_PASSWORD" \
  -alias "$KEY_ALIAS" \
  -keypass "$STORE_PASSWORD" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$KEY_VALIDITY" \
  -dname "CN=$FIRST_NAME, O=$ORGANIZATION"

echo "📝 Creating keystore.properties..."

cat <<EOF > "$PROP_FILE"
keystorePassword=$STORE_PASSWORD
keyPassword=$STORE_PASSWORD
keyAlias=$KEY_ALIAS
storeFile=../distribution/android/keystore/keystore.jks
EOF

echo ""
echo "🎉 Done! Generated Keystore files:"
echo " - $STORE_FILE"
echo " - $PROP_FILE"
echo ""
