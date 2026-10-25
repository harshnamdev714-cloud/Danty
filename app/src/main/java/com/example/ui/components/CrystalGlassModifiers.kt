package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CrystalGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = Color(0x3DF1F5F9),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x2E1E293B),
                        Color(0x180F172A)
                    )
                )
            )
            .border(
                borderWidth,
                Brush.linearGradient(
                    listOf(
                        Color(0x66FFFFFF),
                        Color(0x2238BDF8),
                        Color(0x15FFFFFF),
                        Color(0x33A78BFA)
                    )
                ),
                shape
            )
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
fun CrystalPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    tintColor: Color = NeonCyan
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tintColor.copy(alpha = 0.14f))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        tintColor.copy(alpha = 0.6f),
                        Color(0x33FFFFFF),
                        tintColor.copy(alpha = 0.4f)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = tintColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
