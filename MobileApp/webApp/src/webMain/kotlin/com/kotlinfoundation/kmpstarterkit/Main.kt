package com.kotlinfoundation.kmpstarterkit

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kotlinfoundation.kmpstarterkit.root.App
import com.kotlinfoundation.kmpstarterkit.root.AppInitializer

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AppInitializer.initialize {}
    ComposeViewport {
        App()
    }
}
