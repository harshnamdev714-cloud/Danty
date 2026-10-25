package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.AudioTrackItem
import com.example.model.DacHardwareInfo
import com.example.model.HiResFilterType
import com.example.model.HiResUpsamplerConfig
import com.example.ui.theme.*

@Composable
fun HiResGoldBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0F0E08))
            .border(1.5.dp, Brush.linearGradient(listOf(LosslessGold, TubeAmber, Color(0xFFFFE082))), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("hi_res_gold_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Hi-Res",
                color = LosslessGold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(LosslessGold)
                    .padding(horizontal = 2.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "AUDIO",
                    color = Color.Black,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun HiResAudioConsole(
    track: AudioTrackItem?,
    dacInfo: DacHardwareInfo,
    upsamplerConfig: HiResUpsamplerConfig,
    onFilterSelected: (HiResFilterType) -> Unit,
    onToggleUpsampler: (Boolean) -> Unit,
    onTargetSampleRateSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("hi_res_audio_console"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Hi-Res Gold Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HiResGoldBadge()
                Column {
                    Text(
                        text = "Hi-Res Audio Studio Engine",
                        color = StudioTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "JAS Standard: 24/32-Bit • ≥96kHz • Direct DAC",
                        color = LosslessGold,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x22FFD166))
                    .border(1.dp, LosslessGold, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "DR${track?.dynamicRangeDr ?: 14} MASTER",
                    color = LosslessGold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Complete Audio Signal Chain Flow
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSurfaceVariant)
                .padding(10.dp)
        ) {
            Text(
                text = "HI-RES AUDIO SIGNAL PATH",
                color = StudioTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignalNode(
                    stage = "SOURCE",
                    desc = "${track?.bitDepth ?: 24}b/${(track?.sampleRateHz ?: 96000) / 1000}k",
                    color = NeonCyan
                )
                Text("➔", color = StudioTextMuted, fontSize = 11.sp)
                SignalNode(
                    stage = "DSP CORE",
                    desc = if (upsamplerConfig.isEnabled) "${upsamplerConfig.targetSampleRateHz / 1000}k 32b" else "Direct",
                    color = TubeAmber
                )
                Text("➔", color = StudioTextMuted, fontSize = 11.sp)
                SignalNode(
                    stage = "FILTER",
                    desc = upsamplerConfig.filterType.name.take(7),
                    color = LosslessGold
                )
                Text("➔", color = StudioTextMuted, fontSize = 11.sp)
                SignalNode(
                    stage = "DAC OUT",
                    desc = if (dacInfo.isBitPerfectActive) "Bit-Perfect" else "Direct",
                    color = BitPerfectGreen
                )
            }
        }

        // Digital Reconstruction Filters (ESS Sabre / AKM velvet sound style)
        Column {
            Text(
                text = "DAC RECONSTRUCTION FILTER",
                color = StudioTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HiResFilterType.values()) { filter ->
                    val isSelected = upsamplerConfig.filterType == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LosslessGold.copy(alpha = 0.2f) else StudioSurfaceVariant)
                            .border(1.dp, if (isSelected) LosslessGold else StudioCardBorder, RoundedCornerShape(8.dp))
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = filter.displayName,
                                color = if (isSelected) LosslessGold else StudioTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Real-time Resampler / Upsampler Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ultra Hi-Res Resampler Engine",
                    color = StudioTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Upsamples media to 192kHz/384kHz 32-bit Float with TPDF dither",
                    color = StudioTextMuted,
                    fontSize = 9.sp
                )
            }
            Switch(
                checked = upsamplerConfig.isEnabled,
                onCheckedChange = onToggleUpsampler,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StudioBackground,
                    checkedTrackColor = LosslessGold
                )
            )
        }

        // Target Sample Rate Selection (when upsampler enabled)
        if (upsamplerConfig.isEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET FS:",
                    color = StudioTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                listOf(96000, 192000, 384000).forEach { rate ->
                    val selected = upsamplerConfig.targetSampleRateHz == rate
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) NeonCyan.copy(alpha = 0.2f) else StudioSurfaceVariant)
                            .border(1.dp, if (selected) NeonCyan else StudioCardBorder, RoundedCornerShape(6.dp))
                            .clickable { onTargetSampleRateSelected(rate) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${rate / 1000} kHz",
                            color = if (selected) NeonCyan else StudioTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalNode(
    stage: String,
    desc: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stage,
            color = StudioTextMuted,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = desc,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
