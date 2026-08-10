package com.kotlinfoundation.koko.root

import com.kotlinfoundation.koko.subscription.config.activeSubscriptionProviderFactory

/**
 * Per-app configuration a developer sets/flips when spinning up an app on this kit — behavioral toggles,
 * backend URLs, contact/legal info, and the auth/subscription provider selectors.
 *
 * Compile-time (baked into the binary). This is distinct from:
 * - `Constants` — framework details unlikely to change per app (DB/prefs file names, paywall
 *   entitlement/placement ids, product-id conventions).
 * - `FeatureFlagManager` — runtime flags controllable remotely via Firebase Remote Config.
 */
object AppConfiguration {

    // TODO(publish): your live privacy policy URL — stores reject placeholders (`publishing` skill).
    const val URL_PRIVACY_POLICY = ""

    // TODO(publish): your live terms & conditions URL (`publishing` skill).
    const val URL_TERMS_CONDITIONS = ""

    // TODO(publish): your real support email — ships as boilerplate (`publishing` skill).
    const val CONTACT_EMAIL = "support@example.com"

    // TODO(publish): numeric App Store id, available once the app exists in App Store Connect
    // (`setup-appstore-connect` skill).
    const val APPSTORE_APP_ID = ""

    /**
     * How AI (OpenAI/Replicate) calls are routed.
     *
     * - `null` (AUTO, default): use the Cloud Functions proxy only when [CLOUD_FUNCTIONS_URL] is set;
     *   if it's blank, call the provider directly from the device (the proxy is useless without a URL).
     * - `true`: always use the proxy.
     * - `false`: always call the provider directly.
     *
     * Keep the proxy for production so API keys stay in Secret Manager, never in the app binary. Direct
     * mode is a no-Firebase prototyping shortcut (the key ships in the app) — see `integrate-web-proxy`.
     */
    val USE_AI_PROXY_SERVER: Boolean? = null

    /**
     * CLOUD_FUNCTIONS_URL should be something like: "https://REGION-PROJECT_ID.cloudfunctions.net"
     * Regions:
     * US(Default): us-central1
     * EU: europe-west1
     *
     * This is used AI proxy functions such as OpenAi, Replicate
     */
    // TODO(integrations): deployed Cloud Functions base URL. Blank is fine to start — direct mode
    // works without Firebase (`integrate-web-proxy` skill).
    const val CLOUD_FUNCTIONS_URL = ""

    // Enables Apple and Google sign-in. If false, only anonymous login is supported.
    // Default false — anonymous auth is the easiest path to a working app (just Firebase +
    // Anonymous sign-in). Flip to true to add Google/Apple (see the enable-auth skill for the
    // extra config: GOOGLE_WEB_CLIENT_ID, iOS Info.plist client IDs, Sign In with Apple capability).
    const val AUTH_SOCIAL_LOGIN_ENABLED = false

    /**
     * Whether the app has any **premium (paid/gated) features**.
     *
     * - `false` (default): **no premium features** — everything is unlocked and free. No paywall,
     *   no subscriptions/upgrade UI, the subscription SDK never initializes, and credits are off
     *   (generation is free, credit UI hidden). Nothing to configure to run the app.
     * - `true`: the app HAS premium features — subscriptions, paywall, and credits are available
     *   to gate or limit features. (The app itself can still be free to download.) Flip this in
     *   the monetization phase, once the store products exist (see the `monetization` skill).
     *
     * This is about premium *features*, not the app's price.
     */
    const val PREMIUM_FEATURES_ENABLED = false

    /**
     * The subscription provider is chosen in ONE place — the `SUBSCRIPTION_PROVIDER`
     * Gradle property in `gradle.properties` (default: `ADAPTY`):
     *
     *   SUBSCRIPTION_PROVIDER=ADAPTY
     *   or
     *   SUBSCRIPTION_PROVIDER=REVENUECAT
     *
     *   Also in local.properties add the following:
     *
     *   SUBSCRIPTION_PROVIDER_ANDROID_API_KEY=YOUR_API_KEY
     *   SUBSCRIPTION_PROVIDER_IOS_API_KEY=YOUR_API_KEY
     *
     * That property decides which provider module is on the classpath; this accessor
     * resolves to whichever one is linked. Do NOT name a concrete provider here.
     */
    val subscriptionProviderFactory get() = activeSubscriptionProviderFactory
}
