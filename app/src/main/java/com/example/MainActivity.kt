package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.model.AudioTrackItem
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.CustomUrlDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResonanceHiFiTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Handle back button to return to Console if on secondary tab
    BackHandler(enabled = uiState.currentTab != ScreenTab.NOW_PLAYING) {
        viewModel.selectTab(ScreenTab.NOW_PLAYING)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StudioBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StudioTopBar(
                currentTrack = uiState.currentTrack,
                isBitPerfect = uiState.isBitPerfectActive,
                onHeaderClick = { viewModel.selectTab(ScreenTab.NOW_PLAYING) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioBackground)
            ) {
                // Mini Player Strip when not on Console screen
                AnimatedVisibility(
                    visible = uiState.currentTab != ScreenTab.NOW_PLAYING && uiState.currentTrack != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    MiniPlayerStrip(
                        track = uiState.currentTrack,
                        isPlaying = uiState.isPlaying,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onOpenConsole = { viewModel.selectTab(ScreenTab.NOW_PLAYING) }
                    )
                }

                // Studio Navigation Bar
                StudioBottomNavBar(
                    selectedTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                ScreenTab.NOW_PLAYING -> {
                    NowPlayingScreen(
                        uiState = uiState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onPlayNext = { viewModel.playNext() },
                        onPlayPrevious = { viewModel.playPrevious() },
                        onSeekTo = { viewModel.seekTo(it) },
                        onToggleBitPerfect = { viewModel.toggleBitPerfect() },
                        onFilterSelected = { viewModel.setHiResFilter(it) },
                        onToggleUpsampler = { viewModel.toggleHiResUpsampler(it) },
                        onTargetSampleRateSelected = { viewModel.setTargetSampleRate(it) },
                        onDownloadTrack = { viewModel.downloadTrack(it) },
                        onDeleteDownload = { viewModel.deleteDownloadedTrack(it) },
                        onNavigateToTab = { viewModel.selectTab(it) }
                    )
                }
                ScreenTab.SPATIAL_STAGE -> {
                    SpatialAudioScreen(
                        uiState = uiState,
                        onCoordinatesChanged = { viewModel.updateSpatialCoordinates(it) },
                        onRoomSelected = { viewModel.setRoomAcoustic(it) },
                        onToggleCrossfeed = { viewModel.toggleCrossfeed(it) },
                        onSetSoundstageWidth = { viewModel.setSoundstageWidth(it) }
                    )
                }
                ScreenTab.YOUTUBE_HUB -> {
                    YouTubeMusicScreen(
                        uiState = uiState,
                        onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                        onSearchOnlineYouTube = { viewModel.searchYouTubeMusicOnline(it) },
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        onLanguageSelected = { viewModel.setLanguageFilter(it) },
                        onToggleOfflineOnly = { viewModel.toggleOfflineOnlyMode() },
                        onDownloadTrack = { viewModel.downloadTrack(it) },
                        onDeleteDownload = { viewModel.deleteDownloadedTrack(it) },
                        onTrackSelected = { viewModel.playTrack(it) },
                        onOpenCustomUrlDialog = { viewModel.setCustomUrlDialogOpen(it) },
                        onImportLocalFile = { uri, name -> viewModel.importLocalFile(uri, name) },
                        onSwitchInstance = { viewModel.switchApiInstance(it) }
                    )
                }
                ScreenTab.STUDIO_RACK -> {
                    StudioRackScreen(
                        uiState = uiState,
                        onBandChanged = { idx, gain -> viewModel.updateEqualizerBand(idx, gain) },
                        onPresetSelected = { viewModel.applyPreset(it) }
                    )
                }
                ScreenTab.OPEN_SOURCE -> {
                    OpenSourceDocsScreen()
                }
            }
        }
    }

    // Custom URL Import Dialog
    CustomUrlDialog(
        isOpen = uiState.isCustomUrlDialogOpen,
        onDismiss = { viewModel.setCustomUrlDialogOpen(false) },
        onConfirm = { url, title, artist ->
            viewModel.importCustomStream(url, title, artist)
        }
    )
}

@Composable
fun StudioTopBar(
    currentTrack: AudioTrackItem?,
    isBitPerfect: Boolean,
    onHeaderClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF0050811),
                        Color(0xB3050811),
                        Color.Transparent
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onHeaderClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CrystalIce, CrystalAurora, LosslessGold)
                            )
                        )
                        .border(1.dp, Color(0x99FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = YtmBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "BEATS HI-FI",
                        color = StudioTextPrimary,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Clear Crystal • 24/32-Bit Lossless Spatial",
                        color = CrystalIce,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Crystal Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isBitPerfect) BitPerfectGreen.copy(alpha = 0.18f) else Color(0x1F1E293B))
                    .border(
                        1.dp,
                        if (isBitPerfect) BitPerfectGreen else Color(0x40FFFFFF),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isBitPerfect) "BIT-PERFECT" else "${currentTrack?.bitDepth ?: 24}-BIT CRYSTAL",
                    color = if (isBitPerfect) BitPerfectGreen else CrystalIce,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MiniPlayerStrip(
    track: AudioTrackItem?,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onOpenConsole: () -> Unit
) {
    if (track == null) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(YtmSurfaceElevated)
            .clickable { onOpenConsole() }
            .testTag("mini_player_strip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Square Thumbnail
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(YtmSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!track.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = YtmRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = YtmWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.bitDepth}-BIT ${(track.sampleRateHz / 1000)}kHz",
                    color = YtmTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = YtmWhite,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // YouTube Music Red Progress Line along the bottom of the mini-player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFF333333))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .fillMaxHeight()
                    .background(YtmRed)
            )
        }
    }
}

@Composable
fun StudioBottomNavBar(
    selectedTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF101010),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(width = 0.5.dp, color = Color(0xFF222222))
            .testTag("studio_bottom_nav")
    ) {
        val items = listOf(
            Triple(ScreenTab.NOW_PLAYING, Icons.Default.Home, "Home"),
            Triple(ScreenTab.SPATIAL_STAGE, Icons.Default.SurroundSound, "Spatial 3D"),
            Triple(ScreenTab.YOUTUBE_HUB, Icons.Default.Explore, "Explore"),
            Triple(ScreenTab.STUDIO_RACK, Icons.Default.Equalizer, "DSP EQ"),
            Triple(ScreenTab.OPEN_SOURCE, Icons.Default.LibraryMusic, "Library")
        )

        items.forEach { (tab, icon, label) ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) YtmWhite else YtmTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) YtmWhite else YtmTextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF242424)
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
