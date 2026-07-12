package com.kotlinfoundation.koko.designsystem.components.modals

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_premium_feature_crown
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.painterResource

@Composable
fun UpgradePremiumDialog(
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AppDialog(
        title = "Premium Features!",
        text = "Upgrade plan to unlock premium features and content!",
        image = {
            Image(
                painter = painterResource(UiRes.drawable.ic_premium_feature_crown),
                contentDescription = null,
            )
        },
        btnConfirmText = "Upgrade Plan",
        btnDismissText = "Maybe Later",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

// `@Preview` intentionally omitted: this preview renders an interactive button that
// shows the dialog on click — useful in IDE preview but non-deterministic for
// Roborazzi (the dialog isn't visible without user interaction).
@Composable
internal fun UpgradePremiumDialogPreview() {
    var isDialogVisible by remember { mutableStateOf(false) }
    AppButton("Show Premium Dialog", onClick = { isDialogVisible = true })
    PreviewHelper {
        if (isDialogVisible) {
            UpgradePremiumDialog(
                onConfirm = { isDialogVisible = false },
                onDismiss = { isDialogVisible = false },
            )
        }
    }
}
