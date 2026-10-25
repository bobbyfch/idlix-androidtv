package com.idlix.tv.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.glassSurface(intensity: Int, shape: Shape = RoundedCornerShape(22.dp)): Modifier {
    val alpha = when (intensity.coerceIn(1, 3)) {
        1 -> 0.075f
        3 -> 0.19f
        else -> 0.13f
    }
    val fill = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = alpha),
            Color(0xFF8CB5D9).copy(alpha = alpha * 0.32f),
            Color(0xFF141D2C).copy(alpha = 0.42f)
        )
    )
    val outline = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.34f), Color(0xFF9BC7F5).copy(alpha = 0.13f))
    )
    return this
        .clip(shape)
        .background(fill, shape)
        .border(BorderStroke(1.dp, outline), shape)
}
