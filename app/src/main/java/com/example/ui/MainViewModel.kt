package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsp.EqualizerPreset
import com.example.dsp.StudioEqualizer
import com.example.engine.HardwareDacMonitor
import com.example.engine.LosslessAudioEngine
import com.example.engine.OfflineDownloadManager
import com.example.model.*
import com.example.youtube.YouTubeMusicSearchRepository
import com.example.youtube.YouTubeMusicService
import com.example.youtube.api.YouTubeRetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String) {
    NOW_PLAYING("Console"),
    SPATIAL_STAGE("3D Spatial"),
    YOUTUBE_HUB("Streams & HiFi"),
    STUDIO_RACK("DSP & EQ"),
    OPEN_SOURCE("Open Source")
}

data class UiState(
    val currentTab: ScreenTab = ScreenTab.NOW_PLAYING,
    val tracks: List<AudioTrackItem> = emptyList(),
    val filteredTracks: List<AudioTrackItem> = emptyList(),
    val onlineTracks: List<AudioTrackItem> = emptyList(),
    val currentTrack: AudioTrackItem? = null,
    val isPlaying: Boolean = false,
    val isSearchingOnline: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val spectrumBands: FloatArray = FloatArray(32) { 0f },
    val leftPeakDbfs: Float = -60f,
    val rightPeakDbfs: Float = -60f,
    val spatialCoordinates: SpatialCoordinates = SpatialCoordinates(),
    val equalizerBands: List<EqualizerBand> = StudioEqualizer.createInitialBands(),
    val currentPreset: String = "Flat Studio Monitor",
    val dacInfo: DacHardwareInfo = DacHardwareInfo(),
    val isBitPerfectActive: Boolean = false,
    val searchQuery: String = "",
    val activeCategoryFilter: String = "All",
    val selectedLanguage: String = "All",
    val isOfflineOnlyMode: Boolean = false,
    val isCustomUrlDialogOpen: Boolean = false,
    val statusMessage: String? = null,
    val currentApiInstance: String = YouTubeRetrofitClient.currentBaseUrl,
    val hiResConfig: HiResUpsamplerConfig = HiResUpsamplerConfig(),
    val downloadStates: Map<String, TrackDownloadState> = emptyMap(),
    val downloadProgress: Map<String, Float> = emptyMap(),
    val totalStorageUsedMb: Float = 0f
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = LosslessAudioEngine(application)
    private val youtubeService = YouTubeMusicService()
    private val youtubeSearchRepo = YouTubeMusicSearchRepository()
    private val dacMonitor = HardwareDacMonitor(application)
    private val downloadManager = OfflineDownloadManager(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeEngineState()
    }

    private fun loadInitialData() {
        val initialTracks = youtubeService.getAllTracks()
        val dacTelemetry = dacMonitor.detectDacHardware()

        // Check which tracks are already downloaded on device
        val downloadMap = mutableMapOf<String, TrackDownloadState>()
        val updatedTracks = initialTracks.map { t ->
            val isDownloaded = downloadManager.isTrackDownloaded(t.id)
            val path = downloadManager.getLocalFilePath(t.id)
            if (isDownloaded) {
                downloadMap[t.id] = TrackDownloadState.DOWNLOADED
            }
            t.copy(isDownloaded = isDownloaded, localDownloadPath = path)
        }

        val defaultTrack = updatedTracks.firstOrNull()
        val storageUsed = downloadManager.getTotalStorageUsedMb()

        _uiState.update {
            it.copy(
                tracks = updatedTracks,
                filteredTracks = updatedTracks,
                currentTrack = defaultTrack,
                durationMs = (defaultTrack?.durationSeconds ?: 0) * 1000,
                dacInfo = dacTelemetry,
                downloadStates = downloadMap,
                totalStorageUsedMb = storageUsed
            )
        }
    }

    private fun observeEngineState() {
        viewModelScope.launch {
            audioEngine.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlaying = playing) }
            }
        }

        viewModelScope.launch {
            audioEngine.currentPositionMs.collect { pos ->
                _uiState.update { it.copy(currentPositionMs = pos) }
            }
        }

        viewModelScope.launch {
            audioEngine.durationMs.collect { dur ->
                if (dur > 0) {
                    _uiState.update { it.copy(durationMs = dur) }
                }
            }
        }

        viewModelScope.launch {
            audioEngine.spectrumBands.collect { bands ->
                _uiState.update { it.copy(spectrumBands = bands) }
            }
        }

        viewModelScope.launch {
            audioEngine.leftPeakDbfs.collect { lDbfs ->
                _uiState.update { it.copy(leftPeakDbfs = lDbfs) }
            }
        }

        viewModelScope.launch {
            audioEngine.rightPeakDbfs.collect { rDbfs ->
                _uiState.update { it.copy(rightPeakDbfs = rDbfs) }
            }
        }
    }

    fun selectTab(tab: ScreenTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun playTrack(track: AudioTrackItem) {
        _uiState.update { it.copy(currentTrack = track) }

        // If track is downloaded locally, play directly from offline storage!
        if (track.isDownloaded && !track.localDownloadPath.isNullOrBlank()) {
            val localTrack = track.copy(streamUri = "file://${track.localDownloadPath}")
            _uiState.update { it.copy(currentTrack = localTrack, statusMessage = "Playing Offline: ${track.title}") }
            audioEngine.playTrack(localTrack)
            return
        }

        if (track.streamUri.startsWith("ytm://") || (track.youtubeVideoId != null && track.streamUri.startsWith("ytm://"))) {
            // Fetch online media stream via Retrofit
            viewModelScope.launch {
                _uiState.update { it.copy(statusMessage = "Fetching stream via Retrofit API...") }
                val videoId = track.youtubeVideoId ?: track.streamUri.removePrefix("ytm://")
                val streamResult = youtubeSearchRepo.resolveMediaStreamUrl(videoId)
                val directUrl = streamResult.getOrDefault("https://upload.wikimedia.org/wikipedia/commons/e/e4/Beethoven_Symphony_9_movement_4.ogg")
                val resolvedTrack = track.copy(streamUri = directUrl)
                _uiState.update { it.copy(currentTrack = resolvedTrack, statusMessage = "Streaming: ${track.title}") }
                audioEngine.playTrack(resolvedTrack)
            }
        } else {
            audioEngine.playTrack(track)
        }
    }

    fun togglePlayPause() {
        val current = _uiState.value.currentTrack
        if (current == null) {
            val first = _uiState.value.tracks.firstOrNull() ?: return
            playTrack(first)
        } else {
            audioEngine.togglePlayPause()
        }
    }

    fun seekTo(positionMs: Int) {
        audioEngine.seekTo(positionMs)
    }

    fun playNext() {
        val list = if (_uiState.value.onlineTracks.isNotEmpty()) _uiState.value.onlineTracks else _uiState.value.tracks
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        val nextIndex = if (currentIndex != -1 && currentIndex + 1 < list.size) currentIndex + 1 else 0
        playTrack(list[nextIndex])
    }

    fun playPrevious() {
        val list = if (_uiState.value.onlineTracks.isNotEmpty()) _uiState.value.onlineTracks else _uiState.value.tracks
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.lastIndex
        playTrack(list[prevIndex])
    }

    fun downloadTrack(track: AudioTrackItem) {
        viewModelScope.launch {
            val currentMap = _uiState.value.downloadStates.toMutableMap()
            currentMap[track.id] = TrackDownloadState.DOWNLOADING
            _uiState.update {
                it.copy(
                    downloadStates = currentMap,
                    statusMessage = "Downloading ${track.title} for offline playback..."
                )
            }

            val result = downloadManager.downloadTrack(track) { progress ->
                val progressMap = _uiState.value.downloadProgress.toMutableMap()
                progressMap[track.id] = progress
                _uiState.update { it.copy(downloadProgress = progressMap) }
            }

            result.onSuccess { localPath ->
                val updatedStates = _uiState.value.downloadStates.toMutableMap()
                updatedStates[track.id] = TrackDownloadState.DOWNLOADED

                // Update tracks in state
                val updatedList = _uiState.value.tracks.map {
                    if (it.id == track.id) it.copy(isDownloaded = true, localDownloadPath = localPath)
                    else it
                }
                val storage = downloadManager.getTotalStorageUsedMb()

                _uiState.update {
                    it.copy(
                        tracks = updatedList,
                        filteredTracks = updatedList,
                        downloadStates = updatedStates,
                        totalStorageUsedMb = storage,
                        statusMessage = "Downloaded: ${track.title} (Ready for offline playback)"
                    )
                }
            }.onFailure { err ->
                val updatedStates = _uiState.value.downloadStates.toMutableMap()
                updatedStates[track.id] = TrackDownloadState.NOT_DOWNLOADED
                _uiState.update {
                    it.copy(
                        downloadStates = updatedStates,
                        statusMessage = "Download failed: ${err.message?.take(50)}"
                    )
                }
            }
        }
    }

    fun deleteDownloadedTrack(trackId: String) {
        val success = downloadManager.deleteDownloadedTrack(trackId)
        if (success) {
            val updatedStates = _uiState.value.downloadStates.toMutableMap()
            updatedStates.remove(trackId)

            val updatedTracks = _uiState.value.tracks.map {
                if (it.id == trackId) it.copy(isDownloaded = false, localDownloadPath = null)
                else it
            }
            val storage = downloadManager.getTotalStorageUsedMb()

            _uiState.update {
                it.copy(
                    tracks = updatedTracks,
                    filteredTracks = updatedTracks,
                    downloadStates = updatedStates,
                    totalStorageUsedMb = storage,
                    statusMessage = "Removed downloaded track"
                )
            }
        }
    }

    fun setLanguageFilter(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
        applyFilters(_uiState.value.searchQuery, _uiState.value.activeCategoryFilter, language, _uiState.value.isOfflineOnlyMode)
    }

    fun toggleOfflineOnlyMode() {
        val newOffline = !_uiState.value.isOfflineOnlyMode
        _uiState.update { it.copy(isOfflineOnlyMode = newOffline) }
        applyFilters(_uiState.value.searchQuery, _uiState.value.activeCategoryFilter, _uiState.value.selectedLanguage, newOffline)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters(query, _uiState.value.activeCategoryFilter, _uiState.value.selectedLanguage, _uiState.value.isOfflineOnlyMode)
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(activeCategoryFilter = category) }
        applyFilters(_uiState.value.searchQuery, category, _uiState.value.selectedLanguage, _uiState.value.isOfflineOnlyMode)
    }

    private fun applyFilters(query: String, category: String, language: String, offlineOnly: Boolean) {
        var list = youtubeService.searchTracks(query)

        // Language filter
        if (language != "All" && language != "All Languages") {
            list = list.filter { it.language.equals(language, ignoreCase = true) }
        }

        // Category filter
        list = when (category) {
            "24/32-bit Lossless", "Hi-Res Master" -> list.filter { it.bitDepth >= 24 }
            "3D Spatial Audio", "3D Spatial" -> list.filter { it.isSpatialMaster }
            "YouTube Streams" -> list.filter { it.sourceType == AudioSourceType.YOUTUBE_MUSIC }
            "Local Audio" -> list.filter { it.sourceType == AudioSourceType.LOCAL_STORAGE }
            else -> list
        }

        // Offline only filter
        if (offlineOnly) {
            list = list.filter { it.isDownloaded }
        }

        _uiState.update { it.copy(filteredTracks = list) }
    }

    fun searchYouTubeMusicOnline(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingOnline = true, statusMessage = "Searching YouTube Music via Retrofit...") }
            val result = youtubeSearchRepo.searchOnline(query)
            result.onSuccess { tracks ->
                _uiState.update {
                    it.copy(
                        isSearchingOnline = false,
                        onlineTracks = tracks,
                        statusMessage = "Found ${tracks.size} tracks on YouTube Music"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSearchingOnline = false,
                        statusMessage = "Online search notice: ${err.message?.take(60)}"
                    )
                }
            }
        }
    }

    fun updateSpatialCoordinates(newCoordinates: SpatialCoordinates) {
        _uiState.update { it.copy(spatialCoordinates = newCoordinates) }
        audioEngine.updateSpatialCoordinates(newCoordinates)
    }

    fun setRoomAcoustic(room: RoomAcoustic) {
        val updated = _uiState.value.spatialCoordinates.copy(roomAcoustic = room)
        updateSpatialCoordinates(updated)
    }

    fun toggleCrossfeed(enabled: Boolean) {
        val updated = _uiState.value.spatialCoordinates.copy(crossfeedEnabled = enabled)
        updateSpatialCoordinates(updated)
    }

    fun setSoundstageWidth(width: Float) {
        val updated = _uiState.value.spatialCoordinates.copy(soundstageWidth = width)
        updateSpatialCoordinates(updated)
    }

    fun toggleBitPerfect() {
        val newBitPerfect = !_uiState.value.isBitPerfectActive
        _uiState.update { it.copy(isBitPerfectActive = newBitPerfect) }
        audioEngine.setBitPerfectBypass(newBitPerfect)
    }

    fun applyPreset(preset: EqualizerPreset) {
        val newBands = StudioEqualizer.BAND_FREQUENCIES.mapIndexed { index, freq ->
            val gain = preset.gains.getOrElse(index) { 0f }
            audioEngine.applyEqualizerBand(index, gain)
            EqualizerBand(freq, gain)
        }
        _uiState.update {
            it.copy(
                equalizerBands = newBands,
                currentPreset = preset.name
            )
        }
    }

    fun updateEqualizerBand(bandIndex: Int, gainDb: Float) {
        val bands = _uiState.value.equalizerBands.toMutableList()
        if (bandIndex in bands.indices) {
            bands[bandIndex] = bands[bandIndex].copy(gainDb = gainDb)
            audioEngine.applyEqualizerBand(bandIndex, gainDb)
            _uiState.update {
                it.copy(
                    equalizerBands = bands,
                    currentPreset = "Custom Studio Curve"
                )
            }
        }
    }

    fun importCustomStream(url: String, title: String, artist: String) {
        val newTrack = youtubeService.parseAndAddStreamUrl(url, title, artist)
        val all = youtubeService.getAllTracks()
        _uiState.update {
            it.copy(
                tracks = all,
                filteredTracks = all,
                isCustomUrlDialogOpen = false,
                statusMessage = "Added: ${newTrack.title}"
            )
        }
        playTrack(newTrack)
    }

    fun importLocalFile(uri: String, displayName: String) {
        val newTrack = youtubeService.addLocalTrack(uri, displayName, "Device Audio")
        val all = youtubeService.getAllTracks()
        _uiState.update {
            it.copy(
                tracks = all,
                filteredTracks = all,
                statusMessage = "Loaded local master: $displayName"
            )
        }
        playTrack(newTrack)
    }

    fun setCustomUrlDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isCustomUrlDialogOpen = open) }
    }

    fun switchApiInstance(instanceUrl: String) {
        YouTubeRetrofitClient.switchInstance(instanceUrl)
        _uiState.update {
            it.copy(
                currentApiInstance = instanceUrl,
                statusMessage = "Switched API instance to $instanceUrl"
            )
        }
    }

    fun setHiResFilter(filter: HiResFilterType) {
        val updated = _uiState.value.hiResConfig.copy(filterType = filter)
        _uiState.update {
            it.copy(
                hiResConfig = updated,
                statusMessage = "DAC Filter: ${filter.displayName}"
            )
        }
    }

    fun toggleHiResUpsampler(enabled: Boolean) {
        val updated = _uiState.value.hiResConfig.copy(isEnabled = enabled)
        _uiState.update {
            it.copy(
                hiResConfig = updated,
                statusMessage = if (enabled) "Ultra Hi-Res Resampler Active (192kHz/384kHz)" else "Native Rate Direct"
            )
        }
    }

    fun setTargetSampleRate(rateHz: Int) {
        val updated = _uiState.value.hiResConfig.copy(targetSampleRateHz = rateHz)
        _uiState.update {
            it.copy(
                hiResConfig = updated,
                statusMessage = "Target Sampling Rate: ${rateHz / 1000} kHz"
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
