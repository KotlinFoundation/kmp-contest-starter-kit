package com.kotlinfoundation.koko.auth.firebase

import com.kotlinfoundation.koko.auth.api.AuthServiceProviderFactory

internal actual val authServiceProviderFactory: AuthServiceProviderFactory
    get() = AuthServiceProviderFactory { FirebaseAuthServiceProvider() }
