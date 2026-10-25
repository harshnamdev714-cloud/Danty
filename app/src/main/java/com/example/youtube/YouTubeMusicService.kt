package com.example.youtube

import com.example.model.AudioFormatType
import com.example.model.AudioSourceType
import com.example.model.AudioTrackItem
import java.util.UUID

class YouTubeMusicService {

    // Curated multi-language international audiophile library (Hindi, English, Spanish, Korean, Japanese, French, Arabic)
    private val masterTracks = mutableListOf(
        // --- HINDI (हिंदी) ---
        AudioTrackItem(
            id = "track_hindi_tum_hi_ho",
            title = "Tum Hi Ho (Acoustic Flute & Piano Master)",
            artist = "Arijit Singh Style (Aura Acoustic)",
            album = "Bollywood Hi-Res Unplugged",
            durationSeconds = 265,
            streamUri = "synth://hindi-tum-hi-ho-24bit",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.LOSSLESS_24BIT,
            format = AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = 4608,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 15,
            language = "Hindi",
            description = "Emotional Indian bamboo bansuri flute and grand piano recorded in 24-bit 96kHz lossless stereo."
        ),
        AudioTrackItem(
            id = "track_hindi_kesariya",
            title = "Kesariya (Binaural 3D Spatial Raga Mix)",
            artist = "Pritam Style Acoustic Lab",
            album = "Indian Spatial Sessions Vol. 1",
            durationSeconds = 270,
            streamUri = "synth://hindi-kesariya-32bit",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.SPATIAL_3D,
            format = AudioFormatType.WAV_PCM,
            bitDepth = 32,
            sampleRateHz = 192000,
            bitRateKbps = 9216,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 16,
            language = "Hindi",
            description = "360-degree spatial soundstage with acoustic guitars and lush North Indian harmonium."
        ),
        AudioTrackItem(
            id = "track_hindi_raga_bhairavi",
            title = "Raga Bhairavi (Sitar & Tabla Live)",
            artist = "Pt. Ravi Shankar & Ustad Zakir Tribute",
            album = "Vedic Classical 32-Bit Masters",
            durationSeconds = 340,
            streamUri = "synth://hindi-sitar-tabla-32bit",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.HI_RES_DXD_DSD,
            format = AudioFormatType.DXD,
            bitDepth = 32,
            sampleRateHz = 384000,
            bitRateKbps = 18432,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 18,
            language = "Hindi",
            description = "Pristine DXD 384kHz sitar sympathetic resonating strings and high-speed baya-daya tabla strokes."
        ),
        AudioTrackItem(
            id = "track_hindi_chaiyya",
            title = "Chaiyya Chaiyya (Sufi Electro Spatial Master)",
            artist = "A.R. Rahman Tribute Lab",
            album = "Bollywood Spatial Remasters",
            durationSeconds = 315,
            streamUri = "synth://hindi-chaiyya-24bit",
            coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.YOUTUBE_MUSIC,
            format = AudioFormatType.OPUS_STREAM,
            bitDepth = 24,
            sampleRateHz = 48000,
            bitRateKbps = 320,
            isSpatialMaster = true,
            isBitPerfectEligible = false,
            dynamicRangeDr = 14,
            language = "Hindi",
            youtubeVideoId = "dQw4w9WgXcQ",
            description = "High-energy Sufi percussion and Punjabi dhol beats remastered with 3D binaural width."
        ),

        // --- ENGLISH ---
        AudioTrackItem(
            id = "track_beethoven_9",
            title = "Symphony No. 9 'Choral' Finale",
            artist = "Ludwig van Beethoven (Vienna Philharmonic)",
            album = "Hi-Res Master Symphonic Series",
            durationSeconds = 380,
            streamUri = "https://upload.wikimedia.org/wikipedia/commons/e/e4/Beethoven_Symphony_9_movement_4.ogg",
            coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.LOSSLESS_24BIT,
            format = AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = 4608,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 14,
            language = "English",
            description = "24-bit 96kHz studio master recording with immense dynamic range and hall acoustic reverberation."
        ),
        AudioTrackItem(
            id = "track_english_blinding_lights",
            title = "Midnight Horizon (80s Synthwave Spatial)",
            artist = "The Weeknd & Kavinsky Style",
            album = "YouTube Music Hi-Fi Sessions",
            durationSeconds = 245,
            streamUri = "synth://synthwave-32bit",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.YOUTUBE_MUSIC,
            format = AudioFormatType.OPUS_STREAM,
            bitDepth = 24,
            sampleRateHz = 48000,
            bitRateKbps = 320,
            isSpatialMaster = true,
            isBitPerfectEligible = false,
            dynamicRangeDr = 13,
            language = "English",
            youtubeVideoId = "4NRXx6U8ABQ",
            description = "Punchy analog synth bass and sparkling spatial gated reverbs."
        ),
        AudioTrackItem(
            id = "track_tchaikovsky_1812",
            title = "1812 Overture, Op. 49 (Cannon Finale)",
            artist = "Pyotr Ilyich Tchaikovsky (Moscow Symphony)",
            album = "Ultra Hi-Res Masterworks 192kHz",
            durationSeconds = 310,
            streamUri = "https://upload.wikimedia.org/wikipedia/commons/3/30/02_-_Vivaldi_Summer_mvt_3_Presto_-_John_Harrison_violin.ogg",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.LOSSLESS_24BIT,
            format = AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 192000,
            bitRateKbps = 9216,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 16,
            language = "English",
            description = "Ultra Hi-Res 24-bit 192kHz recording with explosive transient response and DR16 master dynamic range."
        ),

        // --- SPANISH / LATIN (ESPAÑOL) ---
        AudioTrackItem(
            id = "track_spanish_despacito_strings",
            title = "Despacito (Flamenco Cuatro & Acoustic Master)",
            artist = "Luis Fonsi Tribute Acoustic Group",
            album = "Latin Hi-Res Masters 24/96",
            durationSeconds = 250,
            streamUri = "synth://spanish-latin-24bit",
            coverUrl = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.LOSSLESS_24BIT,
            format = AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = 4608,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 15,
            language = "Spanish",
            description = "Puerto Rican cuatro guitar, warm nylon acoustic chords, and crisp Latin conga percussion."
        ),
        AudioTrackItem(
            id = "track_flamenco_dxd",
            title = "Flamenco Guitar & Andalusian Strings",
            artist = "Paco de Lucia Studio Tribute",
            album = "DXD 384kHz Reference Session",
            durationSeconds = 235,
            streamUri = "synth://flamenco-32bit-dxd",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.HI_RES_DXD_DSD,
            format = AudioFormatType.DXD,
            bitDepth = 32,
            sampleRateHz = 384000,
            bitRateKbps = 18432,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 17,
            language = "Spanish",
            description = "Digital eXtreme Definition (DXD) 32-bit float 384kHz capturing ultra-fast fingernail string transients."
        ),

        // --- KOREAN (K-POP / 한국어) ---
        AudioTrackItem(
            id = "track_korean_dynamite",
            title = "Dynamite (Neon Seoul Spatial 3D Mix)",
            artist = "BTS Style K-Pop Producers",
            album = "K-Pop Hi-Fi Remasters",
            durationSeconds = 200,
            streamUri = "synth://korean-kpop-24bit",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.YOUTUBE_MUSIC,
            format = AudioFormatType.OPUS_STREAM,
            bitDepth = 24,
            sampleRateHz = 48000,
            bitRateKbps = 320,
            isSpatialMaster = true,
            isBitPerfectEligible = false,
            dynamicRangeDr = 13,
            language = "Korean",
            youtubeVideoId = "gdZLi9oWNZg",
            description = "Ultra clean funk bassline, brass horns, and wide spatial vocal harmonization."
        ),

        // --- JAPANESE (J-POP / ANIME / 日本語) ---
        AudioTrackItem(
            id = "track_japanese_gurenge",
            title = "Gurenge (Symphonic Orchestra & Taiko)",
            artist = "LiSA Style Anime Orchestra",
            album = "Anime Masters 24-Bit 96kHz",
            durationSeconds = 240,
            streamUri = "synth://japanese-anime-24bit",
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.LOSSLESS_24BIT,
            format = AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = 4608,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 15,
            language = "Japanese",
            description = "Violin countermelodies and thundering Japanese taiko drums in uncompressed 24-bit FLAC."
        ),

        // --- FRENCH (FRANÇAIS) ---
        AudioTrackItem(
            id = "track_french_la_vie",
            title = "La Vie En Rose (Accordion & Paris Jazz)",
            artist = "Édith Piaf Tribute Quartet",
            album = "Parisian Acoustic Sessions 32-Bit",
            durationSeconds = 220,
            streamUri = "synth://french-la-vie-32bit",
            coverUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.LOSSLESS_32BIT,
            format = AudioFormatType.WAV_PCM,
            bitDepth = 32,
            sampleRateHz = 192000,
            bitRateKbps = 9216,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 16,
            language = "French",
            description = "Warm French musette accordion, upright acoustic double bass, and vintage ribbon microphone warmth."
        ),

        // --- ARABIC (عربي) ---
        AudioTrackItem(
            id = "track_arabic_oud",
            title = "Desert Night Oud & Golden Darbuka",
            artist = "Middle East Acoustic Masters",
            album = "Oriental Binaural Journeys 24/96",
            durationSeconds = 290,
            streamUri = "synth://arabic-oud-24bit",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            sourceType = AudioSourceType.SPATIAL_3D,
            format = AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = 4608,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 17,
            language = "Arabic",
            description = "Rich 11-string fretless Arabian oud paired with crisp ceramic darbuka hand percussion."
        )
    )

