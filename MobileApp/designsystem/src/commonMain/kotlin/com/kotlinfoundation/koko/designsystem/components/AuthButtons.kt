package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_sign_in_with_google
import com.kotlinfoundation.koko.designsystem.theme.LocalThemeIsDark
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.mmk.kmpauth.uihelper.apple.AppleButtonMode
import com.mmk.kmpauth.uihelper.google.GoogleButtonMode
import com.mmk.kmpauth.uihelper.google.GoogleSignInButtonIconOnly
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GoogleSignInButton(
    iconOnly: Boolean = false,
    height: Dp = if (iconOnly) 44.dp else 56.dp,
    shape: Shape = CircleShape,
    textRes: StringResource = UiRes.string.btn_sign_in_with_google,
    onClick: () -> Unit,
) {
    val isDarkMode = LocalThemeIsDark.current
    val mode = if (isDarkMode) GoogleButtonMode.Dark else GoogleButtonMode.Light

    if (iconOnly) {
        GoogleSignInButtonIconOnly(
            onClick = onClick,
            mode = mode,
            modifier = Modifier.size(height),
        )
    } else {
        com.mmk.kmpauth.uihelper.google.GoogleSignInButton(
            modifier = Modifier.fillMaxWidth().height(height),
            fontSize = 24.sp,
            text = stringResource(textRes),
            mode = mode,
            shape = shape,
            onClick = onClick,
        )
    }
}

@Composable
fun AppleSignInButton(
    iconOnly: Boolean = false,
    height: Dp = if (iconOnly) 44.dp else 56.dp,
    shape: Shape = CircleShape,
    textRes: StringResource = UiRes.string.btn_sign_in_with_google,
    onClick: () -> Unit,
) {
    val isDarkMode = LocalThemeIsDark.current
    val mode = if (isDarkMode) AppleButtonMode.White else AppleButtonMode.Black

    if (iconOnly) {
        com.mmk.kmpauth.uihelper.apple.AppleSignInButtonIconOnly(
            onClick = onClick,
            mode = mode,
            modifier = Modifier.size(height),
        )
    } else {
        com.mmk.kmpauth.uihelper.apple.AppleSignInButton(
            modifier = Modifier.fillMaxWidth().height(height),
            text = stringResource(textRes),
            mode = mode,
            shape = shape,
            onClick = onClick,
        )
    }
}

@Composable
@Preview
internal fun AuthButtonsPreview() {
    PreviewHelper {
        GoogleSignInButton { }
        AppleSignInButton { }
        GoogleSignInButton(iconOnly = true) { }
        AppleSignInButton(iconOnly = true) { }
    }
}
