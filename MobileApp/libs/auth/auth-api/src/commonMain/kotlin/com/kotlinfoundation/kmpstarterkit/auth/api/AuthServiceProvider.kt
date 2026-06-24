package com.kotlinfoundation.kmpstarterkit.auth.api

import kotlinx.coroutines.flow.Flow

interface AuthServiceProvider {
    val currentUser: AuthProviderUser?
    val currentUserFlow: Flow<AuthProviderUser?>

    suspend fun signInAnonymously()

    suspend fun getCurrentUserToken(forceRefresh: Boolean): String?

    suspend fun logOut()

    suspend fun deleteAccount()
}
