package com.example.service

import com.example.model.AspectRatioType
import com.example.model.BrandProfile
import com.example.model.MusicTrack
import com.example.model.ScenePlan
import com.example.model.VideoProject
import com.example.model.VideoTransition
import com.example.model.VoiceStyle
import com.example.R
import java.util.UUID

interface AiScriptService {
    val isRealApiConnected: Boolean
    val providerName: String

    suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        musicTrack: MusicTrack,
        aspectRatio: AspectRatioType,
        extraInstructions: String,
        brandProfile: BrandProfile
    ): VideoProject
}

class DefaultAiScriptService : AiScriptService {
    override val isRealApiConnected: Boolean = false
    override val providerName: String = "Studio Local Bengali Script Engine (Offline/Fast)"

    override suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        musicTrack: MusicTrack,
        aspectRatio: AspectRatioType,
        extraInstructions: String,
        brandProfile: BrandProfile
    ): VideoProject {
        val effectiveDuration = durationSeconds.coerceAtLeast(30)
        val numScenes = when {
            effectiveDuration <= 30 -> 4
            effectiveDuration <= 45 -> 5
            effectiveDuration <= 60 -> 6
            else -> 8
        }
        val sceneDuration = (effectiveDuration / numScenes).coerceAtLeast(5)

        val activeBusiness = if (business.isNotBlank()) business else brandProfile.businessNameBn
        val activeTopic = if (topic.isNotBlank()) topic else "ঘরোয়া খাঁটি বাঙালি ভোজ"

        val defaultDrawables = listOf(
            R.drawable.scene_thali_delight,
            R.drawable.scene_kasha_mangsho,
            R.drawable.scene_ilish_paturi,
            R.drawable.scene_rosogolla,
            R.drawable.scene_kitchen_flame
        )

        val generatedScenes = mutableListOf<ScenePlan>()

        val templatePlans = listOf(
            Triple(
                "ভূমিকা ও আগমন",
                "স্বাগত জানাই $activeBusiness-এ! খাঁটি বাঙালিয়ানার সুঘ্রাণে আজ আপনার মন ভরিয়ে তুলুন।",
                "বাঙালিয়ানার সেরা স্বাদ ও ঐতিহ্যের ঠিকানা!"
            ),
            Triple(
                "স্বাদের বিশেষত্ব",
                "আমাদের প্রতিটি পদে রয়েছে মায়ের হাতের রান্নার পরম যত্ন আর খাঁটি দেশি মশলার জাদু।",
                "খাঁটি মশলা ও সেরা উপকরণে যত্নবান প্রস্তুতি"
            ),
            Triple(
                "প্রধান আকর্ষণ",
                "মাছের কালিয়া, সর্ষে ইলিশ থেকে রাজকীয় কষা মাংস—জিভে জল আনা অবিস্মরণীয় তৃপ্তি!",
                "জিভে জল আনা সুস্বাদু বাঙালি রাজকীয় পদ"
            ),
            Triple(
                "শেষ পাতে মিষ্টি",
                "খাওয়ার শেষে নরম তুলতুলে খাঁটি ছানার রসগোল্লা ও নলেন গুড়ের পায়েশে মিষ্টি মুখ।",
                "শেষ পাতে পরম তৃপ্তির মিষ্টি মুখ"
            ),
            Triple(
                "পরিবেশন ও আতিথেয়তা",
                "পরিবার কিংবা বন্ধুবান্ধব নিয়ে আসুন আমাদের পরিপাটি ও আরামদায়ক রেস্তোরাঁয়।",
                "স্নেহমাখা আন্তরিক আতিথেয়তা"
            ),
            Triple(
                "বিশেষ অফার ও উদযাপন",
                "এই বিশেষ মৌসুমে উপভোগ করুন আকর্ষণীয় অফার ও হোম ডেলিভারি সুবিধা!",
                "আকর্ষণীয় পারিবারিক অফার ও সুবিধা"
            ),
            Triple(
                "ঠিকানা ও যোগাযোগ",
                "${brandProfile.location}-এ আমরা সর্বদা আপনার সেবায় নিয়োজিত।",
                "আজই চলে আসুন বা অর্ডার করুন: ${brandProfile.phone}"
            ),
            Triple(
                "আমন্ত্রণ ও সমাপনী",
                "আজই চলে আসুন $activeBusiness-এ। ${brandProfile.taglineBn}",
                "${brandProfile.taglineBn}"
            )
        )

        for (i in 0 until numScenes) {
            val template = templatePlans[i % templatePlans.size]
            val drawableRes = defaultDrawables[i % defaultDrawables.size]
            generatedScenes.add(
                ScenePlan(
                    id = UUID.randomUUID().toString(),
                    order = i + 1,
                    titleBn = template.first,
                    visualDescriptionBn = "মনোরম সিনেমাটিক দৃশ্য: $activeTopic সম্পর্কিত ভিজ্যুয়াল",
                    narrationScriptBn = template.second,
                    onScreenCaptionBn = template.third,
                    durationSeconds = sceneDuration,
                    drawableResId = drawableRes,
                    transition = VideoTransition.FADE
                )
            )
        }

        val fullScript = generatedScenes.joinToString("\n\n") { "${it.order}. [${it.titleBn}]\n${it.narrationScriptBn}" }

        return VideoProject(
            id = UUID.randomUUID().toString(),
            titleBn = "$activeBusiness - $activeTopic",
            topicBn = activeTopic,
            businessBn = activeBusiness,
            purposeBn = purpose,
            styleBn = style,
            targetDurationSeconds = effectiveDuration,
            aspectRatio = aspectRatio,
            voiceStyle = voiceStyle,
            musicTrack = musicTrack,
            scenes = generatedScenes,
            isAiGeneratedVisuals = true,
            fullScriptBn = fullScript
        )
    }
}