    fun getAllTracks(): List<AudioTrackItem> = masterTracks.toList()

    fun getTracksByLanguage(language: String): List<AudioTrackItem> {
        if (language.equals("All", ignoreCase = true) || language.equals("All Languages", ignoreCase = true)) {
            return masterTracks.toList()
        }
        return masterTracks.filter { it.language.equals(language, ignoreCase = true) }
    }

    fun searchTracks(query: String): List<AudioTrackItem> {
        if (query.isBlank()) return masterTracks.toList()
        val q = query.trim().lowercase()
        return masterTracks.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q) ||
            it.language.lowercase().contains(q) ||
            it.format.displayName.lowercase().contains(q) ||
            it.sourceType.name.lowercase().contains(q)
        }
    }

    fun parseAndAddStreamUrl(
        url: String,
        customTitle: String = "",
        customArtist: String = "",
        customLanguage: String = "International"
    ): AudioTrackItem {
        val trimmed = url.trim()
        val isYt = trimmed.contains("youtube.com") || trimmed.contains("youtu.be")
        val isSpatial = trimmed.contains("spatial", ignoreCase = true) || trimmed.contains("binaural", ignoreCase = true)

        val title = if (customTitle.isNotBlank()) customTitle else if (isYt) "YouTube Music Stream" else "Hi-Fi Audio Stream"
        val artist = if (customArtist.isNotBlank()) customArtist else if (isYt) "YouTube Audio" else "Online Media Stream"

        val item = AudioTrackItem(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            title = title,
            artist = artist,
            album = if (isYt) "YouTube Music Live" else "Custom High-Res Import",
            durationSeconds = 300,
            streamUri = trimmed,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            sourceType = if (isYt) AudioSourceType.YOUTUBE_MUSIC else AudioSourceType.WEB_HI_RES_STREAM,
            format = if (isYt) AudioFormatType.OPUS_STREAM else AudioFormatType.FLAC,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = if (isYt) 320 else 2304,
            isSpatialMaster = isSpatial,
            isBitPerfectEligible = !isYt,
            dynamicRangeDr = 14,
            language = customLanguage,
            description = "Custom streaming input via YouTube Music / Web Stream integration engine."
        )

        masterTracks.add(0, item)
        return item
    }

    fun addLocalTrack(
        uri: String,
        title: String,
        artist: String,
        formatName: String = "FLAC"
    ): AudioTrackItem {
        val format = when (formatName.uppercase()) {
            "WAV" -> AudioFormatType.WAV_PCM
            "ALAC" -> AudioFormatType.ALAC
            "DSD", "DFF" -> AudioFormatType.DSD
            "DXD" -> AudioFormatType.DXD
            else -> AudioFormatType.FLAC
        }

        val item = AudioTrackItem(
            id = "local_" + UUID.randomUUID().toString().take(8),
            title = title,
            artist = artist.ifBlank { "Local Artist" },
            album = "Local High-Res Storage",
            durationSeconds = 240,
            streamUri = uri,
            coverUrl = "",
            sourceType = AudioSourceType.LOCAL_STORAGE,
            format = format,
            bitDepth = 24,
            sampleRateHz = 96000,
            bitRateKbps = 4608,
            isSpatialMaster = true,
            isBitPerfectEligible = true,
            dynamicRangeDr = 15,
            language = "Local",
            description = "Imported local master file playing with direct DAC passthrough."
        )
        masterTracks.add(0, item)
        return item
    }
}
