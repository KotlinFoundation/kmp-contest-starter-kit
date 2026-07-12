package com.kotlinfoundation.koko.domain.exceptions

/** Thrown when an action needs credits the user doesn't have. Presentation catches it to open the credit-pack paywall. */
class CreditRequiredException : Exception("You are out of credits")
