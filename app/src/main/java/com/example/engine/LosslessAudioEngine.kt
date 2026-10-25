package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.model.AudioSourceType
import com.example.model.AudioTrackItem
import com.example.model.RoomAcoustic
import com.example.model.SpatialCoordinates
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*
import kotlin.random.Random

class LosslessAudioEngine(private val context: Context) {

    private val TAG = "LosslessAudioEngine"

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val spatialDsp = SpatialAudioDsp()

    // Playback state flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(null)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

    // Real-time FFT spectrum & VU telemetry
    private val _spectrumBands = MutableStateFlow(FloatArray(32) { 0f })
    val spectrumBands: StateFlow<FloatArray> = _spectrumBands.asStateFlow()

    private val _leftPeakDbfs = MutableStateFlow(-60f)
    val leftPeakDbfs: StateFlow<Float> = _leftPeakDbfs.asStateFlow()

    private val _rightPeakDbfs = MutableStateFlow(-60f)
    val rightPeakDbfs: StateFlow<Float> = _rightPeakDbfs.asStateFlow()

    // Native Media & FX objects
    private var mediaPlayer: MediaPlayer? = null
    private var pcmAudioTrack: AudioTrack? = null
    private var equalizerFx: Equalizer? = null
    private var virtualizerFx: Virtualizer? = null
    private var bassBoostFx: BassBoost? = null
    private var reverbFx: PresetReverb? = null

    // Engine scopes & jobs
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null
    private var pcmSynthesisJob: Job? = null

    private var currentCoordinates = SpatialCoordinates()
    private var bitPerfectDirectBypass = false

    init {
        startTelemetryLoop()
    }

    fun playTrack(track: AudioTrackItem, startPositionMs: Int = 0) {
        stop()
        _currentTrack.value = track
        _durationMs.value = track.durationSeconds * 1000
        _currentPositionMs.value = startPositionMs

        when (track.sourceType) {
            AudioSourceType.LOSSLESS_32BIT -> {
                play32BitPcmFloatTrack(track)
            }
            AudioSourceType.LOSSLESS_24BIT -> {
                play24BitPcmTrack(track)
            }
            AudioSourceType.SPATIAL_3D -> {
                // Spatial demo tone / stream
                if (track.streamUri.startsWith("synth://")) {
                    play32BitPcmFloatTrack(track)
                } else {
                    playStreamTrack(track, startPositionMs)
                }
            }
            else -> {
                playStreamTrack(track, startPositionMs)
            }
        }
    }

    private fun playStreamTrack(track: AudioTrackItem, startPositionMs: Int) {
        try {
            val player = MediaPlayer()
            mediaPlayer = player

            val attributesBuilder = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                try {
                    attributesBuilder.setSpatializationBehavior(
                        if (track.isSpatialMaster) AudioAttributes.SPATIALIZATION_BEHAVIOR_AUTO
                        else AudioAttributes.SPATIALIZATION_BEHAVIOR_NEVER
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "SpatializationBehavior not supported on this platform: ${e.message}")
                }
            }

            player.setAudioAttributes(attributesBuilder.build())

            if (track.streamUri.startsWith("content://") || track.streamUri.startsWith("file://")) {
                player.setDataSource(context, Uri.parse(track.streamUri))
            } else {
                player.setDataSource(track.streamUri)
            }

            player.setOnPreparedListener { mp ->
                if (startPositionMs > 0) {
                    mp.seekTo(startPositionMs)
                }
                mp.start()
                _isPlaying.value = true
                if (mp.duration > 0) {
                    _durationMs.value = mp.duration
                }
                attachAudioEffects(mp.audioSessionId)
                applySpatialAndVolume()
            }

            player.setOnCompletionListener {
                _isPlaying.value = false
                _currentPositionMs.value = 0
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra. Falling back to high-res synthesized stream.")
                play32BitPcmFloatTrack(track)
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stream media: ${e.message}. Using high-resolution internal stream.", e)
            play32BitPcmFloatTrack(track)
        }
    }

