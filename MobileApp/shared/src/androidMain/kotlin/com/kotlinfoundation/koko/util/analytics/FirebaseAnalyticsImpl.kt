package com.kotlinfoundation.koko.util.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsImpl(private val firebaseAnalytics: FirebaseAnalytics) : Analytics {

    override fun logEvent(event: String, params: Map<String, Any>?) {
        val bundle = Bundle().apply {
            for (entry in params ?: emptyMap()) putString(entry.key, entry.value.toString())
        }
        firebaseAnalytics.logEvent(event, bundle)
    }

    override fun setEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
}
