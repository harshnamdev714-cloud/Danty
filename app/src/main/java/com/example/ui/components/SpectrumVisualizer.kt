package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun SpectrumVisualizer(
    bands: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("spectrum_visualizer")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val bandCount = bands.size.coerceAtLeast(1)
            val spacing = 3.dp.toPx()
            val totalSpacing = spacing * (bandCount - 1)
            val barWidth = max(2.dp.toPx(), (width - totalSpacing) / bandCount)

            // Draw horizontal dB grid lines
            val gridColor = Color(0x1A00E5FF)
            for (g in 1..4) {
                val y = height * (g / 5f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            for (i in 0 until bandCount) {
                val rawAmp = bands.getOrElse(i) { 0f }
                val barHeight = (rawAmp * (height - 8f)).coerceIn(4f, height)
                val x = i * (barWidth + spacing)
                val y = height - barHeight

                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        TubeAmber,
                        NeonCyan,
                        NeonCyanDim
                    ),
                    startY = y,
                    endY = height
                )

                // Bar body
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )

                // Peak cap dot
                drawRoundRect(
                    color = TubeAmberGlow,
                    topLeft = Offset(x, max(0f, y - 4.dp.toPx())),
                    size = Size(barWidth, 2.5.dp.toPx()),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }

        // Overlay status text
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0x80080C14), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isPlaying) BitPerfectGreen else StudioTextMuted)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isPlaying) "FFT 32-BAND LIVE" else "IDLE",
                color = if (isPlaying) BitPerfectGreen else StudioTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VuMeterStereo(
    leftDbfs: Float,
    rightDbfs: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("vu_meter_stereo"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleVuChannel(label = "L", dbfs = leftDbfs, modifier = Modifier.weight(1f))
        SingleVuChannel(label = "R", dbfs = rightDbfs, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SingleVuChannel(
    label: String,
    dbfs: Float,
    modifier: Modifier = Modifier
) {
    // dbfs from -60 to 0
    val normalized = ((dbfs + 60f) / 60f).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CH $label",
                color = StudioTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${String.format("%.1f", dbfs)} dBFS",
                color = if (dbfs > -3f) VuRed else if (dbfs > -12f) TubeAmber else NeonCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF070B12))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fillWidth = size.width * normalized
                if (fillWidth > 0f) {
                    val brush = Brush.horizontalGradient(
                        colors = listOf(
                            VuGreen,
                            VuYellow,
                            VuRed
                        ),
                        startX = 0f,
                        endX = size.width
                    )
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(0f, 0f),
                        size = Size(fillWidth, size.height),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }
    }
}
