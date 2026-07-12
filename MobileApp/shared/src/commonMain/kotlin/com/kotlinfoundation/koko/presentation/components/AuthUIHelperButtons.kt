package com.kotlinfoundation.koko.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.AuthProvider

@Composable
fun AuthUIHelperButtons(
    modifier: Modifier = Modifier,
    authProviders: List<AuthProvider> = AuthProvider.entries,
    shape: Shape = CircleShape,
    height: Dp = 56.dp,
    spaceBetweenButtons: Dp = AppTheme.spacing.groupedVerticalElementSpacing,
    autoClickEnabledIfOneProviderExists: Boolean = true,
    linkAccount: Boolean = false,
    onResult: (Result<Unit>) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spaceBetweenButtons)) {
        val isExistOnlyOneAuthProvider by remember { mutableStateOf(authProviders.size == 1) }
        val updatedOnResult by rememberUpdatedState(onResult)

        authProviders.forEach { authProvider ->
            AuthUiButton(
                authProvider = authProvider,
                shape = shape,
                height = height,
                linkAccount = linkAccount,
                autoClickEnabled = isExistOnlyOneAuthProvider && autoClickEnabledIfOneProviderExists,
                onResult = updatedOnResult,
            )
        }
    }
}

@Composable
expect fun AuthUiButton(
    authProvider: AuthProvider,
    shape: Shape,
    height: Dp,
    linkAccount: Boolean,
    autoClickEnabled: Boolean,
    onResult: (Result<Unit>) -> Unit,
)
