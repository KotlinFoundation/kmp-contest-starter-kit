package com.kotlinfoundation.koko.auth.api

fun interface AuthServiceProviderFactory {
    companion object {}

    fun create(): AuthServiceProvider
}
