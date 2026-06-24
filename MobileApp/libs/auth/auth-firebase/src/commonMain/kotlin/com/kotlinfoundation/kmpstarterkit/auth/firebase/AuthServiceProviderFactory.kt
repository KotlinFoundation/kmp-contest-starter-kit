package com.kotlinfoundation.kmpstarterkit.auth.firebase

import com.kotlinfoundation.kmpstarterkit.auth.api.AuthServiceProviderFactory

val AuthServiceProviderFactory.Companion.Firebase: AuthServiceProviderFactory
    get() = authServiceProviderFactory

internal expect val authServiceProviderFactory: AuthServiceProviderFactory
