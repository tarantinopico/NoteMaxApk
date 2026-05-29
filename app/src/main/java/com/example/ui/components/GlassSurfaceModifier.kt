package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 16.dp,
    tint: Color = Color.White.copy(alpha = 0.3f),
    borderColor: Color = Color.White.copy(alpha = 0.5f)
): Modifier = composed {
    val gradient = Brush.verticalGradient(
        colors = listOf(tint, tint.copy(alpha = tint.alpha * 0.5f))
    )
    
    val baseModifier = this
        .clip(shape)
        .background(gradient)

    val blurredModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.blur(blurRadius)
    } else {
        baseModifier
    }

    blurredModifier.border(0.5.dp, borderColor, shape)
}

fun Modifier.glassBorder(shape: Shape = RoundedCornerShape(16.dp)): Modifier = composed {
    this.border(0.5.dp, Color.White.copy(alpha = 0.3f), shape)
}