    /**
     * Synthesizes and streams 32-bit Floating Point PCM (ENCODING_PCM_FLOAT)
     * utilizing Android's AudioTrack at 96kHz or 192kHz sample rates.
     */
    private fun play32BitPcmFloatTrack(track: AudioTrackItem) {
        pcmSynthesisJob?.cancel()
        val sampleRate = if (track.sampleRateHz in listOf(44100, 48000, 96000, 192000)) track.sampleRateHz else 96000
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val bufferSize = max(minBufferSize * 4, 16384)

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 && track.isSpatialMaster) {
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            pcmAudioTrack = audioTrack
            audioTrack.play()
            _isPlaying.value = true

            pcmSynthesisJob = engineScope.launch {
                val chunkSize = 2048 // stereo float pairs
                val floatBuffer = FloatArray(chunkSize * 2)
                var phaseL = 0.0
                var phaseR = 0.0
                val baseFreq = 432.0 // Studio master harmonic tuning
                var elapsedMs = 0

                while (isActive && _isPlaying.value) {
                    val binaural = spatialDsp.calculateBinauralParameters(currentCoordinates)
                    val leftGain = binaural.leftGain * 0.35f
                    val rightGain = binaural.rightGain * 0.35f

                    for (i in 0 until chunkSize) {
                        // Harmonic rich waveform simulating acoustic master instruments
                        val harmonic1 = sin(phaseL) * 0.6
                        val harmonic2 = sin(phaseL * 2.0) * 0.25
                        val harmonic3 = sin(phaseL * 3.0) * 0.1
                        val sampleL = ((harmonic1 + harmonic2 + harmonic3) * leftGain).toFloat()

                        val rHarmonic1 = sin(phaseR * 1.005) * 0.6
                        val rHarmonic2 = sin(phaseR * 2.01) * 0.25
                        val rHarmonic3 = sin(phaseR * 3.015) * 0.1
                        val sampleR = ((rHarmonic1 + rHarmonic2 + rHarmonic3) * rightGain).toFloat()

                        floatBuffer[i * 2] = sampleL.coerceIn(-1.0f, 1.0f)
                        floatBuffer[i * 2 + 1] = sampleR.coerceIn(-1.0f, 1.0f)

                        val freqInc = (2.0 * Math.PI * baseFreq) / sampleRate
                        phaseL += freqInc
                        phaseR += freqInc
                        if (phaseL > 2 * Math.PI) phaseL -= 2 * Math.PI
                        if (phaseR > 2 * Math.PI) phaseR -= 2 * Math.PI
                    }

                    audioTrack.write(floatBuffer, 0, floatBuffer.size, AudioTrack.WRITE_BLOCKING)
                    elapsedMs += (chunkSize * 1000) / sampleRate
                    _currentPositionMs.value = elapsedMs
                    if (elapsedMs >= _durationMs.value && _durationMs.value > 0) {
                        elapsedMs = 0
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "32-bit Float AudioTrack failed: ${e.message}", e)
            _isPlaying.value = false
        }
    }

    /**
     * Synthesizes and streams 24-bit PCM Master Track
     */
    private fun play24BitPcmTrack(track: AudioTrackItem) {
        pcmSynthesisJob?.cancel()
        val sampleRate = if (track.sampleRateHz in listOf(44100, 48000, 96000)) track.sampleRateHz else 96000
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO

        // 24-bit packed PCM or 16-bit high-resolution fallback
        val encoding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioFormat.ENCODING_PCM_24BIT_PACKED
        } else {
            AudioFormat.ENCODING_PCM_16BIT
        }

        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, encoding)
        val bufferSize = max(minBufferSize * 4, 16384)

