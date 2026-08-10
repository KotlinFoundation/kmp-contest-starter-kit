package com.kotlinfoundation.koko.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.presentation.screens.home.HomeScreen
import com.kotlinfoundation.koko.presentation.screens.home.HomeUiEvent
import com.kotlinfoundation.koko.presentation.screens.home.HomeUiState
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Template for verifying a REAL app screen headlessly — copy this shape for new screens.
 *
 * How it works: every screen has a **pure overload** taking `uiState` + `onUiEvent` instead of a
 * ViewModel, so a test can render it with no Koin, no ViewModel, no device and no browser. Queries
 * go through Compose's semantics tree (the same data a screen reader uses), so
 * `onNodeWithText("Create")` matches real rendered text rather than a pixel guess.
 *
 * Run: `./gradlew :shared:jvmTest` — ~2s warm, and part of the PR gate.
 *
 * This covers behaviour. For *visual* checks (layout, spacing, theming) add a `@Preview` — it is
 * snapshotted automatically by `PreviewScreenshotTest`; see the `verify-ui` skill.
 */
@OptIn(ExperimentalTestApi::class)
class SampleComposeUiTest {
    @Test
    fun `generate button is disabled until the prompt has text`() = runComposeUiTest {
        setContent {
            AppTheme {
                HomeScreen(uiState = HomeUiState(prompt = ""), onUiEvent = {})
            }
        }

        // isGenerationButtonEnabled is derived from uiState, and the button reflects it directly.
        onNodeWithText("Create").assertIsNotEnabled()
    }

    @Test
    fun `clicking generate emits OnClickGenerate`() = runComposeUiTest {
        val events = mutableListOf<HomeUiEvent>()

        setContent {
            AppTheme {
                HomeScreen(uiState = HomeUiState(prompt = "a cat"), onUiEvent = { events += it })
            }
        }

        onNodeWithText("Create")
            .assertIsEnabled()
            .performClick()

        assertTrue(
            events.any { it is HomeUiEvent.OnClickGenerate },
            "expected OnClickGenerate, got $events",
        )
    }
}
