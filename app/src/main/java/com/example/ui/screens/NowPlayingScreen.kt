package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrackItem
import com.example.model.HiResFilterType
import com.example.model.TrackDownloadState
import com.example.ui.ScreenTab
import com.example.ui.UiState
import com.example.ui.components.DacStatusBadge
import com.example.ui.components.HiResAudioConsole
import com.example.ui.components.HiResGoldBadge
import com.example.ui.components.SpectrumVisualizer
import com.example.ui.components.VuMeterStereo
import com.example.ui.theme.*

@Composable
fun NowPlayingScreen(
    uiState: UiState,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onToggleBitPerfect: () -> Unit,
    onFilterSelected: (HiResFilterType) -> Unit,
    onToggleUpsampler: (Boolean) -> Unit,
    onTargetSampleRateSelected: (Int) -> Unit,
    onDownloadTrack: (AudioTrackItem) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onNavigateToTab: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val track = uiState.currentTrack
    var isLiked by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Visualizer & DAC, 1 = Hi-Res Audio Studio

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtmBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // YouTube Music Top Player Header (Cast, More, Bit-Perfect Pill)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigateToTab(ScreenTab.YOUTUBE_HUB) }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Minimize Player",
                    tint = YtmWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Audio Format Pill (YouTube Music style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .clickable { onToggleBitPerfect() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                if ((track?.sampleRateHz ?: 0) >= 96000) {
                    HiResGoldBadge()
                }
                Text(
                    text = if (uiState.isBitPerfectActive) "BIT-PERFECT DIRECT" else "${track?.bitDepth ?: 24}-BIT • ${(track?.sampleRateHz ?: 96000) / 1000}kHz",
                    color = if (uiState.isBitPerfectActive) BitPerfectGreen else YtmWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { /* Cast to audio device */ }) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = "Cast",
                    tint = YtmWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // YouTube Music Iconic Large Square Album Artwork with Rounded Corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f)
                .clip(RoundedCornerShape(12.dp))
                .background(YtmSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!track?.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track?.coverUrl,
                    contentDescription = "Track Artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A1A1A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = YtmRed,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        }

        // Track Title, Artist & Like Action (YouTube Music Layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track?.title ?: "Select Track",
                    color = YtmWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!track?.language.isNullOrBlank() && track?.language != "All") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0x33FF0000))
                                .border(0.5.dp, YtmRed.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = track?.language ?: "",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "${track?.artist ?: "Unknown Artist"} • ${track?.album ?: "Hi-Res Master"}",
                        color = YtmTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Download action button
                track?.let { t ->
                    val dlState = uiState.downloadStates[t.id] ?: (if (t.isDownloaded) TrackDownloadState.DOWNLOADED else TrackDownloadState.NOT_DOWNLOADED)
                    when (dlState) {
                        TrackDownloadState.DOWNLOADING -> {
                            val prog = uiState.downloadProgress[t.id] ?: 0.5f
                            CircularProgressIndicator(
                                progress = { prog },
                                modifier = Modifier.size(24.dp),
                                color = YtmRed,
                                strokeWidth = 2.dp
                            )
                        }
                        TrackDownloadState.DOWNLOADED -> {
                            IconButton(onClick = { onDeleteDownload(t.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DownloadDone,
                                    contentDescription = "Downloaded offline. Tap to remove",
                                    tint = BitPerfectGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            IconButton(onClick = { onDownloadTrack(t) }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download song for offline listening",
                                    tint = YtmWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Thumbs Up Like button
                IconButton(onClick = { isLiked = !isLiked }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Like",
                        tint = if (isLiked) YtmWhite else YtmTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // YouTube Music Red Progress Scrubber Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            val progress = if (uiState.durationMs > 0) {
                (uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Slider(
                value = progress,
                onValueChange = { frac ->
                    onSeekTo((frac * uiState.durationMs).toInt())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("track_progress_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = YtmWhite,
                    activeTrackColor = YtmRed,
                    inactiveTrackColor = Color(0xFF333333)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(uiState.currentPositionMs),
                    color = YtmTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formatMs(uiState.durationMs),
                    color = YtmTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // YouTube Music Master Media Controls (Shuffle, Prev, Big Play, Next, Repeat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigateToTab(ScreenTab.SPATIAL_STAGE) }) {
                Icon(
                    imageVector = Icons.Default.SurroundSound,
                    contentDescription = "Spatial 3D",
                    tint = if (track?.isSpatialMaster == true) NeonCyan else YtmTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onPlayPrevious,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = YtmWhite,
                    modifier = Modifier.size(36.dp)
                )
            }

            // YouTube Music White/Red Solid Circular Play/Pause Button
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(YtmWhite)
                    .clickable { onTogglePlayPause() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = YtmBackground,
                    modifier = Modifier.size(38.dp)
                )
            }

            IconButton(
                onClick = onPlayNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = YtmWhite,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = { onNavigateToTab(ScreenTab.STUDIO_RACK) }) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "EQ",
                    tint = YtmTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // YouTube Music Bottom Sheet Subtabs: "UP NEXT" | "SPECTRUM & VU" | "HI-RES STUDIO"
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = YtmWhite,
            divider = {},
            indicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141414))
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = {
                    Text(
                        "FFT SPECTRUM & VU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubTab == 0) YtmWhite else YtmTextMuted
                    )
                },
                modifier = Modifier.background(if (activeSubTab == 0) Color(0xFF242424) else Color.Transparent)
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = {
                    Text(
                        "HI-RES AUDIO STUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubTab == 1) LosslessGold else YtmTextMuted
                    )
                },
                modifier = Modifier.background(if (activeSubTab == 1) Color(0xFF242424) else Color.Transparent)
            )
        }

        if (activeSubTab == 0) {
            // Real-Time 32-Band FFT Spectrum Visualizer
            SpectrumVisualizer(
                bands = uiState.spectrumBands,
                isPlaying = uiState.isPlaying,
                modifier = Modifier.fillMaxWidth()
            )

            // Dual Left/Right Analog VU Peak Meters
            VuMeterStereo(
                leftDbfs = uiState.leftPeakDbfs,
                rightDbfs = uiState.rightPeakDbfs,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Dedicated Hi-Res Audio Studio Console (Reconstruction filter & signal path)
            HiResAudioConsole(
                track = track,
                dacInfo = uiState.dacInfo,
                upsamplerConfig = uiState.hiResConfig,
                onFilterSelected = onFilterSelected,
                onToggleUpsampler = onToggleUpsampler,
                onTargetSampleRateSelected = onTargetSampleRateSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // DAC Hardware and bit-perfect routing
        DacStatusBadge(
            track = track,
            dacInfo = uiState.dacInfo,
            isBitPerfectActive = uiState.isBitPerfectActive,
            isSpatialActive = track?.isSpatialMaster == true,
            onToggleBitPerfect = onToggleBitPerfect
        )
    }
}

private fun formatMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
