package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpatialCoordinates
import com.example.ui.theme.*
import kotlin.math.*

data class SpatialChannel(
    val id: String,
    val name: String,
    val polarAngleDeg: Float, // 0 = North/Center, 90 = Right, 180 = Rear, 270 = Left
    val nominalDistanceM: Float,
    val isHeightChannel: Boolean = false,
    val isSubwoofer: Boolean = false
)

object SpatialSpeakerLayout {
    // 7.1.4 Dolby Atmos / Auro-3D Spatial Speaker Layout
    val CHANNELS = listOf(
        SpatialChannel("C", "Center", 0f, 2.0f),
        SpatialChannel("FR", "Front R", 30f, 2.2f),
        SpatialChannel("TFR", "Top Front R", 45f, 1.8f, isHeightChannel = true),
        SpatialChannel("SR", "Surround R", 90f, 2.0f),
        SpatialChannel("TBR", "Top Back R", 135f, 1.8f, isHeightChannel = true),
        SpatialChannel("SBR", "Rear R", 150f, 2.2f),
        SpatialChannel("SBL", "Rear L", 210f, 2.2f),
        SpatialChannel("TBL", "Top Back L", 225f, 1.8f, isHeightChannel = true),
        SpatialChannel("SL", "Surround L", 270f, 2.0f),
        SpatialChannel("TFL", "Top Front L", 315f, 1.8f, isHeightChannel = true),
        SpatialChannel("FL", "Front L", 330f, 2.2f),
        SpatialChannel("LFE", "Subwoofer", 0f, 0.5f, isSubwoofer = true)
    )
}

