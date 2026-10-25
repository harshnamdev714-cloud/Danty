package com.example.model

enum class AudioSourceType {
    LOSSLESS_24BIT,
    LOSSLESS_32BIT,
    SPATIAL_3D,
    YOUTUBE_MUSIC,
    WEB_HI_RES_STREAM,
    LOCAL_STORAGE,
    HI_RES_DXD_DSD
}

enum class AudioFormatType(val displayName: String, val extension: String, val bitDepthDefault: Int) {
    FLAC("FLAC Master", "flac", 24),
    WAV_PCM("WAV PCM", "wav", 32),
    ALAC("Apple Lossless", "alac", 24),
    DSD("DSD / DFF", "dsd", 32),
    DXD("DXD 384kHz 32-bit", "dxd", 32),
    OPUS_STREAM("Opus Stream", "opus", 24),
    AAC_HD("AAC HD Stream", "aac", 16)
}

enum class HiResFilterType(val displayName: String, val description: String) {
    APODIZING_FAST_ROLLOFF(
        "Apodizing Fast Roll-Off",
        "Removes pre-ringing and digital filter artifacts for silky smooth highs."
    ),
    MINIMUM_PHASE_SLOW(
        "Minimum Phase Slow",
        "Natural transient attack with zero pre-echo and analog tube-like timbre."
    ),
    LINEAR_PHASE_FAST(
        "Linear Phase Fast",
        "Absolute phase linearity across 20Hz-96kHz for reference studio monitors."
    ),
    NOS_DIRECT_BYPASS(
        "Non-Oversampling (NOS)",
        "Zero digital reconstruction filter; feeds direct native samples to DAC."
    )
}

data class HiResUpsamplerConfig(
    val isEnabled: Boolean = true,
    val targetSampleRateHz: Int = 192000, // 96kHz, 192kHz, 384kHz
    val targetBitDepth: Int = 32, // 24 or 32-bit float
    val filterType: HiResFilterType = HiResFilterType.APODIZING_FAST_ROLLOFF,
    val ditherNoiseShaping: Boolean = true
)

data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val streamUri: String,
    val coverUrl: String,
    val sourceType: AudioSourceType,
    val format: AudioFormatType,
    val bitDepth: Int, // 16, 24, or 32
    val sampleRateHz: Int, // e.g. 44100, 48000, 96000, 192000, 384000
    val bitRateKbps: Int, // e.g. 1411, 4608, 9216, 11289
    val isSpatialMaster: Boolean = false,
    val isBitPerfectEligible: Boolean = true,
    val dynamicRangeDr: Int = 14, // DR rating (e.g. DR14, DR16)
    val language: String = "English", // Hindi, English, Spanish, Korean, Japanese, French, Arabic
    val isDownloaded: Boolean = false,
    val localDownloadPath: String? = null,
    val downloadSizeMb: Float = 0f,
    val youtubeVideoId: String? = null,
    val description: String = ""
)

enum class TrackDownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED
}

data class SpatialCoordinates(
    val azimuthDegrees: Float = 0f, // -180 to 180 degrees around listener
    val elevationDegrees: Float = 0f, // -90 to 90 degrees
    val distanceMeters: Float = 1.5f, // 0.5 to 5.0 meters
    val roomAcoustic: RoomAcoustic = RoomAcoustic.STUDIO_CONTROL_ROOM,
    val crossfeedEnabled: Boolean = true,
    val crossfeedLevel: Float = 0.45f, // 0.0 to 1.0
    val soundstageWidth: Float = 1.35f, // 1.0 = normal, 2.0 = ultra wide
    val isHeadTrackingActive: Boolean = true
)

enum class RoomAcoustic(val displayName: String, val reverbTimeSeconds: Float, val roomSizeMeters: Float) {
    STUDIO_CONTROL_ROOM("Studio Master Room", 0.35f, 5.0f),
    ACOUSTIC_CHAMBER("Acoustic Chamber", 0.85f, 12.0f),
    SYMPHONIC_HALL("Symphonic Concert Hall", 1.85f, 25.0f),
    CATHEDRAL("Binaural Cathedral", 3.2f, 40.0f),
    OPEN_FIELD("Direct Pure Soundfield", 0.05f, 1.0f)
}

data class DacHardwareInfo(
    val deviceName: String = "Internal Hi-Res DAC",
    val connectionType: String = "Direct Bus / 3.5mm Hi-Fi",
    val maxSampleRateHz: Int = 192000,
    val supportedBitDepths: List<Int> = listOf(16, 24, 32),
    val isSpatializerSupported: Boolean = true,
    val isBitPerfectActive: Boolean = true,
    val bufferLatencyMs: Float = 4.2f,
    val audioChipset: String = "Qualcomm Hexagon DSP / Cirrus Logic 32-bit",
    val isHiResCertified: Boolean = true,
    val snrDb: Int = 124
)

data class EqualizerBand(
    val frequencyHz: Int,
    val gainDb: Float // -12dB to +12dB
)
