package com.kotlinfoundation.koko.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
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
 * `onNodeWithText("42")` matches real rendered text rather than a pixel guess.
 *
 * Run: `./gradlew :shared:jvmTest` — ~2s warm, and part of the PR gate.
 *
 * This covers behaviour. For *visual* checks (layout, spacing, theming) add a `@Preview` — it is
 * snapshotted automatically by `PreviewScreenshotTest`; see the `verify-ui` skill.
 */
@OptIn(ExperimentalTestApi::class)
class SampleComposeUiTest {
    @Test
    fun `home screen renders the credit balance from uiState`() = runComposeUiTest {
        setContent {
            AppTheme {
                HomeScreen(uiState = HomeUiState(creditBalance = 42), onUiEvent = {})
            }
        }

        // The toolbar credit chip reflects state directly.
        onNodeWithText("42").assertExists()
    }

    @Test
    fun `clicking the credit chip emits OnClickToolbarCredits`() = runComposeUiTest {
        val events = mutableListOf<HomeUiEvent>()

        setContent {
            AppTheme {
                HomeScreen(uiState = HomeUiState(creditBalance = 7), onUiEvent = { events += it })
            }
        }

        onNodeWithText("7")
            .assertIsEnabled()
            .performClick()

        assertTrue(
            events.any { it is HomeUiEvent.OnClickToolbarCredits },
            "expected OnClickToolbarCredits, got $events",
        )
    }
}
