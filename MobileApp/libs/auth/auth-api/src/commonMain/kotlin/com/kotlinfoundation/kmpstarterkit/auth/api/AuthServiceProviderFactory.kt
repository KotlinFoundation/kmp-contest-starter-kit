package com.kotlinfoundation.kmpstarterkit.auth.api

fun interface AuthServiceProviderFactory {
    companion object {}

    fun create(): AuthServiceProvider
}
