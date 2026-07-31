package com.kotlinfoundation.koko.util.inappreview

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.kotlinfoundation.koko.util.logging.AppLogger

@Composable
actual fun rememberInAppReviewManager(): InAppReviewManager {
    val context: Context = LocalContext.current
    val activity = context as? ComponentActivity
    return remember { InAppReviewManagerImpl(activity = activity) }
}

private class InAppReviewManagerImpl(
    private val activity: ComponentActivity?,
) : InAppReviewManager {

    private var reviewInfo: ReviewInfo? = null
    private var reviewManager: ReviewManager? = null

    init {
        preloadReviewInfo()
    }

    override fun requestReview() {
        if (activity == null) return
        if (reviewInfo != null) launchReviewIfReady() else preloadReviewInfo(showWhenReady = true)
    }

    private fun preloadReviewInfo(showWhenReady: Boolean = false) {
        if (activity == null || reviewInfo != null) return

        reviewManager = ReviewManagerFactory.create(activity.applicationContext)
        val manager = reviewManager ?: return

        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
                AppLogger.d("ReviewInfo loaded.")
                if (showWhenReady) {
                    launchReviewIfReady()
                }
            } else {
                AppLogger.e("Error loading ReviewInfo", task.exception)
            }
        }
    }

    private fun launchReviewIfReady() {
        val activity = this.activity ?: return
        val manager = reviewManager ?: return
        val info = reviewInfo ?: return

        manager.launchReviewFlow(activity, info).addOnFailureListener { e ->
            AppLogger.e("InApp Review launch failed", e)
        }
    }
}
