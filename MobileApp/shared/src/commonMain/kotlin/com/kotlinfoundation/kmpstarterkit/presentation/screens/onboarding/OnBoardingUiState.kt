package com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding

import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.UiRes
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_logo
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.desc_onboarding_page_1
import com.kotlinfoundation.kmpstarterkit.generated.resources.title_onboarding_page_1
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class OnBoardingScreenData(
    val title: StringResource,
    val description: StringResource,
    val imageRes: DrawableResource,
    val isGetPremiumButtonVisible: Boolean = false,
)

data class OnBoardingUiState(

    val pages: List<OnBoardingScreenData> = listOf(
        OnBoardingScreenData(
            Res.string.title_onboarding_page_1,
            Res.string.desc_onboarding_page_1,
            UiRes.drawable.ic_logo,
        ),
        OnBoardingScreenData(
            Res.string.title_onboarding_page_1,
            Res.string.desc_onboarding_page_1,
            UiRes.drawable.ic_logo,
        ),
        OnBoardingScreenData(
            Res.string.title_onboarding_page_1,
            Res.string.desc_onboarding_page_1,
            UiRes.drawable.ic_logo,
            isGetPremiumButtonVisible = true,
        ),
    ),
    val onBoardIsShown: Boolean = false,
    val isPremiumRequired: Boolean = false,
    val isLoading: Boolean = true,
)

sealed class OnBoardingUiEvent {
    data object OnClickStart : OnBoardingUiEvent()
    data object OnClickGetPremiumAccess : OnBoardingUiEvent()
}
