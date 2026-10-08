package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BrandProfile
import com.example.model.WatermarkPosition
import com.example.service.DefaultAiScriptService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("অন্বেষার ভিডিও স্টুডিও", appName)
    }

    @Test
    fun `verify brand profile default properties`() {
        val profile = BrandProfile()
        assertEquals("অন্বেষার রসনা বিলাস", profile.businessNameBn)
        assertEquals("8509360327", profile.phone)
        assertTrue(profile.showLogo)
        assertEquals(WatermarkPosition.TOP_RIGHT, profile.watermarkPosition)
        assertEquals(0.90f, profile.watermarkOpacity, 0.001f)
    }

    @Test
    fun `verify script generator produces scenes with minimum 30 seconds`() = runBlocking {
        val service = DefaultAiScriptService()
        val project = service.generateScriptAndScenes(
            topic = "বাঙালি ভোজ",
            business = "অন্বেষার রসনা বিলাস",
            purpose = "রিল",
            style = "Cinematic",
            durationSeconds = 30,
            voiceStyle = com.example.model.VoiceStyle.BENGALI_FEMALE_NATURAL,
            musicTrack = com.example.model.MusicTrack.BENGALI_FLUTE,
            aspectRatio = com.example.model.AspectRatioType.PORTRAIT_9_16,
            extraInstructions = "",
            brandProfile = BrandProfile()
        )
        assertNotNull(project)
        assertTrue(project.scenes.isNotEmpty())
        assertTrue(project.totalCalculatedDurationSeconds >= 30)
    }

    @Test
    fun `verify user media list and duration management in viewmodel`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.StudioViewModel(application)
        val initialList = viewModel.userMediaList.value
        assertTrue("User media list should not be empty", initialList.isNotEmpty())

        // Test duration balancing to target
        viewModel.setTargetDurationSeconds(45)
        assertEquals(45, viewModel.userMediaTargetDuration.value)
        val totalSec = viewModel.userMediaList.value.sumOf { it.durationSeconds }
        assertEquals(45, totalSec)

        // Test minimum duration enforcement (>= 30 seconds)
        viewModel.setTargetDurationSeconds(20) // should be clamped to at least 30
        assertTrue(viewModel.userMediaTargetDuration.value >= 30)

        // Test reordering
        val firstItem = viewModel.userMediaList.value[0]
        viewModel.moveUserMedia(0, 1)
        assertEquals(firstItem.id, viewModel.userMediaList.value[1].id)

        // Test updating single item duration
        viewModel.updateUserMediaDuration(0, 10)
        assertEquals(10, viewModel.userMediaList.value[0].durationSeconds)
    }
}
