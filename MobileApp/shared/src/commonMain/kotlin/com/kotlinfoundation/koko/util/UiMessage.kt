package com.kotlinfoundation.koko.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A user-facing message that may be either a localized string resource ([Resource]) or a raw
 * already-resolved string ([Message]). [value] resolves it in a composable context. Used by
 * AppGlobalUiState and screen states for snackbars/errors.
 */
sealed interface UiMessage {

    @get:Composable
    val value: String

    data class Resource(val id: StringResource, val args: List<Any> = emptyList()) : UiMessage {

        override val value: String
            @Composable get() = if (args.isEmpty()) {
                stringResource(id)
            } else {
                stringResource(id, *args.toTypedArray())
            }

        companion object {
            operator fun invoke(id: StringResource, vararg args: Any): Resource = Resource(id, args.toList())
        }
    }

    data class Message(val message: String?) : UiMessage {
        override val value: String
            @Composable get() = message ?: ""
    }
}
