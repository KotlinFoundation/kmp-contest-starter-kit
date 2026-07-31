package com.kotlinfoundation.koko.designsystem.components.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.ButtonStyle
import com.kotlinfoundation.koko.designsystem.components.DialogOrBottomSheetTitle
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_ok
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_check
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_close
import com.kotlinfoundation.koko.designsystem.generated.resources.title_error_dialog
import com.kotlinfoundation.koko.designsystem.generated.resources.title_info
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

enum class DialogType {
    INFO,
    ERROR,
}

@Composable
fun AppDialog(
    type: DialogType = DialogType.INFO,
    title: String =
        when (type) {
            DialogType.INFO -> stringResource(UiRes.string.title_info)
            DialogType.ERROR -> stringResource(UiRes.string.title_error_dialog)
        },
    titleColor: Color =
        when (type) {
            DialogType.INFO -> AppTheme.colors.primary
            DialogType.ERROR -> AppTheme.colors.status.error
        },
    text: String? = "",
    image: @Composable (() -> Unit)? = { AppDialogImage(dialogType = type) },
    btnConfirmText: String = stringResource(UiRes.string.btn_ok),
    contentPaddingSpacing: Dp = AppTheme.spacing.dialogContentSpacing,
    btnDismissText: String = "",
    hideButtons: Boolean = false,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit = {},
) = AppDialog(
    title = title,
    titleColor = titleColor,
    text = text,
    image = image,
    hideButtons = hideButtons,
    contentPaddingSpacing = contentPaddingSpacing,
    btnConfirmText = btnConfirmText,
    btnDismissText = btnDismissText,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    content = content,
)

@Composable
fun AppDialog(
    title: String = stringResource(UiRes.string.title_info),
    titleColor: Color = AppTheme.colors.text.primary,
    text: String? = "",
    image: @Composable (() -> Unit)? = null,
    btnConfirmText: String = stringResource(UiRes.string.btn_ok),
    btnDismissText: String = "",
    hideButtons: Boolean = false,
    contentPaddingSpacing: Dp = AppTheme.spacing.dialogContentSpacing,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            colors =
            CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceContainer,
                contentColor = AppTheme.colors.text.primary,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(contentPaddingSpacing)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.dialogContentSpacing),
            ) {
                image?.let {
                    Box(contentAlignment = Alignment.Center) { it() }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
                    horizontalAlignment = if (hideButtons) Alignment.Start else Alignment.CenterHorizontally,
                ) {
                    if (hideButtons) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DialogOrBottomSheetTitle(
                                modifier = Modifier.weight(1f),
                                color = titleColor,
                                text = title,
                                textAlign = TextAlign.Start,
                            )

                            Icon(
                                painter = painterResource(UiRes.drawable.ic_close),
                                contentDescription = "Close",
                                modifier =
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.outline)
                                    .clickable {
                                        onDismiss()
                                    }.padding(6.dp),
                                tint = AppTheme.colors.text.primary,
                            )
                        }
                    } else {
                        DialogOrBottomSheetTitle(
                            color = titleColor,
                            text = title,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (text.isNullOrEmpty().not()) {
                        Text(
                            text = text,
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.text.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    content()
                }

                if (hideButtons) return@Column
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
                    AppButton(
                        text = btnConfirmText.ifEmpty { stringResource(UiRes.string.btn_ok) },
                        onClick = { onConfirm() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (btnDismissText.isNotEmpty()) {
                        AppButton(
                            btnDismissText,
                            onClick = { onDismiss() },
                            style = ButtonStyle.ALTERNATIVE,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppDialogImage(
    dialogType: DialogType,
    icon: DrawableResource =
        when (dialogType) {
            DialogType.INFO -> UiRes.drawable.ic_check
            DialogType.ERROR -> UiRes.drawable.ic_close
        },
) {
    val iconTint =
        when (dialogType) {
            DialogType.INFO -> AppTheme.colors.primary
            DialogType.ERROR -> AppTheme.colors.status.error
        }

    Box(
        modifier =
        Modifier
            .padding(top = AppTheme.spacing.defaultSpacing)
            .size(114.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(listOf(iconTint, iconTint.copy(0.8f))),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppTheme.colors.onPrimary)
                .padding(AppTheme.spacing.defaultSpacing),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
            )
        }
    }
}

@Preview
@Composable
internal fun AppDialogPreview() {
    PreviewHelper {
        var dialogType by remember { mutableStateOf<DialogType?>(null) }
        androidx.compose.material3.Button(
            onClick = {
                dialogType = DialogType.INFO
            },
            colors =
            ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.status.infoContainer,
                contentColor = AppTheme.colors.status.info,
            ),
        ) {
            Text("Show Info Dialog")
        }
        androidx.compose.material3.Button(
            onClick = {
                dialogType = DialogType.ERROR
            },
            colors =
            ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.status.errorContainer,
                contentColor = AppTheme.colors.status.error,
            ),
        ) {
            Text("Show Error Dialog")
        }

        dialogType?.let {
            AppDialog(
                type = it,
                text = "App Dialog Body text",
                onDismiss = {
                    dialogType = null
                },
                onConfirm = {
                    dialogType = null
                },
            )
        }
    }
}
