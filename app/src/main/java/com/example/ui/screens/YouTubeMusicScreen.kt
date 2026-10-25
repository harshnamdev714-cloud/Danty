package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.TrackDownloadState
import com.example.ui.UiState
import com.example.ui.components.YouTubeMusicLogo
import com.example.ui.components.YouTubePillChip
import com.example.ui.theme.*
import com.example.youtube.api.YouTubeRetrofitClient

@Composable
fun YouTubeMusicScreen(
    uiState: UiState,
    onSearchChanged: (String) -> Unit,
    onSearchOnlineYouTube: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onToggleOfflineOnly: () -> Unit,
    onDownloadTrack: (AudioTrackItem) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onTrackSelected: (AudioTrackItem) -> Unit,
    onOpenCustomUrlDialog: (Boolean) -> Unit,
    onImportLocalFile: (String, String) -> Unit,
    onSwitchInstance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val displayName = it.lastPathSegment ?: "Local Audio Track"
            onImportLocalFile(it.toString(), displayName)
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Home / Curated, 1 = YouTube Music Search
    var showInstanceDialog by remember { mutableStateOf(false) }

    val moodChips = listOf("All", "Hi-Res Master", "3D Spatial", "Energize", "Relax", "Focus", "Workout")
    val languageChips = listOf(
        "All Languages",
        "Hindi",
        "English",
        "Spanish",
        "Korean",
        "Japanese",
        "French",
        "Arabic"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtmBackground)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // YouTube Music Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            YouTubeMusicLogo()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = { /* Cast */ }) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = "Cast",
                        tint = YtmWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = { showInstanceDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "API Instance",
                        tint = YtmWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = { filePickerLauncher.launch(arrayOf("audio/*")) }) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open Local Lossless",
                        tint = YtmWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Profile Avatar (YouTube style)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // YouTube Music Mood / Activity Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(moodChips) { chip ->
                val isSelected = uiState.activeCategoryFilter == chip || (chip == "All" && uiState.activeCategoryFilter == "All")
                YouTubePillChip(
                    text = chip,
                    isSelected = isSelected,
                    onClick = {
                        val mapped = when (chip) {
                            "Hi-Res Master" -> "24/32-bit Lossless"
                            "3D Spatial" -> "3D Spatial Audio"
                            else -> chip
                        }
                        onCategorySelected(mapped)
                    }
                )
            }
        }

        // Multi-Language Filter Row & Offline Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(languageChips) { lang ->
                    val isSelected = (lang == "All Languages" && (uiState.selectedLanguage == "All" || uiState.selectedLanguage == "All Languages")) ||
                            uiState.selectedLanguage.equals(lang, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) YtmRed else Color(0xFF1E1E1E))
                            .border(1.dp, if (isSelected) YtmRed else Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                            .clickable {
                                val selected = if (lang == "All Languages") "All" else lang
                                onLanguageSelected(selected)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = lang,
                            color = if (isSelected) Color.White else YtmTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Offline Mode Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (uiState.isOfflineOnlyMode) BitPerfectGreen.copy(alpha = 0.2f) else Color(0xFF1E1E1E))
                    .border(
                        1.dp,
                        if (uiState.isOfflineOnlyMode) BitPerfectGreen else Color(0x33FFFFFF),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onToggleOfflineOnly() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isOfflineOnlyMode) Icons.Default.DownloadDone else Icons.Default.CloudQueue,
                        contentDescription = "Offline Mode",
                        tint = if (uiState.isOfflineOnlyMode) BitPerfectGreen else YtmTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (uiState.isOfflineOnlyMode) "Offline" else "Online",
                        color = if (uiState.isOfflineOnlyMode) BitPerfectGreen else YtmTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Bar (YouTube Music Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_field"),
                placeholder = {
                    Text(
                        "Search songs, albums, artists...",
                        color = YtmTextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = YtmTextSecondary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = YtmTextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YtmRed,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedContainerColor = YtmSurfaceVariant,
                    unfocusedContainerColor = YtmSurfaceVariant,
                    focusedTextColor = YtmWhite,
                    unfocusedTextColor = YtmWhite
                )
            )

            // Search Online Button
            Button(
                onClick = {
                    selectedTab = 1
                    onSearchOnlineYouTube(uiState.searchQuery.ifBlank { "Audiophile 24bit" })
                },
                modifier = Modifier
                    .height(44.dp)
                    .testTag("search_online_button"),
                colors = ButtonDefaults.buttonColors(containerColor = YtmRed),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Text("Search", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { onOpenCustomUrlDialog(true) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(YtmSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.AddLink,
                    contentDescription = "Import URL",
                    tint = YtmWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Subtabs: "Home / Quick Picks" vs "YouTube Music Online"
        TabRow(
            selectedTabIndex = selectedTab,
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
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "QUICK PICKS (${uiState.filteredTracks.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) YtmWhite else YtmTextMuted
                    )
                },
                modifier = Modifier.background(if (selectedTab == 0) Color(0xFF242424) else Color.Transparent)
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    if (uiState.onlineTracks.isEmpty()) {
                        onSearchOnlineYouTube("Hans Zimmer Master")
                    }
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "ONLINE STREAM API",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) YtmRed else YtmTextMuted
                        )
                        if (uiState.onlineTracks.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(YtmRed)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    "${uiState.onlineTracks.size}",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.background(if (selectedTab == 1) Color(0xFF242424) else Color.Transparent)
            )
        }

        if (selectedTab == 0) {
            // Horizontal Quick Picks Carousel (YouTube Music style)
            Text(
                text = "LISTEN AGAIN & FEATURED",
                color = YtmWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.filteredTracks.take(6)) { track ->
                    YouTubeQuickPickCard(
                        track = track,
                        isPlaying = uiState.currentTrack?.id == track.id && uiState.isPlaying,
                        onClick = { onTrackSelected(track) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SONGS & HI-RES MASTERS",
                color = YtmWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Vertical Track List (YouTube Music layout)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(uiState.filteredTracks, key = { it.id }) { track ->
                    val state = uiState.downloadStates[track.id] ?: (if (track.isDownloaded) TrackDownloadState.DOWNLOADED else TrackDownloadState.NOT_DOWNLOADED)
                    val prog = uiState.downloadProgress[track.id]
                    YouTubeTrackRow(
                        track = track,
                        isCurrent = uiState.currentTrack?.id == track.id,
                        isPlaying = uiState.currentTrack?.id == track.id && uiState.isPlaying,
                        downloadState = state,
                        downloadProgress = prog,
                        onDownload = { onDownloadTrack(track) },
                        onDelete = { onDeleteDownload(track.id) },
                        onClick = { onTrackSelected(track) }
                    )
                }
            }
        } else {
            // Online YouTube Music Search Results View
            if (uiState.isSearchingOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = YtmRed, modifier = Modifier.size(36.dp))
                        Text(
                            text = "Querying YouTube Music REST API...",
                            color = YtmTextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else if (uiState.onlineTracks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = YtmRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Search YouTube Music Streams",
                        color = YtmWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fetch audio codecs (Opus/AAC 320kbps) with 24-bit spatial DSP",
                        color = YtmTextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.onlineTracks, key = { it.id }) { track ->
                        val state = uiState.downloadStates[track.id] ?: (if (track.isDownloaded) TrackDownloadState.DOWNLOADED else TrackDownloadState.NOT_DOWNLOADED)
                        val prog = uiState.downloadProgress[track.id]
                        YouTubeTrackRow(
                            track = track,
                            isCurrent = uiState.currentTrack?.id == track.id,
                            isPlaying = uiState.currentTrack?.id == track.id && uiState.isPlaying,
                            downloadState = state,
                            downloadProgress = prog,
                            onDownload = { onDownloadTrack(track) },
                            onDelete = { onDeleteDownload(track.id) },
                            onClick = { onTrackSelected(track) }
                        )
                    }
                }
            }
        }
    }

    // Instance Selector Dialog
    if (showInstanceDialog) {
        AlertDialog(
            onDismissRequest = { showInstanceDialog = false },
            title = {
                Text(
                    "YouTube Music API Instance",
                    color = YtmWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    YouTubeRetrofitClient.INSTANCES.forEach { inst ->
                        val isSelected = uiState.currentApiInstance == inst
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF333333) else YtmSurfaceVariant)
                                .clickable {
                                    onSwitchInstance(inst)
                                    showInstanceDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = inst.removePrefix("https://").removeSuffix("/"),
                                color = if (isSelected) YtmRed else YtmWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = YtmRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstanceDialog = false }) {
                    Text("Close", color = YtmRed)
                }
            },
            containerColor = YtmSurfaceElevated
        )
    }
}

