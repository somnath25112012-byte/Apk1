package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

interface AiVisualProvider {
    val providerId: String
    val providerNameBn: String
    val isConfigured: Boolean
    val requiredSecretKey: String

    suspend fun generateVisualForScene(
        context: Context,
        prompt: String,
        sceneNumber: Int
    ): Result<File>
}

interface AiVideoProvider {
    val providerId: String
    val providerNameBn: String
    val isConfigured: Boolean
    val requiredSecretKey: String

    suspend fun generateVideoScene(
        context: Context,
        prompt: String,
        durationSeconds: Int
    ): Result<File>
}

interface AiVideoService {
    val isVisualApiConfigured: Boolean
    val isVideoApiConfigured: Boolean
    val visualProviderNameBn: String
    val videoProviderNameBn: String

    suspend fun obtainVisualForScene(
        context: Context,
        prompt: String,
        sceneIndex: Int
    ): Pair<Int?, File?>
}

class GeminiAiVisualProvider : AiVisualProvider {
    private val TAG = "GeminiAiVisual"

    override val providerId: String = "gemini_visual_imagen"
    override val providerNameBn: String = "Gemini 2.5 Flash Image (বাঙালি ফুড ফটোগ্রাফি)"
    override val requiredSecretKey: String = "GEMINI_API_KEY"

    override val isConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return !key.isNullOrBlank() && !key.equals("your_api_key_here", ignoreCase = true)
        }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generateVisualForScene(
        context: Context,
        prompt: String,
        sceneNumber: Int
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("AI ছবি তৈরির জন্য AI Studio Secrets প্যানেলে '$requiredSecretKey' কনফিগার করুন।")
            )
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val outputDir = File(context.cacheDir, "ai_visuals").apply { mkdirs() }
        val outputFile = File(outputDir, "scene_visual_${sceneNumber}_${prompt.hashCode()}.jpg")

        if (outputFile.exists() && outputFile.length() > 5000) {
            return@withContext Result.success(outputFile)
        }

        val enrichedPrompt = "Cinematic vertical 9:16 food photography of authentic Bengali culinary dish: $prompt. Served on traditional brass plate with banana leaf, warm atmospheric restaurant lighting, steam rising, shallow depth of field, appetizing texture, no text overlays, vertical orientation."

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", enrichedPrompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("IMAGE") })
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", "9:16")
                        put("imageSize", "1K")
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Image API error HTTP ${response.code}")
                return@withContext Result.failure(Exception("ইমেজ API এরর: HTTP ${response.code}"))
            }

            val respBody = response.body?.string().orEmpty()
            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCand = candidates?.optJSONObject(0)
            val parts = firstCand?.optJSONObject("content")?.optJSONArray("parts")
            var base64Image = ""

            if (parts != null) {
                for (p in 0 until parts.length()) {
                    val partObj = parts.getJSONObject(p)
                    val inline = partObj.optJSONObject("inlineData")
                    if (inline != null && inline.optString("mimeType").startsWith("image")) {
                        base64Image = inline.optString("data")
                        break
                    }
                }
            }

            if (base64Image.isBlank()) {
                return@withContext Result.failure(Exception("কোনো ইমেজ ডেটা পাওয়া যায়নি।"))
            }

            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            FileOutputStream(outputFile).use { fos ->
                fos.write(imageBytes)
                fos.flush()
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Image generation error: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class VeoAiVideoProvider : AiVideoProvider {
    override val providerId: String = "veo_video"
    override val providerNameBn: String = "Veo 3.1 Fast (অফিসিয়াল AI ভিডিও ইঞ্জিন)"
    override val requiredSecretKey: String = "VEO_API_KEY / GEMINI_API_KEY"

    override val isConfigured: Boolean
        get() {
            // Veo requires specialized video model access and billing
            val key = BuildConfig.GEMINI_API_KEY
            return !key.isNullOrBlank() && !key.equals("your_api_key_here", ignoreCase = true)
        }

    override suspend fun generateVideoScene(
        context: Context,
        prompt: String,
        durationSeconds: Int
    ): Result<File> = withContext(Dispatchers.IO) {
        // As mandated in PART 9:
        // Do NOT invent a fake video API.
        // If not connected / configured, clearly tell user: "AI ভিডিও জেনারেশনের জন্য API সংযোগ প্রয়োজন।"
        Result.failure(
            IllegalStateException("AI ভিডিও জেনারেশনের জন্য API সংযোগ প্রয়োজন। বর্তমানে বাস্তবায়িত মিডিয়া কম্পোজিশন ইঞ্জিন ব্যবহার করে রিয়েল MP4 তৈরি হচ্ছে।")
        )
    }
}

class DefaultAiVideoService : AiVideoService {
    val visualProvider = GeminiAiVisualProvider()
    val videoProvider = VeoAiVideoProvider()

    override val isVisualApiConfigured: Boolean
        get() = visualProvider.isConfigured

    override val isVideoApiConfigured: Boolean
        get() = false // True when Veo model endpoint is provisioned

    override val visualProviderNameBn: String
        get() = if (visualProvider.isConfigured) visualProvider.providerNameBn else "স্টুডিও লোকাল ফুড লাইব্রেরি (অফলাইন)"

    override val videoProviderNameBn: String
        get() = videoProvider.providerNameBn

    private val fallbackDrawables = listOf(
        R.drawable.scene_thali_delight,
        R.drawable.scene_kasha_mangsho,
        R.drawable.scene_ilish_paturi,
        R.drawable.scene_rosogolla,
        R.drawable.scene_kitchen_flame
    )

    override suspend fun obtainVisualForScene(
        context: Context,
        prompt: String,
        sceneIndex: Int
    ): Pair<Int?, File?> {
        if (visualProvider.isConfigured && prompt.isNotBlank()) {
            val genResult = visualProvider.generateVisualForScene(context, prompt, sceneIndex)
            val file = genResult.getOrNull()
            if (file != null && file.exists()) {
                return Pair(null, file)
            }
        }

        // Curated authentic Bengali food fallback
        val resId = fallbackDrawables[sceneIndex % fallbackDrawables.size]
        return Pair(resId, null)
    }
}
