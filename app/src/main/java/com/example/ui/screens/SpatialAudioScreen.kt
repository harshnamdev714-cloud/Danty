package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoomAcoustic
import com.example.model.SpatialCoordinates
import com.example.ui.UiState
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.CrystalPillBadge
import com.example.ui.components.SpatialPolarRadarPlot
import com.example.ui.components.SpatialSoundstageCanvas
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SpatialAudioScreen(
    uiState: UiState,
    onCoordinatesChanged: (SpatialCoordinates) -> Unit,
    onRoomSelected: (RoomAcoustic) -> Unit,
    onToggleCrossfeed: (Boolean) -> Unit,
    onSetSoundstageWidth: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coords = uiState.spatialCoordinates
    var activeViewMode by remember { mutableStateOf(0) } // 0 = 3D Soundfield, 1 = Polar Coordinate Radar (7.1.4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Clear Crystal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "3D Spatial Soundstage",
                    color = StudioTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Spherical HRTF • Polar Radar • Room Convolution",
                    color = CrystalIce,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            CrystalPillBadge(text = "CRYSTAL DSP ON", tintColor = NeonCyan)
        }

        // View Mode Segmented Selector (3D Orbital Stage vs Polar Radar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x1F1E293B))
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0x44FFFFFF), Color(0x2238BDF8), Color(0x33A78BFA))
                    ),
                    RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeViewMode == 0) Color(0x3338BDF8) else Color.Transparent)
                    .clickable { activeViewMode = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "3D Soundfield Stage",
                    color = if (activeViewMode == 0) CrystalIce else StudioTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeViewMode == 1) Color(0x33FBBF24) else Color.Transparent)
                    .clickable { activeViewMode = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Polar Radar (7.1.4 Channels)",
                    color = if (activeViewMode == 1) TubeAmber else StudioTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Visualization Component
        if (activeViewMode == 0) {
            // Interactive 3D Soundstage Touch Canvas
            SpatialSoundstageCanvas(
                coordinates = coords,
                onCoordinatesChanged = onCoordinatesChanged,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Real-Time Polar Coordinate Radar Plot (7.1.4 spatial audio channels)
            SpatialPolarRadarPlot(
                coordinates = coords,
                onCoordinatesChanged = onCoordinatesChanged,
                isPlaying = uiState.isPlaying,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Presets Quick Buttons in Crystalline Glass
        Text(
            text = "SPATIAL SOUNDSTAGE PRESETS",
            color = StudioTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpatialPresetButton(
                label = "Front Stage",
                onClick = {
                    onCoordinatesChanged(coords.copy(azimuthDegrees = 0f, distanceMeters = 1.5f))
                },
                modifier = Modifier.weight(1f)
            )
            SpatialPresetButton(
                label = "Wide Stereo",
                onClick = {
                    onCoordinatesChanged(coords.copy(azimuthDegrees = 45f, distanceMeters = 2.2f, soundstageWidth = 1.8f))
                },
                modifier = Modifier.weight(1f)
            )
            SpatialPresetButton(
                label = "Binaural Orbit",
                onClick = {
                    onCoordinatesChanged(coords.copy(azimuthDegrees = 120f, distanceMeters = 1.0f))
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Room Acoustics selector
        Text(
            text = "CRYSTAL ROOM REVERBERATION",
            color = StudioTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(RoomAcoustic.values()) { room ->
                val selected = coords.roomAcoustic == room
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0x33FBBF24) else Color(0x1F1E293B))
                        .border(
                            1.dp,
                            if (selected) TubeAmber else Color(0x22FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onRoomSelected(room) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("room_${room.name.lowercase()}")
                ) {
                    Column {
                        Text(
                            text = room.displayName,
                            color = if (selected) TubeAmber else StudioTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Decay: ${room.reverbTimeSeconds}s • ${room.roomSizeMeters.toInt()}m",
                            color = StudioTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Soundstage Controls Card in Clear Crystal Glass
        CrystalGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "DSP ACOUSTIC PARAMETERS",
                    color = StudioTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Soundstage Width Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Soundstage Width Expander",
                            color = StudioTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(coords.soundstageWidth * 100).roundToInt()}%",
                            color = CrystalIce,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = coords.soundstageWidth,
                        onValueChange = onSetSoundstageWidth,
                        valueRange = 0.8f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = CrystalIce,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0x3364748B)
                        )
                    )
                }

                // Binaural Crossfeed Switch & Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Binaural Crossfeed (Bauer/Moy)",
                            color = StudioTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Eliminates headphone acoustic fatigue via sub-bass cross-blending",
                            color = StudioTextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = coords.crossfeedEnabled,
                        onCheckedChange = onToggleCrossfeed,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StudioBackground,
                            checkedTrackColor = TubeAmber
                        )
                    )
                }

                // Android Hardware Spatializer info card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1F0F172A))
                        .border(0.8.dp, Color(0x3334D399), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BitPerfectGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Android AudioTrack Spatializer API connected. Spherical ITD/ILD convolution active at 60Hz update rate.",
                        color = StudioTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SpatialPresetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x1F1E293B))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color(0x44FFFFFF), Color(0x15FFFFFF), Color(0x33A78BFA))
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = StudioTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
