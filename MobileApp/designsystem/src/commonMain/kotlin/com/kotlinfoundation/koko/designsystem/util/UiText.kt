package com.kotlinfoundation.koko.designsystem.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    companion object {
        fun of(value: String?) = Text(value)

        fun of(
            id: StringResource,
            vararg formatArgs: Any,
        ) = Resource(id, *formatArgs)

        /**
         * Plural string resource. The [count] picks the right grammatical form
         * (one/other) and is also passed as the first format arg when the plural
         * uses `%1$d` inside.
         */
        fun of(
            id: PluralStringResource,
            count: Int,
            vararg formatArgs: Any,
        ) = Plural(id, count, formatArgs.toList())

        /**
         * Resource string whose format args are themselves [UiText] — useful when an
         * arg is another translated word (e.g. a localized period unit). Each arg is
         * resolved at render time and passed as a string to [stringResource].
         */
        fun ofComposed(
            id: StringResource,
            vararg formatArgs: UiText,
        ) = ComposedResource(id, formatArgs.toList())

        fun empty() = Text("")
    }

    @get:Composable
    val value: String

    fun isEmpty() = this is Text && this.message.isNullOrEmpty()

    fun isNotEmpty() = !isEmpty()

    class Resource(
        val id: StringResource,
        vararg val formatArgs: Any,
    ) : UiText {
        override val value: String
            @Composable get() = stringResource(resource = id, formatArgs = formatArgs)

        companion object {
            operator fun invoke(id: StringResource, vararg args: Any): Resource = Resource(id, *args)
        }
    }

    class ComposedResource(
        val id: StringResource,
        val formatArgs: List<UiText>,
    ) : UiText {
        override val value: String
            @Composable get() = stringResource(
                resource = id,
                formatArgs = formatArgs.map { it.value }.toTypedArray(),
            )

        companion object {
            operator fun invoke(id: StringResource, vararg args: UiText): ComposedResource = ComposedResource(id, args.toList())
        }
    }

    class Plural(
        val id: PluralStringResource,
        val count: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : UiText {
        override val value: String
            @Composable get() = if (formatArgs.isEmpty()) {
                pluralStringResource(resource = id, quantity = count)
            } else {
                pluralStringResource(
                    resource = id,
                    quantity = count,
                    formatArgs = formatArgs.toTypedArray(),
                )
            }

        companion object {
            operator fun invoke(
                id: PluralStringResource,
                count: Int,
                vararg args: Any,
            ): Plural = Plural(id, count, args.toList())
        }
    }

    class Text(
        val message: String?,
    ) : UiText {
        override val value: String
            @Composable get() = message ?: ""
    }

    private class Combined(
        val parts: List<UiText>,
    ) : UiText {
        override val value: String
            @Composable get() {
                var result = ""
                parts.forEach {
                    result += it.value
                }
                return result
            }
    }

    operator fun plus(other: UiText): UiText = when {
        this is Combined && other is Combined -> Combined(this.parts + other.parts)
        this is Combined -> Combined(this.parts + other)
        other is Combined -> Combined(listOf(this) + other.parts)
        else -> Combined(listOf(this, other))
    }
}
