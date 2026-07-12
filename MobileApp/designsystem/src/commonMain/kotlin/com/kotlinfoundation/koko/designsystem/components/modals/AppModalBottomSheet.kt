package com.kotlinfoundation.koko.designsystem.components.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.ButtonStyle
import com.kotlinfoundation.koko.designsystem.components.DialogOrBottomSheetTitle
import com.kotlinfoundation.koko.designsystem.components.Divider
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_cancel
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_ok
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_close
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    title: String,
    subTitle: String? = null,
    isDismissable: Boolean = true,
    titleColor: Color = AppTheme.colors.text.primary,
    btnConfirmText: String = stringResource(UiRes.string.btn_ok),
    btnDismissText: String = stringResource(UiRes.string.btn_cancel),
    hideButtons: Boolean = false,
    reverseButtonsOrder: Boolean = false,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = {
            isDismissable
        })
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            if (isDismissable) onDismiss()
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(AppTheme.spacing.defaultSpacing))
                Divider(
                    thickness = 3.dp,
                    modifier =
                    Modifier
                        .width(36.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.outerSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            if (hideButtons) {
                // Title on left with close icon on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TitleAndSubtitleColumn(
                        modifier = Modifier.weight(1f),
                        title = title,
                        subtitle = subTitle,
                        titleColor = titleColor,
                        textAlign = TextAlign.Start,
                    )

                    Icon(
                        painter = painterResource(UiRes.drawable.ic_close),
                        contentDescription = "Close",
                        modifier =
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.outline.copy(alpha = 0.3f))
                            .clickable {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            }.padding(6.dp),
                        tint =
                        AppTheme.colors.text.primary
                            .copy(alpha = 0.7f),
                    )
                }
            } else {
                // Centered title for button mode
                TitleAndSubtitleColumn(
                    title = title,
                    subtitle = subTitle,
                    titleColor = titleColor,
                    textAlign = TextAlign.Center,
                )
            }

            if (!hideButtons) Divider(modifier = Modifier.fillMaxWidth())
            content()

            if (hideButtons) return@Column

            Divider(modifier = Modifier.fillMaxWidth())
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppButton(
                    text = if (reverseButtonsOrder) btnConfirmText else btnDismissText,
                    style = ButtonStyle.ALTERNATIVE,
                    modifier = Modifier.weight(1f),
                ) {
                    if (reverseButtonsOrder) onConfirm() else onDismiss()
                }
                AppButton(
                    text = if (reverseButtonsOrder) btnDismissText else btnConfirmText,
                    modifier = Modifier.weight(1f),
                ) {
                    if (reverseButtonsOrder) onDismiss() else onConfirm()
                }
            }
        }
    }
}

@Composable
private fun TitleAndSubtitleColumn(
    title: String,
    titleColor: Color,
    textAlign: TextAlign,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DialogOrBottomSheetTitle(
            text = title,
            color = titleColor,
            textAlign = textAlign,
        )
        subtitle?.let {
            Text(
                text = it,
                style = AppTheme.typography.bodySmall,
                textAlign = textAlign,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
@Preview
internal fun AppModalBottomSheetPreview() {
    PreviewHelper {
        var isVisible by remember { mutableStateOf(false) }
        var isButtonsVisible by remember { mutableStateOf(true) }
        AppButton("Show Modal Bottom Sheet") {
            isButtonsVisible = true
            isVisible = true
        }
        AppButton("Show Modal Bottom Sheet (No Buttons)") {
            isButtonsVisible = false
            isVisible = true
        }
        if (isVisible) {
            AppModalBottomSheet(
                title = "Modal Bottom Sheet Title",
                hideButtons = !isButtonsVisible,
                onConfirm = {
                    isVisible = false
                },
                onDismiss = {
                    isVisible = false
                },
            ) {
                Text("Modal Bottom Sheet Content")
            }
        }
    }
}
