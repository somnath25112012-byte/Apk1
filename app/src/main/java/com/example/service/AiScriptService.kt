package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.model.AspectRatioType
import com.example.model.BrandProfile
import com.example.model.MusicTrack
import com.example.model.ScenePlan
import com.example.model.VideoProject
import com.example.model.VideoTransition
import com.example.model.VisualSourceMode
import com.example.model.VoiceEmotionPreset
import com.example.model.VoiceStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

interface AiScriptProvider {
    val providerId: String
    val providerNameBn: String
    val isConfigured: Boolean
    val requiredSecretKey: String

    suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        voicePreset: VoiceEmotionPreset,
        musicTrack: MusicTrack,
        aspectRatio: AspectRatioType,
        extraInstructions: String,
        brandProfile: BrandProfile
    ): Result<VideoProject>
}

interface AiScriptService {
    val isRealApiConnected: Boolean
    val providerName: String
    val activeProviderId: String

    suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        voicePreset: VoiceEmotionPreset = VoiceEmotionPreset.NATURAL_WARM,
        musicTrack: MusicTrack = MusicTrack.BENGALI_FLUTE,
        aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
        extraInstructions: String = "",
        brandProfile: BrandProfile
    ): VideoProject
}

class GeminiAiScriptProvider : AiScriptProvider {
    override val providerId: String = "gemini_script"
    override val providerNameBn: String = "Gemini 3.5 Flash (অফিসিয়াল AI ইঞ্জিন)"
    override val requiredSecretKey: String = "GEMINI_API_KEY"

