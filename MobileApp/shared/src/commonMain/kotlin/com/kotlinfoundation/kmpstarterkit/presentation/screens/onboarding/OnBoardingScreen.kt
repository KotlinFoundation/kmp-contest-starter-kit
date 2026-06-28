package com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LogoImage
import com.kotlinfoundation.kmpstarterkit.designsystem.theme.AppTheme

enum class OnBoardingScreenStyle {
    STYLE1,
    STYLE2,
}

@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    style: OnBoardingScreenStyle,
    viewModel: OnBoardingViewModel,
    onNavigateMain: () -> Unit,
    onNavigatePaywall: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.onBoardIsShown) {
        LaunchedEffect(uiState.onBoardIsShown) {
            onNavigateMain()
        }
    }

    if (uiState.isPremiumRequired) {
        LaunchedEffect(uiState.isPremiumRequired) {
            onNavigatePaywall()
            viewModel.onPaywallEventHandled()
        }
    }

    Crossfade(
        targetState = uiState.isLoading,
        animationSpec = tween(durationMillis = 500),
        modifier = modifier.fillMaxSize(),
        label = "onboardingSplash",
    ) { isLoading ->
        if (isLoading) {
            SplashLogo(modifier = Modifier.fillMaxSize())
        } else {
            when (style) {
                OnBoardingScreenStyle.STYLE1 -> {
                    OnBoardingScreenVariation1(
                        modifier = Modifier.fillMaxSize(),
                        uiState = uiState,
                        onUiEvent = viewModel::onUiEvent,
                    )
                }

                OnBoardingScreenStyle.STYLE2 -> {
                    OnBoardingScreenVariation2(
                        modifier = Modifier.fillMaxSize(),
                        uiState = uiState,
                        onUiEvent = viewModel::onUiEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashLogo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "splashLogoPulse")
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splashLogoScale",
    )
    Box(
        modifier = modifier.background(AppTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        LogoImage(modifier = Modifier.scale(scale))
    }
}
