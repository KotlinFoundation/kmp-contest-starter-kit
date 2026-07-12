package com.kotlinfoundation.koko.subscription.api

enum class BillingPeriod(val approximateDays: Int?) {
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30),
    TWO_MONTHS(60),
    THREE_MONTHS(90),
    SIX_MONTHS(180),
    YEARLY(365),
    LIFETIME(null),
}

val BillingPeriod?.isRecurring: Boolean
    get() = this != null && this != BillingPeriod.LIFETIME
