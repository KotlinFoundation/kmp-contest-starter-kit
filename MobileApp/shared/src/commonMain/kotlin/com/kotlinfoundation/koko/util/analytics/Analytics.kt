package com.kotlinfoundation.koko.util.analytics

// Analytics abstraction (Firebase Analytics on mobile, no-op elsewhere). Event/param name
// constants live in the companion; use logScreenView for the common screen-view event.
interface Analytics {
    fun logEvent(event: String, params: Map<String, Any>? = emptyMap())
    fun setEnabled(enabled: Boolean = true)

    companion object {
        const val EVENT_SCREEN_VIEW = "screen_view"
        const val PARAM_SCREEN_NAME = "screen_name"
        const val EVENT_PAYWALL_DISMISSED = "paywall_dismissed"
        const val PARAM_NB_PAYWALL_DISMISSED = "nb_paywall_dismissed"
        const val EVENT_CLICKED_GENERATE = "clicked_generate"
        const val EVENT_CLICKED_REPORT_AI_CONTENT = "clicked_report_ai_content"
    }
}

fun Analytics.logScreenView(screenName: String, params: Map<String, Any>? = emptyMap()) {
    logEvent(
        event = Analytics.EVENT_SCREEN_VIEW,
        params = mapOf(Analytics.PARAM_SCREEN_NAME to screenName) + (params ?: emptyMap()),
    )
}
