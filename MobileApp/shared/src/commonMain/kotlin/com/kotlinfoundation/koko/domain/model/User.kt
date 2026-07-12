package com.kotlinfoundation.koko.domain.model

/** The signed-in user. `isAnonymous` is true for guest (anonymous-auth) sessions. */
data class User(
    val id: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val hasPremiumAccess: Boolean = false,
    val isAnonymous: Boolean = false,
)
