@file:OptIn(ExperimentalTime::class)

package com.kotlinfoundation.kmpstarterkit.data.repository

import com.kotlinfoundation.kmpstarterkit.auth.api.AuthProviderUser
import com.kotlinfoundation.kmpstarterkit.auth.api.AuthServiceProvider
import com.kotlinfoundation.kmpstarterkit.data.BackgroundExecutor
import com.kotlinfoundation.kmpstarterkit.data.source.preferences.UserPreferences
import com.kotlinfoundation.kmpstarterkit.data.source.preferences.UserPreferences.Keys.KEY_FIRST_TIME_USER
import com.kotlinfoundation.kmpstarterkit.domain.exceptions.UnAuthorizedException
import com.kotlinfoundation.kmpstarterkit.domain.model.User
import com.kotlinfoundation.kmpstarterkit.util.ApplicationScope
import com.kotlinfoundation.kmpstarterkit.util.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Owns the authenticated user. Exposes [currentUser] as a hot flow that merges the auth
 * provider's user with premium status from [SubscriptionRepository], and signs guests in
 * anonymously on first launch. Emits `Result.failure(UnAuthorizedException)` when signed out.
 */
class UserRepository(
    private val authServiceProvider: AuthServiceProvider,
    private val subscriptionRepository: SubscriptionRepository,
    private val userPreferences: UserPreferences,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
    private val applicationScope: ApplicationScope,
) {

    init {
        signInAnonymouslyIfNecessary()
    }

    private val authTrigger = MutableStateFlow(Clock.System.now().toEpochMilliseconds())

    val currentUser: SharedFlow<Result<User>> =
        combine(authTrigger, authServiceProvider.currentUserFlow) { _, currentUser -> currentUser }
            .map { currentUser ->
                AppLogger.d("CUrrent user is updated")
                if (currentUser == null) {
                    Result.failure(UnAuthorizedException())
                } else {
                    subscriptionRepository.login(userId = currentUser.id)
                    val user = currentUser.asUser()
                        .copy(hasPremiumAccess = subscriptionRepository.hasPremiumAccess())
                    Result.success(user)
                }
            }.shareIn(applicationScope, SharingStarted.Eagerly, 1)

    fun signInAnonymouslyIfNecessary() = applicationScope.launch {
        backgroundExecutor.execute {
            val isFirstTimeUser = userPreferences.getBoolean(KEY_FIRST_TIME_USER, true)
            if (authServiceProvider.currentUser == null && isFirstTimeUser) {
                authServiceProvider.signInAnonymously()
                userPreferences.putBoolean(KEY_FIRST_TIME_USER, false)
                AppLogger.d("Signed in anonymously")
            }
            Result.success(Unit)
        }.onFailure {
            AppLogger.e("signInAnonymouslyIfNecessary exception ${it.message}")
        }
    }

    // This is added because when linking anonymous account with google account, firebase listener is not triggered
    fun onSuccessfulOauthSign() {
        applicationScope.launch { authTrigger.emit(Clock.System.now().toEpochMilliseconds()) }
    }

    suspend fun continueAsGuest(): Result<Unit> = backgroundExecutor.execute {
        if (authServiceProvider.currentUser == null) {
            authServiceProvider.signInAnonymously()
        }
        Result.success(Unit)
    }

    suspend fun logOut() = backgroundExecutor.execute {
        subscriptionRepository.logOut()
        authServiceProvider.logOut()
        Result.success(Unit)
    }

    suspend fun deleteAccount() = backgroundExecutor.execute {
        // Here you can send delete request to the server if needed
        authServiceProvider.deleteAccount()
        logOut()
        Result.success(Unit)
    }

    private fun AuthProviderUser.asUser(): User = User(
        id = id,
        isAnonymous = isAnonymous,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
    )
}
