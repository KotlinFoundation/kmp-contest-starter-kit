#!/usr/bin/env bash
#
# Scaffold a new screen and wire it into navigation + DI.
#
# Generates:
#   shared/src/commonMain/kotlin/<base>/presentation/screens/<lower>/
#     ├── <Name>Screen.kt
#     ├── <Name>UiState.kt          (also contains UiEvent)
#     └── <Name>ViewModel.kt
#
# Patches (idempotent — safe to re-run):
#   presentation/navigation/Routes.kt        adds `data object <Name>ScreenRoute : ScreenRoute`
#   presentation/navigation/AppNavigation.kt adds an `entry<>` block + screen/view-model imports
#   root/Di.kt                               adds `viewModelOf(::<Name>ViewModel)` + import
#
# Usage (run from MobileApp/):
#   ./scripts/generate_screen.sh ScreenName

set -e

SCREEN_NAME=$1

if [ -z "$SCREEN_NAME" ]; then
  echo "Usage: ./scripts/generate_screen.sh ScreenName"
  exit 1
fi

# Add `import <fqn>` to a file, in the alphabetical slot ktlint expects. No-op if already there.
insert_import() {
  file=$1
  fqn=$2

  if grep -q "^import $fqn$" "$file"; then
    return 0
  fi

  # First existing import that sorts after the new one — insert above it.
  target_line=$(grep -n '^import ' "$file" |
    awk -F: -v new="import $fqn" '{ text = substr($0, index($0, ":") + 1); if (text > new) { print $1; exit } }')

  if [ -n "$target_line" ]; then
    sed -i '' "${target_line}i\\
import $fqn
" "$file"
    return 0
  fi

  # Sorts last: append after the final import.
  last_import=$(grep -n '^import ' "$file" | tail -1 | cut -d: -f1)
  if [ -n "$last_import" ]; then
    sed -i '' "${last_import}a\\
import $fqn
" "$file"
    return 0
  fi

  # No imports at all: open a block below `package`, keeping the blank line between them.
  sed -i '' "/^package /a\\
\\
import $fqn
" "$file"
}

# Capitalize the first letter, leave the rest alone.
SCREEN_BASE="$(printf '%s' "${SCREEN_NAME:0:1}" | tr '[:lower:]' '[:upper:]')${SCREEN_NAME:1}"

BASE_PACKAGE="com.kotlinfoundation.koko"
BASE_PATH=$(echo "$BASE_PACKAGE" | tr '.' '/')

LOWER_NAME=$(echo "$SCREEN_BASE" | tr '[:upper:]' '[:lower:]')

SCREEN_CLASS="${SCREEN_BASE}Screen"
UI_STATE_CLASS="${SCREEN_BASE}UiState"
UI_EVENT_CLASS="${SCREEN_BASE}UiEvent"
VIEWMODEL_CLASS="${SCREEN_BASE}ViewModel"
ROUTE_CLASS="${SCREEN_BASE}ScreenRoute"

SCREENS_PACKAGE="$BASE_PACKAGE.presentation.screens.$LOWER_NAME"

SCREEN_DIR="shared/src/commonMain/kotlin/$BASE_PATH/presentation/screens/$LOWER_NAME"
ROUTES_FILE="shared/src/commonMain/kotlin/$BASE_PATH/presentation/navigation/Routes.kt"
APPNAV_FILE="shared/src/commonMain/kotlin/$BASE_PATH/presentation/navigation/AppNavigation.kt"
DI_FILE="shared/src/commonMain/kotlin/$BASE_PATH/root/Di.kt"

mkdir -p "$SCREEN_DIR"

UI_STATE_FILE="$SCREEN_DIR/${UI_STATE_CLASS}.kt"
VIEWMODEL_FILE="$SCREEN_DIR/${VIEWMODEL_CLASS}.kt"
SCREEN_FILE="$SCREEN_DIR/${SCREEN_CLASS}.kt"

################################
# 1. UiState + UiEvent
################################
if [ -f "$UI_STATE_FILE" ]; then
  echo "Skipping (already exists): $UI_STATE_FILE"
else
  cat > "$UI_STATE_FILE" <<EOF
package $SCREENS_PACKAGE

class $UI_STATE_CLASS

sealed interface $UI_EVENT_CLASS {
    data object OnClick : $UI_EVENT_CLASS
}
EOF
  echo "Created: $UI_STATE_FILE"
fi

################################
# 2. ViewModel
################################
if [ -f "$VIEWMODEL_FILE" ]; then
  echo "Skipping (already exists): $VIEWMODEL_FILE"
else
  cat > "$VIEWMODEL_FILE" <<EOF
package $SCREENS_PACKAGE

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class $VIEWMODEL_CLASS : ViewModel() {
    private val _uiState = MutableStateFlow($UI_STATE_CLASS())
    val uiState: StateFlow<$UI_STATE_CLASS> = _uiState.asStateFlow()

    fun onUiEvent(event: $UI_EVENT_CLASS) {
        when (event) {
            $UI_EVENT_CLASS.OnClick -> TODO()
        }
    }
}
EOF
  echo "Created: $VIEWMODEL_FILE"
