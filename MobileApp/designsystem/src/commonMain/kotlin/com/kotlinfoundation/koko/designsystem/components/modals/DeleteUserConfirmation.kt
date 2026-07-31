package com.kotlinfoundation.koko.designsystem.components.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_cancel
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_delete
import com.kotlinfoundation.koko.designsystem.generated.resources.description_delete_user_dialog
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_delete
import com.kotlinfoundation.koko.designsystem.generated.resources.subtitle_delete_user_dialog
import com.kotlinfoundation.koko.designsystem.generated.resources.title_delete_user_dialog
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.stringResource

enum class DeleteUserConfirmationStyle {
    DIALOG,
    MODALBOTTOMSHEET,
}

@Composable
fun DeleteUserConfirmation(
    style: DeleteUserConfirmationStyle = DeleteUserConfirmationStyle.MODALBOTTOMSHEET,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (style) {
        DeleteUserConfirmationStyle.DIALOG -> {
            DeleteUserConfirmationDialog(onConfirm = onConfirm, onDismiss = onDismiss)
        }

        DeleteUserConfirmationStyle.MODALBOTTOMSHEET -> {
            DeleteUserConfirmationModal(onConfirm = onConfirm, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun DeleteUserConfirmationModal(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    // For Delete, we reverse buttons
    AppModalBottomSheet(
        title = stringResource(UiRes.string.title_delete_user_dialog),
        titleColor = AppTheme.colors.status.error,
        btnDismissText = stringResource(UiRes.string.btn_cancel),
        btnConfirmText = stringResource(UiRes.string.btn_delete),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        reverseButtonsOrder = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            Text(
                text = stringResource(UiRes.string.subtitle_delete_user_dialog),
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(UiRes.string.description_delete_user_dialog),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DeleteUserConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dialogType = DialogType.ERROR
    AppDialog(
        type = dialogType,
        title = stringResource(UiRes.string.title_delete_user_dialog),
        text = stringResource(UiRes.string.description_delete_user_dialog),
        btnDismissText = stringResource(UiRes.string.btn_cancel),
        btnConfirmText = stringResource(UiRes.string.btn_delete),
        image = {
            AppDialogImage(dialogType = dialogType, icon = UiRes.drawable.ic_delete)
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
@Preview
internal fun DeleteUserConfirmationPreview() {
    PreviewHelper {
        var style by remember {
            mutableStateOf<DeleteUserConfirmationStyle?>(null)
        }
        AppButton("Show Delete User Confirmation(Modal)") {
            style = DeleteUserConfirmationStyle.MODALBOTTOMSHEET
        }
        AppButton("Show Delete User Confirmation(Dialog)") {
            style = DeleteUserConfirmationStyle.DIALOG
        }

        style?.let {
            DeleteUserConfirmation(
                style = it,
                onConfirm = {
                    style = null
                },
                onDismiss = {
                    style = null
                },
            )
        }
    }
}
