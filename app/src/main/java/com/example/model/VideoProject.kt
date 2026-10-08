package com.example.model

enum class AspectRatioType(val titleBn: String, val ratio: Float, val label: String) {
    PORTRAIT_9_16("রিল / শর্টস (9:16)", 9f / 16f, "9:16"),
    SQUARE_1_1("স্কয়ার / পোস্ট (1:1)", 1f, "1:1"),
    LANDSCAPE_16_9("ইউটিউব / ল্যান্ডস্কেপ (16:9)", 16f / 9f, "16:9")
}

enum class VoiceStyle(val titleBn: String, val titleEn: String, val isFemale: Boolean) {
    BENGALI_FEMALE_NATURAL("বাঙালি নারী (স্বাভাবিক)", "Bengali Female Natural", true),
    BENGALI_FEMALE_PROFESSIONAL("বাঙালি নারী (প্রফেশনাল)", "Bengali Female Professional", true),
    BENGALI_FEMALE_EMOTIONAL("বাঙালি নারী (আবেগঘন)", "Bengali Female Emotional", true),
    BENGALI_FEMALE_PROMOTIONAL("বাঙালি নারী (বিজ্ঞাপনী)", "Bengali Female Promotional", true),
    BENGALI_MALE_NATURAL("বাঙালি পুরুষ (স্বাভাবিক)", "Bengali Male Natural", false),
    BENGALI_MALE_PROFESSIONAL("বাঙালি পুরুষ (প্রফেশনাল)", "Bengali Male Professional", false),
    BENGALI_MALE_EMOTIONAL("বাঙালি পুরুষ (আবেগঘন)", "Bengali Male Emotional", false),
    BENGALI_MALE_PROMOTIONAL("বাঙালি পুরুষ (বিজ্ঞাপনী)", "Bengali Male Promotional", false)
}

enum class MusicTrack(val titleBn: String, val moodBn: String) {
    BENGALI_FLUTE("ঐতিহ্যবাহী বাঁশির সুর", "ধ্রুপদী ও স্নিগ্ধ"),
    RABINDRA_MELODY("রবীন্দ্র সঙ্গীতের মেলোডি", "মনোরম ও মধুর"),
    BAUL_FUSION("বাউল একতারা ফিউশন", "মাটির সুর ও স্পন্দন"),
    MODERN_COMMERCIAL("মডার্ন কমার্শিয়াল বিট", "প্রাণবন্ত ও আকর্ষক"),
    FESTIVE_SHEHNAI("উৎসবের সানাই ও ঢাক", "আনন্দ ও উৎসবমুখর"),
    NONE("কোনো মিউজিক নেই", "শুধু ভয়েসওভার")
}

enum class VideoTransition(val titleBn: String) {
    FADE("ফেড ট্রানজিশন"),
    CROSS_ZOOM("জুম ট্রানজিশন"),
    SLIDE("স্লাইড ট্রানজিশন"),
    CUT("ডাইরেক্ট কাট")
}

data class ScenePlan(
    val id: String,
    val order: Int,
    val titleBn: String,
    val visualDescriptionBn: String,
    val narrationScriptBn: String,
    val onScreenCaptionBn: String,
    val durationSeconds: Int = 6,
    val mediaUri: String? = null,
    val drawableResId: Int? = null,
    val isVideoMedia: Boolean = false,
    val transition: VideoTransition = VideoTransition.FADE
)

data class VideoProject(
    val id: String,
    val titleBn: String,
    val topicBn: String,
    val businessBn: String = "অন্বেষার রসনা বিলাস",
    val purposeBn: String = "ব্যবসা ও রেস্তোরাঁ প্রচার",
    val styleBn: String = "Cinematic Bengali Heritage",
    val targetDurationSeconds: Int = 30, // 30, 45, 60, 90, or custom
    val aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
    val voiceStyle: VoiceStyle = VoiceStyle.BENGALI_FEMALE_NATURAL,
    val musicTrack: MusicTrack = MusicTrack.BENGALI_FLUTE,
    val scenes: List<ScenePlan> = emptyList(),
    val isAiGeneratedVisuals: Boolean = true,
    val fullScriptBn: String = "",
    val musicVolume: Float = 0.35f,
    val voiceVolume: Float = 1.0f,
    val isMuted: Boolean = false
) {
    val totalCalculatedDurationSeconds: Int
        get() = scenes.sumOf { it.durationSeconds }.coerceAtLeast(30)
}
