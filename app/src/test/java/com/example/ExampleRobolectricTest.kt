package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.dsp.StudioEqualizer
import com.example.engine.SpatialAudioDsp
import com.example.model.HiResFilterType
import com.example.model.HiResUpsamplerConfig
import com.example.model.RoomAcoustic
import com.example.model.SpatialCoordinates
import com.example.youtube.api.YouTubeRetrofitClient
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Beats", appName)
  }

  @Test
  fun `test spatial audio dsp calculations`() {
    val dsp = SpatialAudioDsp()
    val coords = SpatialCoordinates(
      azimuthDegrees = 45f,
      distanceMeters = 1.5f,
      roomAcoustic = RoomAcoustic.STUDIO_CONTROL_ROOM
    )
    val gains = dsp.calculateBinauralParameters(coords)
    assertTrue("Left gain should be positive", gains.leftGain > 0f)
    assertTrue("Right gain should be positive", gains.rightGain > 0f)
    assertTrue("Right ear should receive earlier or equal arrival for positive azimuth", gains.rightDelayMs <= gains.leftDelayMs)
  }

  @Test
  fun `test 10-band studio equalizer presets`() {
    val presets = StudioEqualizer.PRESETS
    assertTrue("Should have multiple studio presets", presets.size >= 5)
    val harman = presets.first { it.name.contains("Harman") }
    assertEquals("Harman target should have 10 bands", 10, harman.gains.size)
  }

  @Test
  fun `test retrofit youtube music service initialization`() {
    val service = YouTubeRetrofitClient.getService()
    assertNotNull("Retrofit service should be initialized", service)
    assertTrue("Default instance should be a valid URL", YouTubeRetrofitClient.currentBaseUrl.startsWith("https://"))
  }

  @Test
  fun `test hi res audio upsampler configuration and filters`() {
    val config = HiResUpsamplerConfig(
      isEnabled = true,
      targetSampleRateHz = 192000,
      targetBitDepth = 32,
      filterType = HiResFilterType.APODIZING_FAST_ROLLOFF
    )
    assertTrue("Hi-Res sampling rate should be >= 96kHz", config.targetSampleRateHz >= 96000)
    assertTrue("Hi-Res bit depth should be >= 24", config.targetBitDepth >= 24)
    assertEquals(HiResFilterType.APODIZING_FAST_ROLLOFF, config.filterType)
  }
}