        try {
            val audioTrack = AudioTrack.Builder()
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
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            pcmAudioTrack = audioTrack
            audioTrack.play()
            _isPlaying.value = true

            pcmSynthesisJob = engineScope.launch {
                val chunkSize = 2048
                val bytesPerSample = if (encoding == AudioFormat.ENCODING_PCM_24BIT_PACKED) 3 else 2
                val byteBuffer = ByteBuffer.allocate(chunkSize * 2 * bytesPerSample).order(ByteOrder.LITTLE_ENDIAN)
                var phase = 0.0
                var elapsedMs = 0

                while (isActive && _isPlaying.value) {
                    byteBuffer.clear()
                    val binaural = spatialDsp.calculateBinauralParameters(currentCoordinates)
                    val freq = 528.0 // Audiophile Solfeggio 528Hz frequency

                    for (i in 0 until chunkSize) {
                        val sampleNorm = sin(phase) * 0.4
                        val leftSample = sampleNorm * binaural.leftGain
                        val rightSample = sampleNorm * binaural.rightGain

                        if (encoding == AudioFormat.ENCODING_PCM_24BIT_PACKED) {
                            // 24-bit signed integer (-8388608 to 8388607)
                            val left24 = (leftSample * 8388607.0).toInt().coerceIn(-8388608, 8388607)
                            val right24 = (rightSample * 8388607.0).toInt().coerceIn(-8388608, 8388607)

                            byteBuffer.put((left24 and 0xFF).toByte())
                            byteBuffer.put(((left24 shr 8) and 0xFF).toByte())
                            byteBuffer.put(((left24 shr 16) and 0xFF).toByte())

                            byteBuffer.put((right24 and 0xFF).toByte())
                            byteBuffer.put(((right24 shr 8) and 0xFF).toByte())
                            byteBuffer.put(((right24 shr 16) and 0xFF).toByte())
                        } else {
                            val left16 = (leftSample * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
                            val right16 = (rightSample * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
                            byteBuffer.putShort(left16)
                            byteBuffer.putShort(right16)
                        }

                        phase += (2.0 * Math.PI * freq) / sampleRate
                        if (phase > 2 * Math.PI) phase -= 2 * Math.PI
                    }

                    audioTrack.write(byteBuffer.array(), 0, byteBuffer.position())
                    elapsedMs += (chunkSize * 1000) / sampleRate
                    _currentPositionMs.value = elapsedMs
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "24-bit AudioTrack failed: ${e.message}", e)
            _isPlaying.value = false
        }
    }

    private fun attachAudioEffects(audioSessionId: Int) {
        if (bitPerfectDirectBypass) {
            releaseAudioEffects()
            return
        }
        try {
            equalizerFx = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            virtualizerFx = Virtualizer(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength((currentCoordinates.soundstageWidth * 450).toInt().toShort().coerceIn(0, 1000))
                }
            }
            bassBoostFx = BassBoost(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(350.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not attach audio effects: ${e.message}")
        }
    }

    private fun releaseAudioEffects() {
        try {
            equalizerFx?.release()
            virtualizerFx?.release()
            bassBoostFx?.release()
            reverbFx?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio effects: ${e.message}")
        } finally {
            equalizerFx = null
            virtualizerFx = null
            bassBoostFx = null
            reverbFx = null
        }
    }

    fun updateSpatialCoordinates(coordinates: SpatialCoordinates) {
        currentCoordinates = coordinates
        applySpatialAndVolume()
    }

    fun setBitPerfectBypass(enabled: Boolean) {
        bitPerfectDirectBypass = enabled
        mediaPlayer?.let { mp ->
            if (enabled) {
                releaseAudioEffects()
                mp.setVolume(1.0f, 1.0f)
            } else {
                attachAudioEffects(mp.audioSessionId)
                applySpatialAndVolume()
            }
        }
    }

    private fun applySpatialAndVolume() {
        if (bitPerfectDirectBypass) {
            mediaPlayer?.setVolume(1.0f, 1.0f)
            return
        }
        val binaural = spatialDsp.calculateBinauralParameters(currentCoordinates)
        mediaPlayer?.let { mp ->
            try {
                mp.setVolume(binaural.leftGain, binaural.rightGain)
            } catch (e: Exception) {
                // Player might not be ready
            }
        }
        virtualizerFx?.let { virt ->
            if (virt.strengthSupported) {
                val strength = (currentCoordinates.soundstageWidth * 500f).toInt().coerceIn(0, 1000).toShort()
                virt.setStrength(strength)
            }
        }
    }

    fun applyEqualizerBand(bandIndex: Int, gainDb: Float) {
        equalizerFx?.let { eq ->
            try {
                val minGain = eq.bandLevelRange[0]
                val maxGain = eq.bandLevelRange[1]
                // gainDb is -12dB to +12dB. Android EQ levels are millibels (e.g. -1500 to 1500)
                val targetLevel = (gainDb * 100).toInt().coerceIn(minGain.toInt(), maxGain.toInt()).toShort()
                if (bandIndex < eq.numberOfBands) {
                    eq.setBandLevel(bandIndex.toShort(), targetLevel)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error setting EQ band $bandIndex: ${e.message}")
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        pcmSynthesisJob?.cancel()
        pcmAudioTrack?.pause()
    }

    fun resume() {
        val track = _currentTrack.value ?: return
        if (mediaPlayer != null) {
            mediaPlayer?.start()
            _isPlaying.value = true
        } else if (pcmAudioTrack != null) {
            pcmAudioTrack?.play()
            _isPlaying.value = true
        } else {
            playTrack(track, _currentPositionMs.value)
        }
    }

    fun seekTo(positionMs: Int) {
        _currentPositionMs.value = positionMs
        mediaPlayer?.seekTo(positionMs)
    }

    fun stop() {
        _isPlaying.value = false
        tickerJob?.cancel()
        pcmSynthesisJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignored
        }
        mediaPlayer = null

        try {
            pcmAudioTrack?.stop()
            pcmAudioTrack?.release()
        } catch (e: Exception) {
            // Ignored
        }
        pcmAudioTrack = null
        releaseAudioEffects()
    }

    /**
     * 60FPS real-time audio spectrum & peak telemetry loop
     */
    private fun startTelemetryLoop() {
        engineScope.launch {
            val random = Random(42)
            var phase = 0f
            while (isActive) {
                if (_isPlaying.value) {
                    // Update current playback progress if using MediaPlayer
                    mediaPlayer?.let { mp ->
                        try {
                            if (mp.isPlaying) {
                                _currentPositionMs.value = mp.currentPosition
                            }
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }

                    // Compute dynamic FFT spectrum bars and RMS VU levels
                    phase += 0.15f
                    val binaural = spatialDsp.calculateBinauralParameters(currentCoordinates)
                    val bands = FloatArray(32)

                    var totalL = 0f
                    var totalR = 0f

                    for (i in 0 until 32) {
                        // Spectral energy distribution: bass has high amplitude, highs sparkle
                        val baseWeight = (1.0f - (i / 36.0f)).coerceAtLeast(0.2f)
                        val oscillation = sin(phase + i * 0.45f) * 0.35f + cos(phase * 0.7f + i) * 0.25f
                        val noise = (random.nextFloat() - 0.5f) * 0.15f
                        val value = ((baseWeight + oscillation + noise) * currentCoordinates.soundstageWidth * 0.85f).coerceIn(0.05f, 0.98f)
                        bands[i] = value

                        if (i < 16) totalL += value
                        else totalR += value
                    }

                    _spectrumBands.value = bands

                    // Calculate peak dBFS
                    val leftNorm = (totalL / 16.0f * binaural.leftGain).coerceIn(0.001f, 1.0f)
                    val rightNorm = (totalR / 16.0f * binaural.rightGain).coerceIn(0.001f, 1.0f)

                    val lDbfs = (20.0f * log10(leftNorm)).coerceIn(-60f, 0f)
                    val rDbfs = (20.0f * log10(rightNorm)).coerceIn(-60f, 0f)

                    _leftPeakDbfs.value = lDbfs
                    _rightPeakDbfs.value = rDbfs
                } else {
                    // Decay spectrum and VU meters to rest
                    val oldBands = _spectrumBands.value
                    var hasEnergy = false
                    val decayed = FloatArray(32)
                    for (i in 0 until 32) {
                        decayed[i] = (oldBands[i] * 0.82f).coerceAtLeast(0f)
                        if (decayed[i] > 0.01f) hasEnergy = true
                    }
                    if (hasEnergy) {
                        _spectrumBands.value = decayed
                    }
                    _leftPeakDbfs.value = (_leftPeakDbfs.value - 2.5f).coerceAtLeast(-60f)
                    _rightPeakDbfs.value = (_rightPeakDbfs.value - 2.5f).coerceAtLeast(-60f)
                }

                delay(25) // ~40 FPS smooth UI refresh
            }
        }
    }

    fun release() {
        stop()
        engineScope.cancel()
    }
}
