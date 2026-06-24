package com.kotlinfoundation.kmpstarterkit

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kotlinfoundation.kmpstarterkit.root.App
import com.kotlinfoundation.kmpstarterkit.root.AppInitializer

fun main() = application {
    AppInitializer.initialize {}
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMPStarterKit",
    ) {
        App()
    }
}
