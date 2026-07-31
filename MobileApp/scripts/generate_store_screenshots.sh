#!/usr/bin/env bash
#
# Render every `@Preview @StoreScreenshot` composable in the project at storefront
# pixel dimensions, framed with the in-tree DeviceFrame + DeviceShell. Output PNGs
# land under distribution/store_screenshots/<locale>/<device>/ — upload-ready, no
# external tooling (Fastlane, ImageMagick) required.
#
# To add a new screenshot, drop a `@Preview @StoreScreenshot` composable anywhere
# under `com.kotlinfoundation.koko` and re-run this script. See:
#   shared/src/commonMain/kotlin/com/kotlinfoundation/koko/screenshot/StoreScreenshot.kt
#   shared/src/commonMain/kotlin/com/kotlinfoundation/koko/screenshot/DeviceFrame.kt
#
# Usage (run from MobileApp/):
#   ./scripts/generate_store_screenshots.sh
#
# How it works:
#   1. Sets `-PgenerateStoreScreenshots=true`, which switches the parameterized
#      Robolectric generator on. Without the flag, regular PR-check Roborazzi runs
#      skip the storefront previews entirely.
#   2. Roborazzi records (writes) the captured PNGs.
#   3. Outputs are placed by the test itself via captureRoboImage(filePath = ...).

set -euo pipefail
cd "$(dirname "$0")/.."

OUTPUT_DIR="distribution/store_screenshots"

echo "📸 Generating store screenshots..."
./gradlew :shared:generateStoreScreenshots -PgenerateStoreScreenshots=true --rerun-tasks

if [ ! -d "$OUTPUT_DIR" ]; then
  echo "⚠️  No screenshots produced. Did you add at least one @Preview @StoreScreenshot composable?"
  exit 1
fi

count=$(find "$OUTPUT_DIR" -name '*.png' 2>/dev/null | wc -l | tr -d ' ')
echo ""
echo "✅ Generated $count screenshot(s) under $OUTPUT_DIR/"
echo ""

# Per-(locale, device) summary so the user can spot misconfigurations at a glance.
find "$OUTPUT_DIR" -mindepth 2 -maxdepth 2 -type d | sort | while read -r dir; do
  n=$(find "$dir" -name '*.png' | wc -l | tr -d ' ')
  echo "   $dir ($n images)"
done
