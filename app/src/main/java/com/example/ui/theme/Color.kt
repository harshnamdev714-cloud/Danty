package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// YouTube Music Authentic Color Palette
val YtmBackground = Color(0xFF030303)
val YtmSurface = Color(0xFF101010)
val YtmSurfaceElevated = Color(0xFF1E1E1E)
val YtmSurfaceVariant = Color(0xFF212121)
val YtmCardBorder = Color(0xFF2D2D2D)

// YouTube Iconic Red & Media Accents
val YtmRed = Color(0xFFFF0000)
val YtmRedDark = Color(0xFFCC0000)
val YtmWhite = Color(0xFFFFFFFF)
val YtmTextPrimary = Color(0xFFFFFFFF)
val YtmTextSecondary = Color(0xFFAAAAAA)
val YtmTextMuted = Color(0xFF717171)

// Mapped legacy tokens for theme compatibility
val StudioBackground = YtmBackground
val StudioSurface = YtmSurface
val StudioSurfaceVariant = YtmSurfaceVariant
val StudioCardBorder = YtmCardBorder

// Audiophile Hi-Res & Spatial Badges
val NeonCyan = Color(0xFF38BDF8)
val NeonCyanDim = Color(0xFF0284C7)
val CrystalIce = Color(0xFF7DD3FC)
val CrystalAurora = Color(0xFFA78BFA)
val TubeAmber = Color(0xFFFBBF24)
val TubeAmberGlow = Color(0xFFF59E0B)
val BitPerfectGreen = Color(0xFF34D399)
val LosslessGold = Color(0xFFFDE047)
val SpatialPurple = Color(0xFFC084FC)

// Text tokens
val StudioTextPrimary = YtmTextPrimary
val StudioTextSecondary = YtmTextSecondary
val StudioTextMuted = YtmTextMuted

// Vu Meter Colors
val VuGreen = Color(0xFF34D399)
val VuYellow = Color(0xFFFBBF24)
val VuRed = Color(0xFFFF0000)

val YtmRedGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF0000), Color(0xFFFF4E45))
)
