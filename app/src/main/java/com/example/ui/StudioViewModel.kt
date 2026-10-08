package com.example.ui

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.StudioRepository
import com.example.model.AspectRatioType
import com.example.model.BrandProfile
import com.example.model.ExportedVideo
import com.example.model.MusicTrack
import com.example.model.ScenePlan
import com.example.model.VideoProject
import com.example.model.VideoTransition
import com.example.model.VoiceStyle
import com.example.model.WatermarkPosition
import com.example.model.WatermarkSize
import com.example.service.AiScriptService
import com.example.service.AiVideoService
import com.example.service.CaptionService
import com.example.service.CaptionStyleConfig
import com.example.service.DefaultAiScriptService
import com.example.service.DefaultAiVideoService
import com.example.service.DefaultCaptionService
import com.example.service.DefaultMediaCompositionService
import com.example.service.DefaultMusicService
import com.example.service.DefaultVoiceService
import com.example.service.ExportProgress
import com.example.service.MediaCompositionService
import com.example.service.MusicService
import com.example.service.VoiceService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class StudioScreen(val titleBn: String, val iconName: String) {
    AI_GENERATOR("AI ভিডিও", "auto_awesome"),
    USER_MEDIA("নিজের মিডিয়া", "photo_library"),
    SCRIPT_VOICE("স্ক্রিপ্ট ও ভয়েস", "record_voice_over"),
    MY_VIDEOS("আমার ভিডিও", "video_library"),
    BRAND_SETTINGS("ব্র্যান্ড সেটিংস", "storefront")
}

