package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.EqualizerPreset
import com.example.dsp.StudioEqualizer
import com.example.model.EqualizerBand
import com.example.ui.UiState
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun StudioRackScreen(
    uiState: UiState,
    onBandChanged: (Int, Float) -> Unit,
    onPresetSelected: (EqualizerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var tubeWarmthEnabled by remember { mutableStateOf(true) }
    var preampGainDb by remember { mutableFloatStateOf(0.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Studio DSP & Mastering Rack",
                    color = StudioTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "10-Band Parametric EQ & Analog Tube Saturation",
                    color = TubeAmber,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { onPresetSelected(StudioEqualizer.PRESETS.first()) },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                    .testTag("reset_eq_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset EQ",
                    tint = StudioTextSecondary
                )
            }
        }

        // Live Frequency Response Curve Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val midY = height / 2f

                // Center 0dB dashed line
                drawLine(
                    color = Color(0x3300E5FF),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 1f
                )

                // Draw smooth frequency response spline
                val bands = uiState.equalizerBands
                if (bands.isNotEmpty()) {
                    val path = Path()
                    val stepX = width / (bands.size - 1).coerceAtLeast(1)

                    bands.forEachIndexed { i, band ->
                        // gainDb is -12 to +12
                        val y = midY - (band.gainDb / 12f) * (midY - 10f)
                        val x = i * stepX
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevY = midY - (bands[i - 1].gainDb / 12f) * (midY - 10f)
                            val ctrlX1 = prevX + (x - prevX) / 2f
                            val ctrlX2 = prevX + (x - prevX) / 2f
                            path.cubicTo(ctrlX1, prevY, ctrlX2, y, x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            listOf(NeonCyan, TubeAmber, BitPerfectGreen)
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Text(
                text = "FREQUENCY RESPONSE: ${uiState.currentPreset.uppercase()}",
                color = NeonCyan,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // Presets Chips
        Text(
            text = "AUDIOPHILE TARGET CURVES",
            color = StudioTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StudioEqualizer.PRESETS) { preset ->
                val selected = uiState.currentPreset == preset.name
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) TubeAmber.copy(alpha = 0.2f) else StudioSurface)
                        .border(
                            1.dp,
                            if (selected) TubeAmber else StudioCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onPresetSelected(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("preset_${preset.name.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = preset.name,
                        color = if (selected) TubeAmber else StudioTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // 10-Band Sliders Console
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "10-BAND GRAPHIC EQUALIZER (-12dB to +12dB)",
                color = StudioTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable row of sliders
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.equalizerBands.indices.toList()) { index ->
                    val band = uiState.equalizerBands[index]
                    BandSliderItem(
                        band = band,
                        onGainChanged = { gain -> onBandChanged(index, gain) }
                    )
                }
            }
        }

        // Analog Tube Saturation & Preamp Module
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ANALOG MASTERING EFFECTS",
                color = StudioTextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // Tube Warmth Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vacuum Tube Triode Warmth",
                        color = StudioTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Simulates 12AX7 even-order harmonic saturation and soft clip",
                        color = StudioTextMuted,
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = tubeWarmthEnabled,
                    onCheckedChange = { tubeWarmthEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioBackground,
                        checkedTrackColor = TubeAmber
                    )
                )
            }

            // Preamp Gain Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Master Pre-amp Gain",
                        color = StudioTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${if (preampGainDb >= 0) "+" else ""}${String.format("%.1f", preampGainDb)} dB",
                        color = TubeAmber,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = preampGainDb,
                    onValueChange = { preampGainDb = it },
                    valueRange = -6.0f..6.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = TubeAmber,
                        activeTrackColor = TubeAmber,
                        inactiveTrackColor = StudioCardBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun BandSliderItem(
    band: EqualizerBand,
    onGainChanged: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(42.dp)
    ) {
        Text(
            text = "${if (band.gainDb >= 0) "+" else ""}${band.gainDb.roundToInt()}",
            color = if (band.gainDb != 0f) NeonCyan else StudioTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        // Vertical Slider representation using custom slider box
        Slider(
            value = band.gainDb,
            onValueChange = onGainChanged,
            valueRange = -12.0f..12.0f,
            modifier = Modifier
                .height(130.dp)
                .testTag("eq_band_${band.frequencyHz}"),
            colors = SliderDefaults.colors(
                thumbColor = if (band.gainDb != 0f) NeonCyan else StudioTextSecondary,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = StudioCardBorder
            )
        )

        val freqLabel = if (band.frequencyHz >= 1000) {
            "${band.frequencyHz / 1000}k"
        } else {
            "${band.frequencyHz}"
        }

        Text(
            text = freqLabel,
            color = StudioTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
