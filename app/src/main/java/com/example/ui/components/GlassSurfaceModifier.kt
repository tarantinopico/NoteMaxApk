package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    blurRadius: Float = 20f
): Modifier = composed {
    val baseModifier = this
        .clip(shape)
        .background(backgroundColor, shape)

    val blurredModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.blur(blurRadius.dp)
    } else {
        baseModifier
    }

    blurredModifier.border(0.5.dp, borderColor, shape)
}

fun Modifier.glassBorder(shape: Shape = RoundedCornerShape(16.dp)): Modifier = composed {
    this.border(0.5.dp, Color.White.copy(alpha = 0.3f), shape)
}
