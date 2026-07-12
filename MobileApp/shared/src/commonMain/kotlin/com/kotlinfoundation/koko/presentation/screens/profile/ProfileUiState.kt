package com.kotlinfoundation.koko.presentation.screens.profile

import com.kotlinfoundation.koko.domain.model.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val deleteUserDialogShown: Boolean = false,
    val errorMessage: String? = null,
) {
    val signInActionRequired: Boolean get() = user == null && isLoading.not()
}

sealed interface ProfileScreenUiEvent {
    data object OnClickDeleteAccount : ProfileScreenUiEvent
}