@Composable
fun SpatialPolarRadarPlot(
    coordinates: SpatialCoordinates,
    onCoordinatesChanged: (SpatialCoordinates) -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // Animation for polar wave ripple
    val infiniteTransition = rememberInfiniteTransition(label = "polarRipple")
    val sweepPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepPhase"
    )

    // Calculate energy distribution across 7.1.4 channels
    val channelEnergies = remember(coordinates, isPlaying, sweepPhase) {
        val soundAzimuth = (coordinates.azimuthDegrees + 360f) % 360f
        SpatialSpeakerLayout.CHANNELS.map { ch ->
            if (!isPlaying) {
                0.08f
            } else if (ch.isSubwoofer) {
                0.75f + sin(Math.toRadians(sweepPhase.toDouble())).toFloat() * 0.15f
            } else {
                val angleDiff = abs(ch.polarAngleDeg - soundAzimuth).let { if (it > 180f) 360f - it else it }
                val angularFalloff = (1f - (angleDiff / 130f)).coerceIn(0.05f, 1.0f)
                val distFactor = (1.0f / coordinates.distanceMeters.coerceAtLeast(0.5f)).coerceIn(0.4f, 1.4f)
                (angularFalloff * distFactor * coordinates.soundstageWidth * 0.75f).coerceIn(0.05f, 0.98f)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("spatial_polar_radar_plot"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Polar Coordinate Spatial Radar",
                    color = StudioTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "7.1.4 Immersive Soundstage Channel Vectors",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x2200E5FF))
                    .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "POLAR RADAR LIVE",
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Polar Coordinate Canvas (Recharts Radar style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF060911))
                .border(1.dp, Color(0xFF162035), RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val maxR = min(cx, cy) * 0.82f

                            val dx = change.position.x - cx
                            val dy = change.position.y - cy

                            var deg = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
                            if (deg < 0) deg += 360f

                            val distMeters = ((sqrt(dx * dx + dy * dy) / maxR) * 3.0f).coerceIn(0.5f, 3.0f)
                            val standardAzimuth = if (deg > 180f) deg - 360f else deg

                            onCoordinatesChanged(
                                coordinates.copy(
                                    azimuthDegrees = standardAzimuth,
                                    distanceMeters = distMeters
                                )
                            )
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val cx = width / 2f
                val cy = height / 2f
                val maxR = min(cx, cy) * 0.82f

                // 1. Concentric Range Rings (0.5m, 1.0m, 1.5m, 2.0m, 2.5m, 3.0m)
                val ringLabels = listOf("0.5m", "1.0m", "1.5m", "2.0m", "2.5m", "3.0m")
                for (i in 1..6) {
                    val r = maxR * (i / 6f)
                    drawCircle(
                        color = Color(0x1F00E5FF),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1f)
                    )
                }

                // 2. Radial Spokes (every 30 degrees)
                for (deg in 0 until 360 step 30) {
                    val rad = Math.toRadians((deg - 90).toDouble())
                    val endX = cx + (cos(rad) * maxR).toFloat()
                    val endY = cy + (sin(rad) * maxR).toFloat()
                    drawLine(
                        color = Color(0x1F64748B),
                        start = Offset(cx, cy),
                        end = Offset(endX, endY),
                        strokeWidth = 1f
                    )
                }

                // 3. Rotating radar sweep line
                if (isPlaying) {
                    val sweepRad = Math.toRadians((sweepPhase - 90).toDouble())
                    val sweepX = cx + (cos(sweepRad) * maxR).toFloat()
                    val sweepY = cy + (sin(sweepRad) * maxR).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.5f)),
                            start = Offset(cx, cy),
                            end = Offset(sweepX, sweepY)
                        ),
                        start = Offset(cx, cy),
                        end = Offset(sweepX, sweepY),
                        strokeWidth = 2f
                    )
                }

                // 4. Draw Real-time Polar Energy Polygon (Recharts RadarChart style)
                val polygonPath = Path()
                val polygonPoints = mutableListOf<Offset>()

                SpatialSpeakerLayout.CHANNELS.forEachIndexed { idx, ch ->
                    if (!ch.isSubwoofer) {
                        val energy = channelEnergies[idx]
                        val r = maxR * energy
                        val rad = Math.toRadians((ch.polarAngleDeg - 90).toDouble())
                        val px = cx + (cos(rad) * r).toFloat()
                        val py = cy + (sin(rad) * r).toFloat()
                        polygonPoints.add(Offset(px, py))
                    }
                }

                if (polygonPoints.isNotEmpty()) {
                    polygonPath.moveTo(polygonPoints.first().x, polygonPoints.first().y)
                    for (i in 1 until polygonPoints.size) {
                        polygonPath.lineTo(polygonPoints[i].x, polygonPoints[i].y)
                    }
                    polygonPath.close()

                    // Shaded gradient fill
                    drawPath(
                        path = polygonPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.40f),
                                TubeAmber.copy(alpha = 0.20f),
                                Color(0x0500E5FF)
                            ),
                            center = Offset(cx, cy),
                            radius = maxR
                        )
                    )

                    // Polygon outline
                    drawPath(
                        path = polygonPath,
                        color = NeonCyan,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 5. Draw 7.1.4 Speaker Nodes on Polar Grid
                SpatialSpeakerLayout.CHANNELS.forEachIndexed { idx, ch ->
                    val rad = Math.toRadians((ch.polarAngleDeg - 90).toDouble())
                    val chR = if (ch.isSubwoofer) maxR * 0.22f else maxR * (ch.nominalDistanceM / 3.0f)
                    val nx = cx + (cos(rad) * chR).toFloat()
                    val ny = cy + (sin(rad) * chR).toFloat()

                    val energy = channelEnergies[idx]
                    val dotColor = when {
                        ch.isSubwoofer -> TubeAmberGlow
                        ch.isHeightChannel -> LosslessGold
                        else -> NeonCyan
                    }

                    // Radiating node pulse
                    drawCircle(
                        color = dotColor.copy(alpha = (energy * 0.45f).coerceIn(0.1f, 0.7f)),
                        radius = (5.dp.toPx() + energy * 8.dp.toPx()),
                        center = Offset(nx, ny)
                    )

                    // Channel node center
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = Offset(nx, ny)
                    )
                }

                // 6. Draw Center Listener
                drawCircle(
                    color = Color(0x3300E5FF),
                    radius = 16.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = TubeAmber,
                    radius = 6.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // 7. Active Sound Source Marker
                val soundAzimuth = (coordinates.azimuthDegrees + 360f) % 360f
                val srcRad = Math.toRadians((soundAzimuth - 90).toDouble())
                val srcR = maxR * (coordinates.distanceMeters / 3.0f).coerceIn(0.2f, 1.0f)
                val sx = cx + (cos(srcRad) * srcR).toFloat()
                val sy = cy + (sin(srcRad) * srcR).toFloat()

                // Vector line from listener to target
                drawLine(
                    color = TubeAmber,
                    start = Offset(cx, cy),
                    end = Offset(sx, sy),
                    strokeWidth = 2f
                )

                drawCircle(
                    color = TubeAmberGlow,
                    radius = 12.dp.toPx(),
                    center = Offset(sx, sy),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(sx, sy)
                )
            }

            // Legend / Readout Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0xD9080C14), RoundedCornerShape(6.dp))
                    .border(0.8.dp, StudioCardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "POLAR: (r=${String.format("%.2f", coordinates.distanceMeters)}m, θ=${coordinates.azimuthDegrees.roundToInt()}°)",
                    color = TubeAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CARTESIAN: (x=${String.format("%.2f", coordinates.distanceMeters * sin(Math.toRadians(coordinates.azimuthDegrees.toDouble())))}m, y=${String.format("%.2f", coordinates.distanceMeters * cos(Math.toRadians(coordinates.azimuthDegrees.toDouble())))}m)",
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Channel Levels Grid (7.1.4 discrete meters)
        Text(
            text = "7.1.4 DISCRETE CHANNEL RMS METERS",
            color = StudioTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(SpatialSpeakerLayout.CHANNELS.indices.toList()) { idx ->
                val ch = SpatialSpeakerLayout.CHANNELS[idx]
                val energy = channelEnergies[idx]
                val db = (20.0f * log10(energy.coerceAtLeast(0.001f))).coerceIn(-48f, 0f)

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioSurfaceVariant)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ch.id,
                        color = if (ch.isHeightChannel) LosslessGold else if (ch.isSubwoofer) TubeAmber else NeonCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${db.roundToInt()}dB",
                        color = StudioTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
