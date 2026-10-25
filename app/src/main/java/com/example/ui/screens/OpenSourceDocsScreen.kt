package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class CodeSnippet(
    val title: String,
    val category: String,
    val description: String,
    val kotlinCode: String
)

@Composable
fun OpenSourceDocsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val snippets = remember {
        listOf(
            CodeSnippet(
                title = "32-Bit Float AudioTrack Pipeline",
                category = "32-Bit Float PCM",
                description = "Configures native Android AudioTrack for 32-bit floating point high-resolution playback with unbounded dynamic range.",
                kotlinCode = """
// 1. Android 32-bit Float AudioTrack Architecture
val sampleRate = 192000 // 192 kHz Studio Master
val channelConfig = AudioFormat.CHANNEL_OUT_STEREO

val minBufferSize = AudioTrack.getMinBufferSize(
    sampleRate,
    channelConfig,
    AudioFormat.ENCODING_PCM_FLOAT
)

val audioTrack = AudioTrack.Builder()
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                    setSpatializationBehavior(AudioAttributes.SPATIALIZATION_BEHAVIOR_AUTO)
                }
            }
            .build()
    )
    .setAudioFormat(
        AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(sampleRate)
            .setChannelMask(channelConfig)
            .build()
    )
    .setBufferSizeInBytes(max(minBufferSize * 4, 16384))
    .setTransferMode(AudioTrack.MODE_STREAM)
    .build()

audioTrack.play()

// Writing 32-bit float samples (-1.0f .. +1.0f)
val floatBuffer = FloatArray(2048) { /* PCM float samples */ }
audioTrack.write(floatBuffer, 0, floatBuffer.size, AudioTrack.WRITE_BLOCKING)
""".trimIndent()
            ),
            CodeSnippet(
                title = "24-Bit Packed PCM Pipeline",
                category = "24-Bit Lossless",
                description = "Low-latency bit-perfect 24-bit integer PCM pipeline using 3-byte packed byte arrays for high-fidelity DAC pass-through.",
                kotlinCode = """
// 2. Android 24-bit Packed PCM Architecture
val sampleRate = 96000 // 96 kHz FLAC Master
val encoding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    AudioFormat.ENCODING_PCM_24BIT_PACKED
} else {
    AudioFormat.ENCODING_PCM_16BIT
}

val track24Bit = AudioTrack.Builder()
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    )
    .setAudioFormat(
        AudioFormat.Builder()
            .setEncoding(encoding)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
    )
    .setTransferMode(AudioTrack.MODE_STREAM)
    .build()

// Convert normalized float sample (-1.0 .. 1.0) to 24-bit packed 3 bytes (Little Endian)
fun put24BitSample(buffer: ByteBuffer, sample: Float) {
    val intVal = (sample * 8388607.0f).toInt().coerceIn(-8388608, 8388607)
    buffer.put((intVal and 0xFF).toByte())
    buffer.put(((intVal shr 8) and 0xFF).toByte())
    buffer.put(((intVal shr 16) and 0xFF).toByte())
}
""".trimIndent()
            ),
            CodeSnippet(
                title = "3D Binaural Spatializer HRTF Math",
                category = "3D Spatial Audio",
                description = "Spherical Woodworth Interaural Time Difference (ITD) and Interaural Level Difference (ILD) acoustic soundstage calculations.",
                kotlinCode = """
// 3. Spherical Head HRTF & Crossfeed Math
fun computeSpatialGains(azimuthDeg: Float, distanceMeters: Float): Pair<Float, Float> {
    val rad = Math.toRadians(azimuthDeg.toDouble()).toFloat()
    
    // Woodworth ITD (Interaural Time Difference)
    val headRadius = 0.0875f // ~8.75 cm human head
    val speedOfSound = 343.0f // m/s
    val itdSeconds = (headRadius / speedOfSound) * (sin(rad) + rad)

    // ILD (Interaural Level Difference) High-frequency head shadowing
    val pan = sin(rad)
    val leftGain = ((1.0f - pan) * 0.5f).coerceIn(0.05f, 1.0f)
    val rightGain = ((1.0f + pan) * 0.5f).coerceIn(0.05f, 1.0f)

    // Binaural Bauer/Chu Moy stereophonic crossfeed
    val crossfeed = 0.25f // 25% sub-bass blend
    val finalL = leftGain * (1f - crossfeed) + rightGain * crossfeed
    val finalR = rightGain * (1f - crossfeed) + leftGain * crossfeed

    return Pair(finalL, finalR)
}
""".trimIndent()
            ),
            CodeSnippet(
                title = "Retrofit YouTube Music Stream Pipeline",
                category = "YouTube Music Hub",
                description = "Retrofit REST API integration querying Invidious/Piped endpoints to search YouTube Music and extract raw Opus/AAC media streams.",
                kotlinCode = """
// 4. Retrofit YouTube Music Service & Stream Resolver
interface YouTubeMusicApiService {
    @GET("api/v1/search")
    suspend fun searchMusic(
        @Query("q") query: String,
        @Query("type") type: String = "video"
    ): List<InvidiousSearchResultDto>

    @GET("api/v1/videos/{videoId}")
    suspend fun getVideoDetails(
        @Path("videoId") videoId: String
    ): InvidiousVideoDetailDto
}

// Build Retrofit with OkHttpClient & Moshi Converter
val retrofit = Retrofit.Builder()
    .baseUrl("https://inv.tux.pizza/")
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

val api = retrofit.create(YouTubeMusicApiService::class.java)

// Search & resolve direct audio stream (Opus / AAC 320kbps)
val searchResults = api.searchMusic("Daft Punk Lossless")
val details = api.getVideoDetails(searchResults.first().videoId!!)
val audioStreamUrl = details.adaptiveFormats
    ?.filter { it.type?.contains("audio") == true }
    ?.maxByOrNull { it.bitrate?.toLongOrNull() ?: 0L }
    ?.url

// Stream directly into LosslessAudioEngine with 24-bit spatial DSP!
mediaPlayer.setDataSource(audioStreamUrl)
mediaPlayer.prepareAsync()
""".trimIndent()
            ),
            CodeSnippet(
                title = "Bit-Perfect DAC Hardware Inspector",
                category = "Bit-Perfect DAC",
                description = "Detects external USB DACs, Bluetooth LDAC codecs, and internal 32-bit DAC buses with zero resampling bypass.",
                kotlinCode = """
// 5. Hardware DAC & Bit-Perfect Inspection
val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

for (device in devices) {
    when (device.type) {
        AudioDeviceInfo.TYPE_USB_DEVICE -> {
            // Bit-Perfect USB Audio Class 2.0 Direct Access
            val dacName = device.productName
            val sampleRates = device.sampleRates // e.g. [44100, 96000, 192000, 384000]
            val encodings = device.encodings
        }
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
            // High-Resolution Wireless (Sony LDAC 990kbps / Qualcomm aptX HD)
        }
    }
}
""".trimIndent()
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "32-Bit Float PCM", "24-Bit Lossless", "3D Spatial Audio", "YouTube Music Hub", "Bit-Perfect DAC")

    val filteredSnippets = remember(selectedCategory) {
        if (selectedCategory == "All") snippets
        else snippets.filter { it.category == selectedCategory }
    }

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
                    text = "Open Source Audio Engine",
                    color = StudioTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Production Kotlin Architecture & Implementation",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x2210B981))
                    .border(1.dp, BitPerfectGreen, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "APACHE 2.0 / MIT",
                    color = BitPerfectGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val selected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) TubeAmber.copy(alpha = 0.2f) else StudioSurface)
                        .border(
                            1.dp,
                            if (selected) TubeAmber else StudioCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (selected) TubeAmber else StudioTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Snippets List
        filteredSnippets.forEach { snippet ->
            SnippetCard(snippet = snippet, onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(snippet.title, snippet.kotlinCode)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied code to clipboard", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
private fun SnippetCard(
    snippet: CodeSnippet,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snippet.title,
                    color = StudioTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = snippet.description,
                    color = StudioTextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurfaceVariant)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code block
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF060910))
                    .border(1.dp, Color(0xFF1B2436), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = snippet.kotlinCode,
                    color = NeonCyanDim,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
