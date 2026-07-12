package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

sealed interface IconSource {
    data class Vector(
        val imageVector: ImageVector,
    ) : IconSource

    data class Resource(
        val resId: DrawableResource,
    ) : IconSource

    companion object {
        fun of(imageVector: ImageVector): IconSource = Vector(imageVector)

        fun of(resId: DrawableResource): IconSource = Resource(resId)
    }
}

@Composable
fun IconWrapper(
    icon: IconSource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    when (icon) {
        is IconSource.Vector ->
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint,
            )

        is IconSource.Resource ->
            Icon(
                painter = painterResource(icon.resId),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint,
            )
    }
}
