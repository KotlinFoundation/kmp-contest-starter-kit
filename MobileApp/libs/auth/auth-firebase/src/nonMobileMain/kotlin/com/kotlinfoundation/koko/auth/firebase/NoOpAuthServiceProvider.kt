package com.kotlinfoundation.koko.auth.firebase

import com.kotlinfoundation.koko.auth.api.AuthProviderUser
import com.kotlinfoundation.koko.auth.api.AuthServiceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op implementation for JVM / Web / WASM
 * Does nothing but keeps the API compatible.
 */
class NoOpAuthServiceProvider : AuthServiceProvider {
    private val _currentUserFlow = MutableStateFlow<AuthProviderUser?>(null)
    override val currentUserFlow: Flow<AuthProviderUser?> = _currentUserFlow.asStateFlow()

    override val currentUser: AuthProviderUser? get() = null

    override suspend fun signInAnonymously() {
        // No-op
    }

    override suspend fun getCurrentUserToken(forceRefresh: Boolean): String? = null

    override suspend fun logOut() {
        // No-op
    }

    override suspend fun deleteAccount() {
        // No-op
    }
}
