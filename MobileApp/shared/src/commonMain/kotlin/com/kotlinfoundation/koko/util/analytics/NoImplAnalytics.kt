package com.kotlinfoundation.koko.util.analytics

// Fallback analytics for platforms without a backend (desktop/web): just logs to stdout.
object NoImplAnalytics : Analytics {
    private var isEnabled = true

    override fun logEvent(
        event: String,
        params: Map<String, Any>?,
    ) {
        if (!isEnabled) return
        println("Event: $event, params: $params. Note: Analytics is not implemented in this platform, so no event will be tracked.")
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
