package com.kotlinfoundation.koko.util

object Constants {

    /**
     * Identifier used to unlock Premium access.
     *
     * - RevenueCat: Entitlement ID
     * - Adapty: Access Level ID
     */
    const val PAYWALL_PREMIUM_ACCESS = "Premium"

    /**
     * Credit pack paywall placement identifier.
     *
     * When showing the credit pack paywall:
     * - In RevenueCat, target this value as the placement (Target by Placement ID).
     * - In Adapty, configure the placement with this exact identifier.
     */
    const val PAYWALL_PLACEMENT_CREDITS_PACK = "credits_pack"

    /**
     * Prefix used to identify credit pack products.
     *
     * Credit pack product IDs must start with this prefix and
     * include a numeric credit, amount and suffix, for example:
     *
     * - credit_pack_50
     * - credit_pack_100_v2
     *
     * The numeric part is extracted after a successful purchase and used
     * to determine how many credits should be added to the user’s account.
     */
    const val CREDIT_PACK_PRODUCT_ID_PREFIX = "credit_pack_"

    /**
     * Default paywall placement.
     *
     * - RevenueCat: Placement is optional.
     * - Adapty: Placement is required.
     *
     * If set to `null`, the provider will fall back to the "default" placement.
     */
    val PAYWALL_PLACEMENT_DEFAULT: String? = null

    /**
     * Optional placement for showing a different paywall during onboarding
     * (for example, a higher-priced or special offer paywall).
     *
     * Set a value like "onboarding" and configure the corresponding placement
     * in RevenueCat or Adapty.
     *
     * If `null`, the default paywall will be used.
     */
    val PAYWALL_PLACEMENT_ONBOARDING: String? = null

    const val LOCAL_DB_STORAGE_NAME = "local_storage.db"

    // DataStore requires the file name to end with ".preferences_pb"
    const val PREFERENCES_STORAGE_NAME = "user_preferences.preferences_pb"

    const val WEB_INTERNAL_FILES_DIR_NAME = "internal_files"

    val subscriptionUrl =
        if (isAndroid) {
            "https://play.google.com/store/account/subscriptions"
        } else {
            "https://apps.apple.com/account/subscriptions"
        }

    val MAX_FILE_UPLOAD_SIZE = 10 * 1024 * 1024L // 10mb
}
