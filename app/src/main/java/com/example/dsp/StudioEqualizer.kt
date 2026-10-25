package com.example.dsp

import com.example.model.EqualizerBand

data class EqualizerPreset(
    val name: String,
    val description: String,
    val gains: List<Float> // 10 bands
)

object StudioEqualizer {

    val BAND_FREQUENCIES = listOf(31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

    val PRESETS = listOf(
        EqualizerPreset(
            name = "Flat Studio Monitor",
            description = "Uncolored, bit-accurate reference response for studio mastering.",
            gains = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        ),
        EqualizerPreset(
            name = "Harman Target Curve",
            description = "Acoustic science target for natural headphone bass shelf and vocal presence.",
            gains = listOf(5.5f, 4.8f, 3.2f, 0.5f, 0.0f, 1.2f, 3.8f, 4.5f, 2.0f, 1.0f)
        ),
        EqualizerPreset(
            name = "Vacuum Tube Warmth",
            description = "Rich even-order harmonics, lush mid-range and rolled-off silky treble.",
            gains = listOf(3.8f, 4.2f, 3.0f, 1.8f, 0.5f, -0.8f, -1.2f, -0.5f, 1.0f, 1.5f)
        ),
        EqualizerPreset(
            name = "3D Spatial Expansion",
            description = "Amplifies binaural HRTF cues and upper spatial air bands (4kHz - 16kHz).",
            gains = listOf(2.0f, 1.0f, -1.0f, -1.5f, 0.5f, 2.5f, 4.0f, 5.5f, 6.0f, 5.0f)
        ),
        EqualizerPreset(
            name = "Audiophile Acoustic",
            description = "Crisp string timbre, lifelike piano transients, and natural breath.",
            gains = listOf(1.0f, 1.5f, 0.8f, -0.5f, 0.2f, 1.5f, 2.2f, 3.0f, 4.2f, 3.8f)
        ),
        EqualizerPreset(
            name = "Deep Bass Reflex",
            description = "Sub-bass excitation (<80Hz) for planar magnetic headphones and subwoofers.",
            gains = listOf(8.0f, 7.2f, 5.0f, 2.0f, 0.0f, -1.0f, 0.0f, 1.0f, 2.0f, 2.5f)
        ),
        EqualizerPreset(
            name = "Vocal Clarity",
            description = "Focuses speech and lead vocal harmonics between 500Hz and 4kHz.",
            gains = listOf(-2.0f, -1.0f, 0.5f, 2.0f, 4.0f, 4.5f, 3.2f, 1.5f, 0.5f, 0.0f)
        )
    )

    fun createInitialBands(): List<EqualizerBand> {
        return BAND_FREQUENCIES.map { freq -> EqualizerBand(freq, 0.0f) }
    }
}
