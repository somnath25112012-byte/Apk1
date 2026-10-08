package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BrandProfile
import com.example.model.ExportedVideo
import com.example.model.WatermarkPosition
import com.example.model.WatermarkSize
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class StudioRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anweshar_studio_prefs", Context.MODE_PRIVATE)

    private val _brandProfileFlow = MutableStateFlow(loadBrandProfile())
    val brandProfileFlow: StateFlow<BrandProfile> = _brandProfileFlow.asStateFlow()

    private val _exportedVideosFlow = MutableStateFlow<List<ExportedVideo>>(emptyList())
    val exportedVideosFlow: StateFlow<List<ExportedVideo>> = _exportedVideosFlow.asStateFlow()

    init {
        loadExportedVideos()
    }

    private fun loadBrandProfile(): BrandProfile {
        return BrandProfile(
            businessNameBn = prefs.getString("biz_name_bn", "অন্বেষার রসনা বিলাস") ?: "অন্বেষার রসনা বিলাস",
            businessNameEn = prefs.getString("biz_name_en", "ANWESHAR ROSHONA BILAS") ?: "ANWESHAR ROSHONA BILAS",
            taglineBn = prefs.getString("tagline_bn", "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ।") ?: "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ।",
            location = prefs.getString("location", "বহরমপুর, মুর্শিদাবাদ, পশ্চিমবঙ্গ") ?: "বহরমপুর, মুর্শিদাবাদ, পশ্চিমবঙ্গ",
            phone = prefs.getString("phone", "8509360327") ?: "8509360327",
            website = prefs.getString("website", "anweshar-roshona-bilas.ai.studio") ?: "anweshar-roshona-bilas.ai.studio",
            customLogoUri = prefs.getString("custom_logo_uri", null),
            showLogo = prefs.getBoolean("show_logo", true),
            watermarkPosition = try {
                WatermarkPosition.valueOf(prefs.getString("wm_pos", WatermarkPosition.TOP_RIGHT.name) ?: WatermarkPosition.TOP_RIGHT.name)
            } catch (e: Exception) {
                WatermarkPosition.TOP_RIGHT
            },
            watermarkSize = try {
                WatermarkSize.valueOf(prefs.getString("wm_size", WatermarkSize.MEDIUM.name) ?: WatermarkSize.MEDIUM.name)
            } catch (e: Exception) {
                WatermarkSize.MEDIUM
            },
            watermarkOpacity = prefs.getFloat("wm_opacity", 0.90f)
        )
    }

    fun updateBrandProfile(profile: BrandProfile) {
        prefs.edit().apply {
            putString("biz_name_bn", profile.businessNameBn)
            putString("biz_name_en", profile.businessNameEn)
            putString("tagline_bn", profile.taglineBn)
            putString("location", profile.location)
            putString("phone", profile.phone)
            putString("website", profile.website)
            putString("custom_logo_uri", profile.customLogoUri)
            putBoolean("show_logo", profile.showLogo)
            putString("wm_pos", profile.watermarkPosition.name)
            putString("wm_size", profile.watermarkSize.name)
            putFloat("wm_opacity", profile.watermarkOpacity)
            apply()
        }
        _brandProfileFlow.value = profile
    }

    fun saveExportedVideo(video: ExportedVideo) {
        val currentList = _exportedVideosFlow.value.toMutableList()
        currentList.add(0, video)
        _exportedVideosFlow.value = currentList
    }

    fun deleteExportedVideo(videoId: String) {
        val currentList = _exportedVideosFlow.value.toMutableList()
        val item = currentList.find { it.id == videoId }
        if (item != null) {
            try {
                val f = File(item.filePath)
                if (f.exists()) f.delete()
            } catch (e: Exception) {}
            currentList.remove(item)
            _exportedVideosFlow.value = currentList
        }
    }

    fun renameExportedVideo(videoId: String, newTitle: String) {
        val currentList = _exportedVideosFlow.value.map {
            if (it.id == videoId) it.copy(titleBn = newTitle) else it
        }
        _exportedVideosFlow.value = currentList
    }

    private fun loadExportedVideos() {
        val outputDir = File(context.filesDir, "exported_videos")
        val files = outputDir.listFiles()?.filter { it.extension.equals("mp4", ignoreCase = true) } ?: emptyList()
        val loaded = files.mapIndexed { index, file ->
            ExportedVideo(
                id = file.name,
                titleBn = "ভিডিও প্রজেক্ট ${index + 1} (${file.nameWithoutExtension})",
                filePath = file.absolutePath,
                durationSeconds = 30,
                timestamp = file.lastModified(),
                fileSizeBytes = file.length(),
                resolutionLabel = "1080p MP4",
                thumbnailResId = R.drawable.scene_thali_delight,
                isWatermarked = true
            )
        }.sortedByDescending { it.timestamp }

        // Preload an initial sample if list is empty so the user can immediately test My Videos
        if (loaded.isEmpty()) {
            val sampleVideo = ExportedVideo(
                id = "sample_anweshar_video",
                titleBn = "অন্বেষার রসনা বিলাস - স্পেশাল বাঙালি ভোজ প্রমো",
                filePath = File(outputDir, "Sample_Promotion.mp4").absolutePath,
                durationSeconds = 30,
                timestamp = System.currentTimeMillis() - 3600000,
                fileSizeBytes = 4_850_000,
                resolutionLabel = "1080p Full HD",
                thumbnailResId = R.drawable.scene_thali_delight,
                isWatermarked = true
            )
            _exportedVideosFlow.value = listOf(sampleVideo)
        } else {
            _exportedVideosFlow.value = loaded
        }
    }
}
