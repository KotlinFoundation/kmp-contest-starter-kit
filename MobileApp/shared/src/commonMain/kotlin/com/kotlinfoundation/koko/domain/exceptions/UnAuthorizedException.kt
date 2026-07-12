package com.kotlinfoundation.koko.domain.exceptions

/** Thrown when an action requires a signed-in user. Presentation catches it to route to sign-in. */
class UnAuthorizedException : Exception("You need to be logged in to do this operation")
