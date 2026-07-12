package com.kotlinfoundation.koko.auth.firebase

import com.kotlinfoundation.koko.auth.api.AuthServiceProviderFactory

val AuthServiceProviderFactory.Companion.Firebase: AuthServiceProviderFactory
    get() = authServiceProviderFactory

internal expect val authServiceProviderFactory: AuthServiceProviderFactory
