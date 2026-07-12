#!/bin/bash

# Usage:
# ./create_module.sh ModuleName [targetDir] [namespace]

MODULE_NAME=$1
TARGET_DIR=${2:-libs}                # Default: libs

if [ -z "$MODULE_NAME" ]; then
    echo "Usage: $0 ModuleName [targetDir] [namespace]"
    exit 1
fi

# Safe module name (replace hyphens with dots)
SAFE_MODULE_NAME=$(echo "$MODULE_NAME" | tr '-' '.' | tr '[:upper:]' '[:lower:]')

# Namespace: use passed or default
NAMESPACE=${3:-"com.kotlinfoundation.koko.$SAFE_MODULE_NAME"}
SAFE_NAMESPACE=$(echo "$NAMESPACE" | tr -d ' ' | tr '[:upper:]' '[:lower:]')
MODULE_PATH="$TARGET_DIR/$MODULE_NAME"
COMMON_SRC_PATH="$MODULE_PATH/src/commonMain/kotlin/$(echo "$SAFE_NAMESPACE" | tr '.' '/')"


# -------------------------------
# Create module directories
# -------------------------------
echo "Creating module at $MODULE_PATH..."
mkdir -p "$COMMON_SRC_PATH"

# -------------------------------
# Create build.gradle.kts
# -------------------------------
cat > "$MODULE_PATH/build.gradle.kts" <<EOL
plugins {
    id("configure-kmp-library-module")
}

kotlin {
    android {
        namespace = "$NAMESPACE"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
EOL

# -------------------------------
# Add module to settings.gradle.kts
# -------------------------------
SETTINGS_FILE="settings.gradle.kts"
GRADLE_MODULE_PATH=$(echo "$TARGET_DIR/$MODULE_NAME" | tr '/' ':')
INCLUDE_STATEMENT="include(\":$GRADLE_MODULE_PATH\")"

if ! grep -qF "$INCLUDE_STATEMENT" "$SETTINGS_FILE"; then
    echo "Adding module to $SETTINGS_FILE..."
    echo "$INCLUDE_STATEMENT" >> "$SETTINGS_FILE"
else
    echo "Module already included in $SETTINGS_FILE"
fi

echo "Module $MODULE_NAME created successfully!"
echo "Namespace: $NAMESPACE"
echo "CommonMain path: $COMMON_SRC_PATH"
