package com.example.engine

import com.example.model.RoomAcoustic
import com.example.model.SpatialCoordinates
import kotlin.math.*

class SpatialAudioDsp {

    data class BinauralGains(
        val leftGain: Float,
        val rightGain: Float,
        val leftDelayMs: Float,
        val rightDelayMs: Float,
        val leftFilterCutoffHz: Float,
        val rightFilterCutoffHz: Float,
        val roomEarlyReflectionGain: Float
    )

    /**
     * Calculates binaural ITD (Interaural Time Difference) and ILD (Interaural Level Difference)
     * based on spherical head acoustic models.
     */
    fun calculateBinauralParameters(coords: SpatialCoordinates): BinauralGains {
        val azimuthRad = Math.toRadians(coords.azimuthDegrees.toDouble()).toFloat()
        val elevationRad = Math.toRadians(coords.elevationDegrees.toDouble()).toFloat()

        // Distance attenuation using inverse square law
        val distanceFactor = (1.0f / (coords.distanceMeters.coerceAtLeast(0.5f))).coerceIn(0.2f, 1.5f)

        // ITD Woodworth Spherical Head Model (Head radius ~ 0.0875m, speed of sound ~ 343 m/s)
        val maxItdSeconds = 0.00065f // 0.65 ms
        val sinAzimuth = sin(azimuthRad)
        val itdSeconds = maxItdSeconds * (sinAzimuth + azimuthRad) * 0.5f

        val leftDelayMs: Float
        val rightDelayMs: Float
        if (sinAzimuth >= 0) {
            // Source is to the right
            leftDelayMs = abs(itdSeconds) * 1000f
            rightDelayMs = 0f
        } else {
            // Source is to the left
            leftDelayMs = 0f
            rightDelayMs = abs(itdSeconds) * 1000f
        }

        // ILD: High-frequency head shadowing
        // Right ear attenuation when source is to the left, and vice-versa
        val pan = sinAzimuth * coords.soundstageWidth
        val leftBaseGain = ((1.0f - pan) * 0.5f).coerceIn(0.05f, 1.0f) * distanceFactor
        val rightBaseGain = ((1.0f + pan) * 0.5f).coerceIn(0.05f, 1.0f) * distanceFactor

        // Binaural Crossfeed (Chu Moy model): subtle bass leakage between channels to prevent listening fatigue
        val crossfeed = if (coords.crossfeedEnabled) coords.crossfeedLevel * 0.28f else 0f
        val finalLeftGain = (leftBaseGain * (1f - crossfeed) + rightBaseGain * crossfeed).coerceIn(0f, 1.2f)
        val finalRightGain = (rightBaseGain * (1f - crossfeed) + leftBaseGain * crossfeed).coerceIn(0f, 1.2f)

        // High frequency head-shadow cutoff (approximate pinna / head obstruction)
        val leftCutoff = if (sinAzimuth > 0.2f) 20000f - (sinAzimuth * 14000f) else 20000f
        val rightCutoff = if (sinAzimuth < -0.2f) 20000f - (abs(sinAzimuth) * 14000f) else 20000f

        val roomReflection = when (coords.roomAcoustic) {
            RoomAcoustic.STUDIO_CONTROL_ROOM -> 0.12f
            RoomAcoustic.ACOUSTIC_CHAMBER -> 0.28f
            RoomAcoustic.SYMPHONIC_HALL -> 0.55f
            RoomAcoustic.CATHEDRAL -> 0.78f
            RoomAcoustic.OPEN_FIELD -> 0.02f
        }

        return BinauralGains(
            leftGain = finalLeftGain,
            rightGain = finalRightGain,
            leftDelayMs = leftDelayMs,
            rightDelayMs = rightDelayMs,
            leftFilterCutoffHz = leftCutoff,
            rightFilterCutoffHz = rightCutoff,
            roomEarlyReflectionGain = roomReflection
        )
    }

    /**
     * Converts orbital azimuth and distance to 2D UI coordinates (normalized -1..1)
     */
    fun toNormalizedPlane(azimuthDegrees: Float, distanceMeters: Float): Pair<Float, Float> {
        val rad = Math.toRadians((azimuthDegrees - 90).toDouble())
        val distNorm = (distanceMeters / 3.0f).coerceIn(0.2f, 1.0f)
        val x = (cos(rad) * distNorm).toFloat()
        val y = (sin(rad) * distNorm).toFloat()
        return Pair(x, y)
    }

    /**
     * Converts 2D UI drag coordinates back to Azimuth (-180..180) and Distance (0.5..3.0m)
     */
    fun fromNormalizedPlane(x: Float, y: Float): Pair<Float, Float> {
        val dist = sqrt(x * x + y * y).coerceIn(0.15f, 1.0f) * 3.0f
        var deg = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat() + 90f
        if (deg > 180f) deg -= 360f
        if (deg < -180f) deg += 360f
        return Pair(deg, dist)
    }
}
