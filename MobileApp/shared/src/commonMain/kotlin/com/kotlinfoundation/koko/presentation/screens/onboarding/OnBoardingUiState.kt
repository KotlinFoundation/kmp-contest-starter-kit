package com.kotlinfoundation.koko.presentation.screens.onboarding

import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_logo
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.desc_onboarding_page_1
import com.kotlinfoundation.koko.generated.resources.title_onboarding_page_1
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class OnBoardingScreenData(
    val title: StringResource,
    val description: StringResource,
    val imageRes: DrawableResource,
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
        ),
    ),
    val isOnBoardingFinished: Boolean = false,
    // isNewUser distinguishes a fresh completion from an already-onboarded user
    val isNewUser: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface OnBoardingUiEvent {
    data object OnClickStart : OnBoardingUiEvent
}
