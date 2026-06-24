package com.kotlinfoundation.kmpstarterkit.auth.api

/**
 * User model used specifically by auth providers.
 * Platform-agnostic, does not depend on domain layer.
 */
data class AuthProviderUser(
    val id: String,
    val isAnonymous: Boolean,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
)
