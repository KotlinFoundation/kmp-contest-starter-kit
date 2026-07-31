package com.kotlinfoundation.koko.util.extensions

import com.kotlinfoundation.koko.util.Constants.CREDIT_PACK_PRODUCT_ID_PREFIX

/**
 * Extracts the credit amount from a credit-pack product id (e.g. `credit_pack_50`
 * → 50, `credit_pack_100_v2` → 100). Returns null if  does not start
 * with [CREDIT_PACK_PRODUCT_ID_PREFIX] or has no leading digits after the prefix.
 */
fun String.parseCreditAmountFromProductId(): Int? = this
    .substringAfter(CREDIT_PACK_PRODUCT_ID_PREFIX, missingDelimiterValue = "")
    .takeWhile { it.isDigit() }
    .toIntOrNull()

/** True if  is a credit pack (i.e. starts with the configured prefix). */
fun String?.isCreditPackProductId(): Boolean = this?.startsWith(CREDIT_PACK_PRODUCT_ID_PREFIX) == true