    override val isConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return !key.isNullOrBlank() && !key.equals("your_api_key_here", ignoreCase = true)
        }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        voicePreset: VoiceEmotionPreset,
        musicTrack: MusicTrack,
        aspectRatio: AspectRatioType,
        extraInstructions: String,
        brandProfile: BrandProfile
    ): Result<VideoProject> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("AI স্ক্রিপ্ট তৈরির জন্য AI Studio Secrets প্যানেলে '$requiredSecretKey' কনফিগার করুন।")
            )
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val effectiveDuration = durationSeconds.coerceAtLeast(30)
        val numScenes = when {
            effectiveDuration <= 30 -> 4
            effectiveDuration <= 45 -> 5
            effectiveDuration <= 60 -> 6
            else -> 8
        }
        val targetSceneDuration = (effectiveDuration / numScenes).coerceAtLeast(4)

        val activeBusiness = if (business.isNotBlank()) business else brandProfile.businessNameBn
        val activeTopic = if (topic.isNotBlank()) topic else "ঘরোয়া খাঁটি বাঙালি ভোজ"

        val systemPrompt = """
            You are a master Bengali creative copywriter and advertising storyteller for "$activeBusiness" located in "${brandProfile.location}". Tagline: "${brandProfile.taglineBn}".
            Create an authentic, warm, appetizing Bengali video script for promotional short video / Reels (Duration: $effectiveDuration seconds, Exactly $numScenes scenes).
            Tone requirement: ${voicePreset.descriptionBn} (${voiceStyle.titleBn}).
            Language: 100% natural conversational Bengali, emotionally rich, engaging, culturally evocative. NEVER use clumsy robotic literal translations.
            
            Return JSON in this exact structure:
            {
              "projectTitle": "Bengali title",
              "scenes": [
                {
                  "order": 1,
                  "title": "Scene title in Bengali",
                  "narrationScript": "Natural, spoken Bengali voiceover for this scene",
                  "onScreenCaption": "Short catchy on-screen Bengali caption/headline",
                  "durationSeconds": $targetSceneDuration,
                  "visualPromptEn": "Detailed cinematic prompt for food photography in English (e.g., steaming Kolkata mustard hilsa fish on brass platter, warm rustic kitchen)"
                }
              ]
            }
        """.trimIndent()

        val userPrompt = """
            Topic: $activeTopic
            Purpose: $purpose
            Style: $style
            Duration: $effectiveDuration seconds ($numScenes scenes)
            Additional Instructions: $extraInstructions
        """.trimIndent()

        val defaultDrawables = listOf(
            R.drawable.scene_thali_delight,
            R.drawable.scene_kasha_mangsho,
            R.drawable.scene_ilish_paturi,
            R.drawable.scene_rosogolla,
            R.drawable.scene_kitchen_flame
        )

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemPrompt\n\n$userPrompt"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string().orEmpty()
                Log.e("GeminiScript", "API error: ${response.code} $errBody")
                return@withContext Result.failure(Exception("Gemini API এরর: HTTP ${response.code}"))
            }

            val respBody = response.body?.string().orEmpty()
            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCand = candidates?.optJSONObject(0)
            val parts = firstCand?.optJSONObject("content")?.optJSONArray("parts")
            val textContent = parts?.optJSONObject(0)?.optString("text").orEmpty()

            if (textContent.isBlank()) {
                return@withContext Result.failure(Exception("Gemini থেকে কোনো প্রতিক্রিয়া পাওয়া যায়নি।"))
            }

            val dataObj = JSONObject(textContent)
            val jsonScenes = dataObj.optJSONArray("scenes") ?: JSONArray()
            val scenes = mutableListOf<ScenePlan>()

            for (i in 0 until jsonScenes.length()) {
                val sc = jsonScenes.getJSONObject(i)
                val order = sc.optInt("order", i + 1)
                val title = sc.optString("title", "দৃশ্য $order")
                val narration = sc.optString("narrationScript", "")
                val caption = sc.optString("onScreenCaption", title)
                val dur = sc.optInt("durationSeconds", targetSceneDuration).coerceAtLeast(3)
                val visualPrompt = sc.optString("visualPromptEn", "Authentic Bengali cuisine photography")

                scenes.add(
                    ScenePlan(
                        id = UUID.randomUUID().toString(),
                        order = order,
                        titleBn = title,
                        visualDescriptionBn = "AI ভিজ্যুয়াল দৃশ্য: $visualPrompt",
                        narrationScriptBn = narration,
                        onScreenCaptionBn = caption,
                        durationSeconds = dur,
                        drawableResId = defaultDrawables[i % defaultDrawables.size],
                        transition = VideoTransition.FADE,
                        visualPromptEn = visualPrompt
                    )
                )
            }

            if (scenes.isEmpty()) {
                return@withContext Result.failure(Exception("স্ক্রিপ্ট থেকে সিন তৈরি করা সম্ভব হয়নি।"))
            }

            val fullScript = scenes.joinToString("\n\n") { "${it.order}. [${it.titleBn}]\n${it.narrationScriptBn}" }
            val project = VideoProject(
                id = UUID.randomUUID().toString(),
                titleBn = dataObj.optString("projectTitle", "$activeBusiness - $activeTopic"),
                topicBn = activeTopic,
                businessBn = activeBusiness,
                purposeBn = purpose,
                styleBn = style,
                targetDurationSeconds = effectiveDuration,
                aspectRatio = aspectRatio,
                voiceStyle = voiceStyle,
                voicePreset = voicePreset,
                musicTrack = musicTrack,
                scenes = scenes,
                isAiGeneratedVisuals = true,
                fullScriptBn = fullScript
            )

            Result.success(project)
        } catch (e: Exception) {
            Log.e("GeminiScript", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class LocalBengaliScriptProvider : AiScriptProvider {
    override val providerId: String = "local_script"
    override val providerNameBn: String = "স্টুডিও লোকাল বাঙালি স্ক্রিপ্ট ইঞ্জিন (অফলাইন প্রস্তুত)"
    override val requiredSecretKey: String = "নেই (অফলাইন)"
    override val isConfigured: Boolean = true

    override suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        voicePreset: VoiceEmotionPreset,
        musicTrack: MusicTrack,
        aspectRatio: AspectRatioType,
        extraInstructions: String,
        brandProfile: BrandProfile
    ): Result<VideoProject> = withContext(Dispatchers.Default) {
        val effectiveDuration = durationSeconds.coerceAtLeast(30)
        val numScenes = when {
            effectiveDuration <= 30 -> 4
            effectiveDuration <= 45 -> 5
            effectiveDuration <= 60 -> 6
            else -> 8
        }
        val sceneDuration = (effectiveDuration / numScenes).coerceAtLeast(4)

        val activeBusiness = if (business.isNotBlank()) business else brandProfile.businessNameBn
        val activeTopic = if (topic.isNotBlank()) topic else "ঘরোয়া খাঁটি বাঙালি ভোজ"

        val defaultDrawables = listOf(
            R.drawable.scene_thali_delight,
            R.drawable.scene_kasha_mangsho,
            R.drawable.scene_ilish_paturi,
            R.drawable.scene_rosogolla,
            R.drawable.scene_kitchen_flame
        )

        val templatePlans = listOf(
            Triple(
                "ভূমিকা ও আহ্বান",
                "স্বাগত জানাই $activeBusiness-এ! মাটির সোঁদা ঘ্রাণ আর জিভে জল আনা বাঙালি স্বাদের আনন্দময় মিলনমেলা।",
                "খাঁটি বাঙালিয়ানার সেরা স্বাদের ঠিকানা!"
            ),
            Triple(
                "বিশেষত্বের ছোঁয়া",
                "আমাদের প্রতিটি পদে রয়েছে মায়ের হাতের রান্নার স্নেহমাখা যত্ন আর খাঁটি দেশি মশলার অতুলনীয় গন্ধ।",
                "টাটকা উপকরণ ও খাঁটি মশলায় যত্নশীল রান্না"
            ),
            Triple(
                "প্রধান সুস্বাদু আকর্ষণ",
                "সর্ষে ইলিশের ঝাঁজ থেকে রাজকীয় কষা মাংস—ভোজনরসিকের পরম তৃপ্তির সেরা আয়োজন।",
                "জিভে জল আনা বাঙালি রাজকীয় পদ সম্ভার"
            ),
            Triple(
                "শেষ পাতে মিষ্টি মুখ",
                "খাওয়ার শেষে নরম তুলতুলে খাঁটি ছানার রসগোল্লা আর নলেন গুড়ের মিষ্টিতে মধুর সমাপ্তি।",
                "শেষ পাতে অতুলনীয় মিষ্টি মুখ ও পরম তৃপ্তি"
            ),
            Triple(
                "আতিথেয়তা ও পরিবেশন",
                "পরিবার কিংবা বন্ধুদের নিয়ে আসুন আমাদের পরিপাটি ও আরামদায়ক রেস্তোরাঁয়।",
                "স্নেহমাখা আন্তরিক বাঙালি আতিথেয়তা"
            ),
            Triple(
                "উৎসব ও বিশেষ অফার",
                "আজকের বিশেষ আয়োজনে রয়েছে দুর্দান্ত অফার ও দ্রুত হোম ডেলিভারি সুবিধা!",
                "বিশেষ পারিবারিক অফার ও ডেলিভারি সুবিধা"
            ),
            Triple(
                "যোগাযোগ ও অর্ডার",
                "${brandProfile.location}-এ আমরা সর্বদা আপনার সেবায় প্রস্তুত। এখনই যোগাযোগ করুন: ${brandProfile.phone}।",
                "যোগাযোগ ও হোম ডেলিভারি: ${brandProfile.phone}"
            ),
            Triple(
                "আন্তরিক সমাপ্তি",
                "আজই চলে আসুন $activeBusiness-এ। ${brandProfile.taglineBn}",
                "${brandProfile.taglineBn}"
            )
        )

        val generatedScenes = mutableListOf<ScenePlan>()
        for (i in 0 until numScenes) {
            val template = templatePlans[i % templatePlans.size]
            val drawableRes = defaultDrawables[i % defaultDrawables.size]
            generatedScenes.add(
                ScenePlan(
                    id = UUID.randomUUID().toString(),
                    order = i + 1,
                    titleBn = template.first,
                    visualDescriptionBn = "মনোরম সিনেমাটিক বাঙালি দৃশ্য: $activeTopic",
                    narrationScriptBn = template.second,
                    onScreenCaptionBn = template.third,
                    durationSeconds = sceneDuration,
                    drawableResId = drawableRes,
                    transition = VideoTransition.FADE,
                    visualPromptEn = "Authentic Bengali cuisine scene, traditional culinary presentation"
                )
            )
        }

        val fullScript = generatedScenes.joinToString("\n\n") { "${it.order}. [${it.titleBn}]\n${it.narrationScriptBn}" }

        val project = VideoProject(
            id = UUID.randomUUID().toString(),
            titleBn = "$activeBusiness - $activeTopic",
            topicBn = activeTopic,
            businessBn = activeBusiness,
            purposeBn = purpose,
            styleBn = style,
            targetDurationSeconds = effectiveDuration,
            aspectRatio = aspectRatio,
            voiceStyle = voiceStyle,
            voicePreset = voicePreset,
            musicTrack = musicTrack,
            scenes = generatedScenes,
            isAiGeneratedVisuals = true,
            fullScriptBn = fullScript
        )

        Result.success(project)
    }
}

class DefaultAiScriptService : AiScriptService {
    val geminiProvider = GeminiAiScriptProvider()
    val localProvider = LocalBengaliScriptProvider()

    override val isRealApiConnected: Boolean
        get() = geminiProvider.isConfigured

    override val activeProviderId: String
        get() = if (geminiProvider.isConfigured) geminiProvider.providerId else localProvider.providerId

    override val providerName: String
        get() = if (geminiProvider.isConfigured) geminiProvider.providerNameBn else localProvider.providerNameBn

    override suspend fun generateScriptAndScenes(
        topic: String,
        business: String,
        purpose: String,
        style: String,
        durationSeconds: Int,
        voiceStyle: VoiceStyle,
        voicePreset: VoiceEmotionPreset,
        musicTrack: MusicTrack,
        aspectRatio: AspectRatioType,
        extraInstructions: String,
        brandProfile: BrandProfile
    ): VideoProject {
        if (geminiProvider.isConfigured) {
            val result = geminiProvider.generateScriptAndScenes(
                topic, business, purpose, style, durationSeconds,
                voiceStyle, voicePreset, musicTrack, aspectRatio, extraInstructions, brandProfile
            )
            result.getOrNull()?.let { return it }
        }

        // Graceful fallback to rich local Bengali template generator
        return localProvider.generateScriptAndScenes(
            topic, business, purpose, style, durationSeconds,
            voiceStyle, voicePreset, musicTrack, aspectRatio, extraInstructions, brandProfile
        ).getOrThrow()
    }
}
