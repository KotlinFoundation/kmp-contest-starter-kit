package com.kotlinfoundation.koko.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ExampleNativeTextView(text: String, modifier: Modifier) {
    Text(text = text, modifier = modifier)
}
