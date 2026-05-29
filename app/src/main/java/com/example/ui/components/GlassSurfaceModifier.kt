package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 16.dp, // Unused now but kept for signature
    tint: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    content: @Composable () -> Unit
) {
    val actualTint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.surface else tint
    val actualBorder = if (borderColor == Color.Unspecified) MaterialTheme.colorScheme.outlineVariant else borderColor

    Surface(
        modifier = modifier.border(0.5.dp, actualBorder, shape),
        shape = shape,
        color = actualTint,
        content = content
    )
}
