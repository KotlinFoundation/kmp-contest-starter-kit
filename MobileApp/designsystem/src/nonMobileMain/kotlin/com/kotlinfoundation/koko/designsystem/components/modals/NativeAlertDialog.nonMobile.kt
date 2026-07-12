package com.kotlinfoundation.koko.designsystem.components.modals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kotlinfoundation.koko.designsystem.theme.AppTheme

/**
 * Shows Native dialog. In Android AlertDialog, in iOS UIAlerts.
 *
 * @param title The title of the dialog.
 * @param text The text of the dialog.
 * @param btnConfirmText The text of the confirm button.
 * @param btnDismissText The text of the dismiss button.
 * @param dismissOnClickOutside Indicates should dismiss dialog when clicking outside.
 * @param onConfirm Lambda that is invoked when the confirm button is clicked.
 * @param onDismiss Lambda that is invoked when the dismiss button is clicked.
 */
@Composable
actual fun NativeAlertDialog(
    title: String,
    text: String,
    btnConfirmText: String,
    btnDismissText: String,
    dismissOnClickOutside: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var isPresented by remember { mutableStateOf(true) }
    if (!isPresented) return
    AlertDialog(
        containerColor = AppTheme.colors.surfaceContainer,
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = {
            if (dismissOnClickOutside) {
                onDismiss()
                isPresented = false
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                isPresented = false
            }) {
                Text(btnConfirmText)
            }
        },
        dismissButton = {
            if (btnDismissText.isEmpty().not()) {
                TextButton(onClick = {
                    onDismiss()
                    isPresented = false
                }) {
                    Text(btnDismissText)
                }
            }
        },
    )
}
