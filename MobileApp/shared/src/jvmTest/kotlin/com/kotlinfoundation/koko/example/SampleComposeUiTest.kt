package com.kotlinfoundation.koko.example

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Demonstrates a multiplatform Compose UI test using the new `runComposeUiTest` API
 * from `org.jetbrains.compose.ui:ui-test`. Runs headlessly on JVM as part of
 * `:shared:jvmTest`.
 */
@OptIn(ExperimentalTestApi::class)
class SampleComposeUiTest {
    @Test
    fun `clicking a labeled clickable invokes the callback exactly once`() = runComposeUiTest {
        var clickCount = 0

        setContent {
            ClickableLabel(
                text = "Tap me",
                onClick = { clickCount++ },
            )
        }

        onNodeWithText("Tap me")
            .assertIsEnabled()
            .performClick()

        assertTrue(clickCount == 1, "expected exactly one click, got $clickCount")
    }

    @Composable
    private fun ClickableLabel(
        text: String,
        onClick: () -> Unit,
    ) {
        Text(
            text = text,
            modifier =
            Modifier
                .semantics {
                    role = Role.Button
                    contentDescription = text
                }
                .clickable(onClick = onClick),
        )
    }
}
