package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackItem
import com.example.model.DacHardwareInfo
import com.example.ui.theme.*

@Composable
fun DacStatusBadge(
    track: AudioTrackItem?,
    dacInfo: DacHardwareInfo,
    isBitPerfectActive: Boolean,
    isSpatialActive: Boolean,
    onToggleBitPerfect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
            .testTag("dac_status_badge")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hi-Res Lossless badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if ((track?.bitDepth ?: 16) >= 24) NeonCyanDim.copy(alpha = 0.35f) else Color(0x3364748B))
                        .border(1.dp, if ((track?.bitDepth ?: 16) >= 24) NeonCyan else StudioTextMuted, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if ((track?.bitDepth ?: 16) >= 32) "32-BIT FLOAT" else "${track?.bitDepth ?: 24}-BIT LOSSLESS",
                        color = if ((track?.bitDepth ?: 16) >= 24) NeonCyan else StudioTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TubeAmber.copy(alpha = 0.15f))
                        .border(1.dp, TubeAmber.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(track?.sampleRateHz ?: 96000) / 1000} kHz",
                        color = TubeAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track?.format?.displayName ?: "FLAC",
                        color = StudioTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bit-Perfect Direct switch
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isBitPerfectActive) BitPerfectGreen.copy(alpha = 0.18f) else Color(0x2264748B))
                    .border(1.dp, if (isBitPerfectActive) BitPerfectGreen else StudioTextMuted, RoundedCornerShape(6.dp))
                    .clickable { onToggleBitPerfect() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(if (isBitPerfectActive) BitPerfectGreen else StudioTextMuted)
                )
                Text(
                    text = if (isBitPerfectActive) "BIT-PERFECT" else "DSP MODE",
                    color = if (isBitPerfectActive) BitPerfectGreen else StudioTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hardware Route & Latency Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = StudioTextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = dacInfo.deviceName,
                    color = StudioTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "LATENCY: ${dacInfo.bufferLatencyMs}ms",
                color = StudioTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
