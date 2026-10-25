package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SpatialAudioDsp
import com.example.model.SpatialCoordinates
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun SpatialSoundstageCanvas(
    coordinates: SpatialCoordinates,
    onCoordinatesChanged: (SpatialCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    val spatialDsp = remember { SpatialAudioDsp() }

    // Pulsing acoustic wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
            .testTag("spatial_soundstage_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val centerX = canvasWidth / 2f
                        val centerY = canvasHeight / 2f
                        val radius = min(centerX, centerY) * 0.85f

                        val touchX = (change.position.x - centerX) / radius
                        val touchY = (change.position.y - centerY) / radius

                        val (azimuth, dist) = spatialDsp.fromNormalizedPlane(touchX, touchY)
                        onCoordinatesChanged(
                            coordinates.copy(
                                azimuthDegrees = azimuth,
                                distanceMeters = dist
                            )
                        )
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            val maxR = min(centerX, centerY) * 0.85f

            // Concentric soundstage rings (1m, 2m, 3m)
            val ringColors = listOf(
                Color(0x1F00E5FF),
                Color(0x2E00E5FF),
                Color(0x3D00E5FF)
            )
            for (idx in 1..3) {
                val r = maxR * (idx / 3f)
                drawCircle(
                    color = ringColors[idx - 1],
                    radius = r,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.2f)
                )
            }

            // Crosshair axes (Front/Back, Left/Right)
            drawLine(
                color = Color(0x2600E5FF),
                start = Offset(centerX, centerY - maxR),
                end = Offset(centerX, centerY + maxR),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0x2600E5FF),
                start = Offset(centerX - maxR, centerY),
                end = Offset(centerX + maxR, centerY),
                strokeWidth = 1f
            )

            // Center listener icon (Headphone Listener)
            drawCircle(
                color = Color(0x3300E5FF),
                radius = 18.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = NeonCyan,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )

            // Listener Direction Chevron (Facing 0° North)
            val chevronPath = Path().apply {
                moveTo(centerX, centerY - 14.dp.toPx())
                lineTo(centerX - 5.dp.toPx(), centerY - 7.dp.toPx())
                lineTo(centerX + 5.dp.toPx(), centerY - 7.dp.toPx())
                close()
            }
            drawPath(chevronPath, color = TubeAmber)

            // Virtual 7.1 Speaker nodes around perimeter
            val speakers = listOf(
                Pair("L", -45f),
                Pair("C", 0f),
                Pair("R", 45f),
                Pair("SR", 110f),
                Pair("SL", -110f)
            )
            for (sp in speakers) {
                val spRad = Math.toRadians((sp.second - 90).toDouble())
                val spX = centerX + (cos(spRad) * maxR).toFloat()
                val spY = centerY + (sin(spRad) * maxR).toFloat()
                drawCircle(
                    color = Color(0x6664748B),
                    radius = 5.dp.toPx(),
                    center = Offset(spX, spY)
                )
            }

            // Current Sound Source Node position
            val (normX, normY) = spatialDsp.toNormalizedPlane(
                coordinates.azimuthDegrees,
                coordinates.distanceMeters
            )
            val sourceX = centerX + normX * maxR
            val sourceY = centerY + normY * maxR

            // Pulsing radiating sound waves from sound node
            val waveR = 8.dp.toPx() + (wavePulse * 28.dp.toPx())
            val waveAlpha = (1f - wavePulse).coerceIn(0f, 1f)
            drawCircle(
                color = TubeAmberGlow.copy(alpha = waveAlpha * 0.7f),
                radius = waveR,
                center = Offset(sourceX, sourceY),
                style = Stroke(width = 2.dp.toPx())
            )

            // Connecting line between Listener and Sound Node
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(NeonCyan, TubeAmber),
                    start = Offset(centerX, centerY),
                    end = Offset(sourceX, sourceY)
                ),
                start = Offset(centerX, centerY),
                end = Offset(sourceX, sourceY),
                strokeWidth = 2f
            )

            // Outer glow for Sound Node
            drawCircle(
                color = TubeAmberGlow.copy(alpha = 0.35f),
                radius = 16.dp.toPx(),
                center = Offset(sourceX, sourceY)
            )
            // Solid center for Sound Node
            drawCircle(
                color = TubeAmber,
                radius = 9.dp.toPx(),
                center = Offset(sourceX, sourceY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(sourceX, sourceY)
            )
        }

        // Telemetry readout badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color(0xD9080C14), RoundedCornerShape(8.dp))
                .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "AZIMUTH: ${coordinates.azimuthDegrees.roundToInt()}°",
                color = TubeAmber,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "DISTANCE: ${String.format("%.2f", coordinates.distanceMeters)} m",
                color = NeonCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "ROOM: ${coordinates.roomAcoustic.displayName}",
                color = StudioTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Touch Hint
        Text(
            text = "DRAG ORB TO POSITION AUDIO",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color(0x80080C14), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            color = StudioTextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
