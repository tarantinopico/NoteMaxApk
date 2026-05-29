package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 16.dp,
    tint: Color = Color.White.copy(alpha = 0.6f),
    borderColor: Color = Color.White.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .border(0.5.dp, borderColor, shape)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(blurRadius)
                    .background(tint)
            )
        } else {
            val fallbackGradient = Brush.verticalGradient(
                colors = listOf(tint, tint.copy(alpha = tint.alpha * 0.7f))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(fallbackGradient)
            )
        }
        content()
    }
}