fi

################################
# 3. Screen
################################
if [ -f "$SCREEN_FILE" ]; then
  echo "Skipping (already exists): $SCREEN_FILE"
else
  cat > "$SCREEN_FILE" <<EOF
package $SCREENS_PACKAGE

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.theme.AppTheme

@Composable
fun $SCREEN_CLASS(
    modifier: Modifier = Modifier,
    viewModel: $VIEWMODEL_CLASS,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    $SCREEN_CLASS(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
    )
}

@Composable
fun $SCREEN_CLASS(
    modifier: Modifier = Modifier,
    uiState: $UI_STATE_CLASS,
    onUiEvent: ($UI_EVENT_CLASS) -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true, // Set to false if content itself has scrollable content such as LazyColumn
        title = "$SCREEN_CLASS",
        includeBottomInsets = true, // Set to false if bottom nav is visible
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
            Text("$SCREEN_CLASS")
        }
    }
}
EOF
  echo "Created: $SCREEN_FILE"
fi

################################
# 4. Routes.kt — append route definition
################################
if [ -f "$ROUTES_FILE" ]; then
  if grep -q "data object $ROUTE_CLASS " "$ROUTES_FILE" || \
     grep -q "data class $ROUTE_CLASS(" "$ROUTES_FILE"; then
    echo "Skipping route in Routes.kt (already present): $ROUTE_CLASS"
  elif ! grep -q "// Add new routes below" "$ROUTES_FILE"; then
    echo "⚠️ Marker '// Add new routes below' missing in Routes.kt — add it before re-running."
  else
    # Insert route declaration in reverse so the final order reads top-to-bottom.
    sed -i '' "/\/\/ Add new routes below/i\\
data object $ROUTE_CLASS : ScreenRoute\\

" "$ROUTES_FILE"
    sed -i '' "/data object $ROUTE_CLASS/i\\
@SerialName(\"$SCREEN_BASE\")
" "$ROUTES_FILE"
    sed -i '' "/@SerialName(\"$SCREEN_BASE\")/i\\
@Serializable
" "$ROUTES_FILE"
    echo "Updated: $ROUTES_FILE"
  fi
else
  echo "⚠️ Warning: $ROUTES_FILE not found — skipping route registration."
fi

################################
# 5. AppNavigation.kt — imports + entry block
################################
if [ -f "$APPNAV_FILE" ]; then
  # Imports (idempotent)
  insert_import "$APPNAV_FILE" "$SCREENS_PACKAGE.$SCREEN_CLASS"
  insert_import "$APPNAV_FILE" "$SCREENS_PACKAGE.$VIEWMODEL_CLASS"

  # Entry block (idempotent)
  if grep -q "entry<$ROUTE_CLASS>" "$APPNAV_FILE"; then
    echo "Skipping entry in AppNavigation.kt (already present): entry<$ROUTE_CLASS>"
  elif ! grep -q "// Add new screen entries below" "$APPNAV_FILE"; then
    echo "⚠️ Marker '// Add new screen entries below' missing in AppNavigation.kt — add it before re-running."
  else
    # One insert for the whole block: every `i\` anchors on the marker, so splitting this into
    # several seds would stack the lines up in reverse order.
    sed -i '' "/\/\/ Add new screen entries below/i\\
    entry<$ROUTE_CLASS> {\\
        val viewModel = koinViewModel<$VIEWMODEL_CLASS>()\\
        $SCREEN_CLASS(viewModel = viewModel)\\
    }\\

" "$APPNAV_FILE"
    echo "Updated: $APPNAV_FILE"
  fi
else
  echo "⚠️ Warning: $APPNAV_FILE not found — skipping nav entry."
fi

################################
# 6. Di.kt — import + viewModelOf
################################
if [ -f "$DI_FILE" ]; then
  insert_import "$DI_FILE" "$SCREENS_PACKAGE.$VIEWMODEL_CLASS"

  if grep -q "viewModelOf(::$VIEWMODEL_CLASS)" "$DI_FILE"; then
    echo "Skipping DI registration (already present): $VIEWMODEL_CLASS"
  elif ! grep -q "// Add new view models below" "$DI_FILE"; then
    echo "⚠️ Marker '// Add new view models below' missing in Di.kt — add it before re-running."
  else
    sed -i '' "/\/\/ Add new view models below/i\\
    viewModelOf(::$VIEWMODEL_CLASS)
" "$DI_FILE"
    echo "Updated: $DI_FILE"
  fi
else
  echo "⚠️ Warning: $DI_FILE not found — skipping DI registration."
fi

echo "✅ Screen $SCREEN_BASE generated and wired up."
echo "   You probably want to customise navigation callbacks in the entry<> block — see AppNavigation.kt."