data class UserMediaItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val isVideo: Boolean = false,
    val durationSeconds: Int = 5,
    val titleBn: String = "মিডিয়া ফাইল",
    val drawableResId: Int? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudioRepository(application)

    // Modular Services
    val aiScriptService: AiScriptService = DefaultAiScriptService()
    val aiVideoService: AiVideoService = DefaultAiVideoService()
    val voiceService: VoiceService = DefaultVoiceService(application)
    val captionService: CaptionService = DefaultCaptionService()
    val musicService: MusicService = DefaultMusicService()
    val mediaCompositionService: MediaCompositionService = DefaultMediaCompositionService()

    // Navigation
    private val _currentScreen = MutableStateFlow(StudioScreen.AI_GENERATOR)
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    // Brand Profile
    val brandProfile: StateFlow<BrandProfile> = repository.brandProfileFlow
    val exportedVideos: StateFlow<List<ExportedVideo>> = repository.exportedVideosFlow

    // Active Project Draft
    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    // AI Generator Input Form
    val aiTopic = MutableStateFlow("ঘরোয়া খাঁটি বাঙালি রসনা বিলাস ও স্পেশাল মেনু")
    val aiBusiness = MutableStateFlow("অন্বেষার রসনা বিলাস")
    val aiPurpose = MutableStateFlow("ফেসবুক ও ইনস্টাগ্রাম রিল প্রচার")
    val aiStyle = MutableStateFlow("Cinematic Heritage Bengali")
    val aiDurationSeconds = MutableStateFlow(30)
    val aiVoiceStyle = MutableStateFlow(VoiceStyle.BENGALI_FEMALE_NATURAL)
    val aiMusicTrack = MutableStateFlow(MusicTrack.BENGALI_FLUTE)
    val aiAspectRatio = MutableStateFlow(AspectRatioType.PORTRAIT_9_16)
    val aiInstructions = MutableStateFlow("বাঙালি খাবারের সুস্বাদু ঘ্রাণ এবং বিশেষ অফারের কথা উল্লেখ করুন।")
    val isGeneratingScript = MutableStateFlow(false)

    // User Media State
    val userMediaTargetDuration = MutableStateFlow(30)
    val previewMediaItem = MutableStateFlow<UserMediaItem?>(null)
    val userMediaList = MutableStateFlow<List<UserMediaItem>>(
        listOf(
            UserMediaItem(
                uri = Uri.EMPTY,
                isVideo = false,
                durationSeconds = 6,
                titleBn = "১. সুস্বাদু বাঙালি রাজকীয় থালি",
                drawableResId = R.drawable.scene_thali_delight
            ),
            UserMediaItem(
                uri = Uri.EMPTY,
                isVideo = false,
                durationSeconds = 6,
                titleBn = "২. খাঁটি সর্ষে ইলিশ ও পাতুরি",
                drawableResId = R.drawable.scene_ilish_paturi
            ),
            UserMediaItem(
                uri = Uri.EMPTY,
                isVideo = false,
                durationSeconds = 6,
                titleBn = "৩. স্পেশাল মটন কষা মাংস",
                drawableResId = R.drawable.scene_kasha_mangsho
            ),
            UserMediaItem(
                uri = Uri.EMPTY,
                isVideo = false,
                durationSeconds = 6,
                titleBn = "৪. শেষ পাতে নরম রসগোল্লা",
                drawableResId = R.drawable.scene_rosogolla
            ),
            UserMediaItem(
                uri = Uri.EMPTY,
                isVideo = false,
                durationSeconds = 6,
                titleBn = "৫. ঘরের টাটকা রান্নার প্রস্তুতি",
                drawableResId = R.drawable.scene_kitchen_flame
            )
        )
    )

    // Preview Player State
    val isPlaying = MutableStateFlow(false)
    val playbackPositionSeconds = MutableStateFlow(0f)
    val isMuted = MutableStateFlow(false)
    val volumeLevel = MutableStateFlow(0.85f)
    private var playbackJob: Job? = null

    // Export State
    val isExporting = MutableStateFlow(false)
    val exportProgressPercent = MutableStateFlow(0)
    val exportStatusText = MutableStateFlow("")
    val lastExportedVideo = MutableStateFlow<ExportedVideo?>(null)
    val exportError = MutableStateFlow<String?>(null)

    // Caption Configuration
    val captionConfig = MutableStateFlow(CaptionStyleConfig())

    init {
        // Generate an initial default project draft so preview works immediately
        generateInitialProject()
    }

    fun setScreen(screen: StudioScreen) {
        _currentScreen.value = screen
    }

    private fun generateInitialProject() {
        viewModelScope.launch {
            val project = aiScriptService.generateScriptAndScenes(
                topic = aiTopic.value,
                business = aiBusiness.value,
                purpose = aiPurpose.value,
                style = aiStyle.value,
                durationSeconds = 30,
                voiceStyle = aiVoiceStyle.value,
                musicTrack = aiMusicTrack.value,
                aspectRatio = aiAspectRatio.value,
                extraInstructions = aiInstructions.value,
                brandProfile = brandProfile.value
            )
            _currentProject.value = project
        }
    }

    fun generateAiVideo() {
        viewModelScope.launch {
            isGeneratingScript.value = true
            try {
                val project = aiScriptService.generateScriptAndScenes(
                    topic = aiTopic.value,
                    business = aiBusiness.value,
                    purpose = aiPurpose.value,
                    style = aiStyle.value,
                    durationSeconds = aiDurationSeconds.value,
                    voiceStyle = aiVoiceStyle.value,
                    musicTrack = aiMusicTrack.value,
                    aspectRatio = aiAspectRatio.value,
                    extraInstructions = aiInstructions.value,
                    brandProfile = brandProfile.value
                )
                _currentProject.value = project
                playbackPositionSeconds.value = 0f
                _currentScreen.value = StudioScreen.SCRIPT_VOICE
            } finally {
                isGeneratingScript.value = false
            }
        }
    }

    fun createProjectFromUserMedia() {
        viewModelScope.launch {
            val items = userMediaList.value
            if (items.isEmpty()) return@launch

            val scenes = items.mapIndexed { index, item ->
                ScenePlan(
                    id = item.id,
                    order = index + 1,
                    titleBn = item.titleBn,
                    visualDescriptionBn = "ব্যবহারকারীর নিজস্ব মিডিয়া",
                    narrationScriptBn = "স্বাদ ও ঐতিহ্যে অনন্য অন্বেষার রসনা বিলাস। মায়ের হাতের রান্নার খাঁটি স্বাদ।",
                    onScreenCaptionBn = item.titleBn,
                    durationSeconds = item.durationSeconds,
                    mediaUri = if (item.uri != Uri.EMPTY) item.uri.toString() else null,
                    drawableResId = item.drawableResId ?: R.drawable.scene_thali_delight,
                    isVideoMedia = item.isVideo,
                    transition = VideoTransition.FADE
                )
            }

            val project = VideoProject(
                id = UUID.randomUUID().toString(),
                titleBn = "${brandProfile.value.businessNameBn} - মাই মিডিয়া ভিডিও",
                topicBn = "নিজস্ব ছবি ও ভিডিও দিয়ে উপস্থাপনা",
                businessBn = brandProfile.value.businessNameBn,
                targetDurationSeconds = scenes.sumOf { it.durationSeconds }.coerceAtLeast(30),
                scenes = scenes,
                isAiGeneratedVisuals = false,
                voiceStyle = aiVoiceStyle.value,
                musicTrack = aiMusicTrack.value
            )

            _currentProject.value = project
            playbackPositionSeconds.value = 0f
            _currentScreen.value = StudioScreen.SCRIPT_VOICE
        }
    }

    // Playback Controls
    fun togglePlayPause() {
        if (isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val totalSec = _currentProject.value?.totalCalculatedDurationSeconds ?: 30
            while (isActive && isPlaying.value) {
                delay(100)
                var next = playbackPositionSeconds.value + 0.1f
                if (next >= totalSec) {
                    next = 0f
                }
                playbackPositionSeconds.value = next
            }
        }

        // Voice preview trigger on scene start
        val currentScene = getCurrentActiveScene()
        if (currentScene != null && !isMuted.value) {
            voiceService.previewVoice(currentScene.narrationScriptBn, _currentProject.value?.voiceStyle ?: VoiceStyle.BENGALI_FEMALE_NATURAL)
        }
    }

    fun pausePlayback() {
        isPlaying.value = false
        playbackJob?.cancel()
        voiceService.stopVoice()
    }

    fun seekTo(seconds: Float) {
        val totalSec = _currentProject.value?.totalCalculatedDurationSeconds ?: 30
        playbackPositionSeconds.value = seconds.coerceIn(0f, totalSec.toFloat())
    }

    fun toggleMute() {
        isMuted.value = !isMuted.value
        if (isMuted.value) {
            voiceService.stopVoice()
        }
    }

    fun setVolume(vol: Float) {
        volumeLevel.value = vol.coerceIn(0f, 1f)
    }

    fun getCurrentActiveScene(): ScenePlan? {
        val project = _currentProject.value ?: return null
        var accumulated = 0
        val currentSec = playbackPositionSeconds.value.toInt()
        for (scene in project.scenes) {
            accumulated += scene.durationSeconds
            if (currentSec < accumulated) {
                return scene
            }
        }
        return project.scenes.lastOrNull()
    }

    // Script & Scene Editing
    fun updateSceneNarration(sceneId: String, newNarration: String) {
        val project = _currentProject.value ?: return
        val updated = project.scenes.map {
            if (it.id == sceneId) it.copy(narrationScriptBn = newNarration) else it
        }
        _currentProject.value = project.copy(scenes = updated)
    }

    fun updateSceneCaption(sceneId: String, newCaption: String) {
        val project = _currentProject.value ?: return
        val updated = project.scenes.map {
            if (it.id == sceneId) it.copy(onScreenCaptionBn = newCaption) else it
        }
        _currentProject.value = project.copy(scenes = updated)
    }

    fun setProjectVoiceStyle(voice: VoiceStyle) {
        val project = _currentProject.value ?: return
        _currentProject.value = project.copy(voiceStyle = voice)
        aiVoiceStyle.value = voice
    }

    fun setProjectMusicTrack(music: MusicTrack) {
        val project = _currentProject.value ?: return
        _currentProject.value = project.copy(musicTrack = music)
        aiMusicTrack.value = music
    }

    fun previewActiveVoiceover() {
        val scene = getCurrentActiveScene() ?: _currentProject.value?.scenes?.firstOrNull() ?: return
        voiceService.previewVoice(scene.narrationScriptBn, _currentProject.value?.voiceStyle ?: VoiceStyle.BENGALI_FEMALE_NATURAL)
    }

    // Brand Profile Actions
    fun updateBrandProfile(profile: BrandProfile) {
        repository.updateBrandProfile(profile)
    }

    fun setWatermarkPosition(position: WatermarkPosition) {
        val current = brandProfile.value
        repository.updateBrandProfile(current.copy(watermarkPosition = position))
    }

    fun setWatermarkSize(size: WatermarkSize) {
        val current = brandProfile.value
        repository.updateBrandProfile(current.copy(watermarkSize = size))
    }

    fun setWatermarkOpacity(opacity: Float) {
        val current = brandProfile.value
        repository.updateBrandProfile(current.copy(watermarkOpacity = opacity))
    }

    fun toggleShowLogo(show: Boolean) {
        val current = brandProfile.value
        repository.updateBrandProfile(current.copy(showLogo = show))
    }

    fun resetLogoToOfficial() {
        val current = brandProfile.value
        repository.updateBrandProfile(current.copy(customLogoUri = null))
    }

    // MP4 Export Action
    fun exportCurrentProject() {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            isExporting.value = true
            exportError.value = null
            pausePlayback()

            mediaCompositionService.exportProjectToMp4(
                context = getApplication(),
                project = project,
                brandProfile = brandProfile.value,
                captionConfig = captionConfig.value
            ).collect { step ->
                when (step) {
                    is ExportProgress.Progress -> {
                        exportProgressPercent.value = step.percentage
                        exportStatusText.value = step.currentStepBn
                    }
                    is ExportProgress.Success -> {
                        isExporting.value = false
                        lastExportedVideo.value = step.video
                        repository.saveExportedVideo(step.video)
                        _currentScreen.value = StudioScreen.MY_VIDEOS
                    }
                    is ExportProgress.Error -> {
                        isExporting.value = false
                        exportError.value = step.messageBn
                    }
                }
            }
        }
    }

    // Video Library Actions
    fun deleteVideo(id: String) {
        repository.deleteExportedVideo(id)
    }

    fun renameVideo(id: String, newTitle: String) {
        repository.renameExportedVideo(id, newTitle)
    }

    // Media List Actions
    fun addUserMedia(uri: Uri, isVideo: Boolean) {
        val current = userMediaList.value.toMutableList()
        current.add(
            UserMediaItem(
                uri = uri,
                isVideo = isVideo,
                durationSeconds = 6,
                titleBn = if (isVideo) "ভিডিও দৃশ্য ${current.size + 1}" else "ছবির দৃশ্য ${current.size + 1}"
            )
        )
        userMediaList.value = current
    }

    fun addUserMediaWithMetadata(context: Context, uri: Uri, isVideo: Boolean) {
        var title = if (isVideo) "ভিডিও দৃশ্য" else "ছবির দৃশ্য"
        var durationSec = 6

        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        val name = cursor.getString(nameIdx)
                        if (!name.isNullOrBlank()) {
                            title = name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Keep default title
        }

        if (isVideo) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durMs = durStr?.toLongOrNull() ?: 6000L
                durationSec = (durMs / 1000).toInt().coerceIn(3, 60)
                retriever.release()
            } catch (e: Exception) {
                durationSec = 6
            }
        }

        val current = userMediaList.value.toMutableList()
        current.add(
            UserMediaItem(
                uri = uri,
                isVideo = isVideo,
                durationSeconds = durationSec,
                titleBn = title
            )
        )
        userMediaList.value = current
    }

    fun updateUserMediaDuration(index: Int, newDuration: Int) {
        val current = userMediaList.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            current[index] = item.copy(durationSeconds = newDuration.coerceIn(2, 60))
            userMediaList.value = current
        }
    }

    fun setTargetDurationSeconds(seconds: Int) {
        val target = seconds.coerceAtLeast(30)
        userMediaTargetDuration.value = target
        balanceUserMediaDurations(target)
    }

    fun balanceUserMediaDurations(targetSec: Int = userMediaTargetDuration.value) {
        val current = userMediaList.value
        if (current.isEmpty()) return
        val count = current.size
        val perScene = (targetSec / count).coerceAtLeast(2)
        val remainder = targetSec - (perScene * count)
        val updated = current.mapIndexed { index, item ->
            val extra = if (index == count - 1 && remainder > 0) remainder else 0
            item.copy(durationSeconds = perScene + extra)
        }
        userMediaList.value = updated
    }

    fun setPreviewItem(item: UserMediaItem?) {
        previewMediaItem.value = item
    }

    fun removeUserMedia(index: Int) {
        val current = userMediaList.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            userMediaList.value = current
        }
    }

    fun moveUserMedia(fromIndex: Int, toIndex: Int) {
        val current = userMediaList.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            userMediaList.value = current
        }
    }

    fun exportUserMediaDirectly() {
        createProjectFromUserMedia()
        exportCurrentProject()
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.release()
        playbackJob?.cancel()
    }
}