@Composable
private fun YouTubeQuickPickCard(
    track: AudioTrackItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(YtmSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (track.coverUrl.isNotBlank()) {
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
                    modifier = Modifier.size(36.dp)
                )
            }

            // Red Play Button Badge (YouTube style)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) YtmWhite else Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isPlaying) YtmBackground else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = track.title,
            color = YtmWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            color = YtmTextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun YouTubeTrackRow(
    track: AudioTrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    downloadState: TrackDownloadState?,
    downloadProgress: Float?,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCurrent) Color(0xFF1C1C1C) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(YtmSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (track.coverUrl.isNotBlank()) {
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
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isCurrent && isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = YtmRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrent) YtmRed else YtmWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (track.language.isNotBlank() && track.language != "All") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x33FF0000))
                            .border(0.5.dp, YtmRed.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = track.language,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${track.artist} • ${track.bitDepth}-BIT ${(track.sampleRateHz / 1000)}kHz",
                    color = YtmTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Download Action Button
        when (downloadState) {
            TrackDownloadState.DOWNLOADING -> {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { downloadProgress ?: 0.5f },
                        modifier = Modifier.size(18.dp),
                        color = YtmRed,
                        strokeWidth = 2.dp
                    )
                }
            }
            TrackDownloadState.DOWNLOADED -> {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Downloaded offline. Tap to remove",
                        tint = BitPerfectGreen,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            else -> {
                IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download for offline listening",
                        tint = YtmTextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // 3-Dots Menu
        IconButton(onClick = { /* Menu */ }, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = YtmTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
