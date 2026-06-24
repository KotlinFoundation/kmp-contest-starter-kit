package com.kotlinfoundation.kmpstarterkit.auth.firebase

import com.kotlinfoundation.kmpstarterkit.auth.api.AuthServiceProviderFactory

internal actual val authServiceProviderFactory: AuthServiceProviderFactory
    get() = AuthServiceProviderFactory { NoOpAuthServiceProvider() }
