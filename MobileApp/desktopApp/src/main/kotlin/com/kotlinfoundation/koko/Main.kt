package com.kotlinfoundation.koko

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kotlinfoundation.koko.root.App
import com.kotlinfoundation.koko.root.AppInitializer

fun main() {
    System.setProperty("java.util.logging.SimpleFormatter.format", "%5\$s%6\$s%n")

    application {
        AppInitializer.initialize {}
        Window(
            onCloseRequest = ::exitApplication,
            title = "Koko",
        ) {
            App()
        }
    }
}
