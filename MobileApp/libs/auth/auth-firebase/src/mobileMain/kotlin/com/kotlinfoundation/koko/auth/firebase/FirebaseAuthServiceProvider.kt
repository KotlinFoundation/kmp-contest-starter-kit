package com.kotlinfoundation.koko.auth.firebase

import com.kotlinfoundation.koko.auth.api.AuthProviderUser
import com.kotlinfoundation.koko.auth.api.AuthRecentLoginRequiredException
import com.kotlinfoundation.koko.auth.api.AuthServiceProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthServiceProvider : AuthServiceProvider {
    override val currentUser: AuthProviderUser?
        get() = Firebase.auth.currentUser?.asUser()

    override val currentUserFlow: Flow<AuthProviderUser?>
        get() = Firebase.auth.authStateChanged.map { it?.asUser() }

    override suspend fun signInAnonymously() {
        Firebase.auth.signInAnonymously()
    }

    override suspend fun logOut() {
        Firebase.auth.signOut()
    }

    override suspend fun getCurrentUserToken(forceRefresh: Boolean): String? = Firebase.auth.currentUser?.getIdToken(forceRefresh)

    override suspend fun deleteAccount() {
        try {
            val currentUser = Firebase.auth.currentUser
            currentUser?.delete()
        } catch (e: Exception) {
            if (e is FirebaseAuthRecentLoginRequiredException) throw AuthRecentLoginRequiredException()
            throw e
        }
    }

    private fun FirebaseUser.asUser(): AuthProviderUser {
        val emailFromProviders =
            providerData.firstOrNull { it.email.isNullOrEmpty().not() && it.email != "null" }?.email
        val displayNameFromProviders =
            providerData
                .firstOrNull {
                    it.displayName.isNullOrEmpty().not() && it.displayName != "null"
                }?.displayName
        val pictureFromProviders =
            providerData
                .firstOrNull {
                    it.photoURL.isNullOrEmpty().not() && it.photoURL != "null"
                }?.photoURL

        return AuthProviderUser(
            id = uid,
            isAnonymous = isAnonymous,
            email = emailFromProviders ?: email,
            displayName = displayNameFromProviders ?: displayName,
            photoUrl = pictureFromProviders ?: photoURL,
        )
    }
}
