package com.kotlinfoundation.koko.util

import androidx.compose.runtime.compositionLocalOf
import platform.UIKit.UIViewController

/**
 This factory is used to help to use swift views in compose. It can be used both for UIKit and SwiftUI.
 */
interface NativeViewFactory {
    // Example method. Can be deleted or replaced
    fun createSwiftTextView(text: String): UIViewController

    fun createAdmobBannerView(
        bannerId: String,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: () -> Unit,
    ): UIViewController
}

val LocalNativeViewFactory = compositionLocalOf<NativeViewFactory> {
    error("LocalNativeViewFactory is not provided")
}
