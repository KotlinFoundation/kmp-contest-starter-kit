package com.kotlinfoundation.kmpstarterkit.domain.exceptions

/** Thrown on sign-up when the email already has an account — prompt the user to sign in instead. */
class UserAlreadyExistsException : Exception("Looks like you already have an account with this email. Please, try again signing in")
