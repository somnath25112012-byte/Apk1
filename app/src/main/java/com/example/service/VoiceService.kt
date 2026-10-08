package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.VoiceEmotionPreset
import com.example.model.VoiceStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

interface TtsProvider {
    val providerId: String
    val providerNameBn: String
    val isConfigured: Boolean
    val requiredSecretKey: String

    suspend fun synthesizeSpeech(
        context: Context,
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset,
        speed: Float,
        pitch: Float
    ): Result<File>

    fun playAudioFile(
        file: File,
        onComplete: () -> Unit = {}
    )

    fun stopAudio()
    fun release()
}

interface VoiceService {
    val activeProviderNameBn: String
    val isRealNeuralApiConnected: Boolean
    val isSpeakingFlow: StateFlow<Boolean>
    val lastSynthesizedFileFlow: StateFlow<File?>
    val isFallbackEnabledFlow: StateFlow<Boolean>

    fun setFallbackEnabled(enabled: Boolean)

    suspend fun synthesizeSceneVoice(
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset = VoiceEmotionPreset.NATURAL_WARM,
        speed: Float = 1.0f,
        pitch: Float = 1.0f
    ): Result<File>

    fun previewVoice(
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset = VoiceEmotionPreset.NATURAL_WARM,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        onStatus: (String) -> Unit = {},
        onComplete: () -> Unit = {}
    )

    fun stopVoice()
    fun release()
}

class GeminiNeuralTtsProvider : TtsProvider {
    private val TAG = "GeminiNeuralTts"

    override val providerId: String = "gemini_neural_tts"
    override val providerNameBn: String = "Gemini 2.5 Flash Neural TTS (বাঙালি নারী স্বাভাবিক কণ্ঠ)"
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

    private var mediaPlayer: MediaPlayer? = null

    override suspend fun synthesizeSpeech(
        context: Context,
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset,
        speed: Float,
        pitch: Float
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("AI নিউরাল ভয়েসের জন্য AI Studio Secrets প্যানেলে '$requiredSecretKey' কনফিগার করুন।")
            )
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val cacheDir = File(context.cacheDir, "tts_cache").apply { mkdirs() }
        val safeTextHash = (text.hashCode().toString() + "_" + voiceStyle.name + "_" + preset.name)
        val outputFile = File(cacheDir, "voice_$safeTextHash.wav")

        if (outputFile.exists() && outputFile.length() > 1024) {
            return@withContext Result.success(outputFile)
        }

        // Voice selection: Aoede (Female Warm & Expressive), Kore (Female Clear), Charon/Fenrir (Male)
        val voiceName = if (voiceStyle.isFemale) "Aoede" else "Charon"

        val emotionPrompt = when (preset) {
            VoiceEmotionPreset.NATURAL_WARM -> "Say warmly, naturally and sincerely in fluent Bengali as a real Bengali woman welcoming guests to a home-cooked food experience:"
            VoiceEmotionPreset.PROMOTIONAL -> "Say energetically, confidently and appetizingly in vibrant commercial Bengali as a professional food promoter:"
            VoiceEmotionPreset.JOYFUL -> "Say joyfully, cheerfully and festively with lively excitement in authentic Bengali:"
            VoiceEmotionPreset.EMOTIONAL -> "Say softly, lovingly and emotionally with maternal warmth in deep Bengali storytelling cadence:"
            VoiceEmotionPreset.STORYTELLING -> "Say gracefully, smoothly and expressively in narrative storytelling Bengali:"
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$emotionPrompt $text"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("AUDIO") })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", voiceName)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string().orEmpty()
                Log.e(TAG, "Gemini TTS API error: ${response.code} $errBody")
                return@withContext Result.failure(Exception("Gemini TTS এরর: HTTP ${response.code}"))
            }

            val respBody = response.body?.string().orEmpty()
            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCand = candidates?.optJSONObject(0)
            val parts = firstCand?.optJSONObject("content")?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val inlineData = firstPart?.optJSONObject("inlineData")
            val base64Data = inlineData?.optString("data").orEmpty()
            val mimeType = inlineData?.optString("mimeType", "audio/wav")

            if (base64Data.isBlank()) {
                return@withContext Result.failure(Exception("Gemini থেকে কোনো অডিও ডেটা পাওয়া যায়নি।"))
            }

            val rawAudioBytes = Base64.decode(base64Data, Base64.DEFAULT)

            // Ensure valid WAV format if PCM or raw audio returned
            val finalBytes = if (mimeType?.contains("pcm", ignoreCase = true) == true || !isWavFile(rawAudioBytes)) {
                createWavWithHeader(rawAudioBytes, 24000, 1)
            } else {
                rawAudioBytes
            }

            FileOutputStream(outputFile).use { fos ->
                fos.write(finalBytes)
                fos.flush()
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during synthesis: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun playAudioFile(file: File, onComplete: () -> Unit) {
        try {
            stopAudio()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    onComplete()
                }
                setOnErrorListener { _, _, _ ->
                    onComplete()
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Audio playback error: ${e.message}", e)
            onComplete()
        }
    }

    override fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (ignored: Exception) {}
    }

    override fun release() {
        stopAudio()
    }

    private fun isWavFile(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        val riff = String(bytes, 0, 4)
        val wave = String(bytes, 8, 4)
        return riff == "RIFF" && wave == "WAVE"
    }

    private fun createWavWithHeader(pcmBytes: ByteArray, sampleRate: Int, channels: Int): ByteArray {
        val totalAudioLen = pcmBytes.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte()
        header[33] = 0
        header[34] = 16 // 16 bits
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        return header + pcmBytes
    }
}

class AndroidLocalTtsFallbackProvider(private val context: Context) : TtsProvider {
    override val providerId: String = "local_android_tts"
    override val providerNameBn: String = "ডিভাইস লোকাল TTS (ঐচ্ছিক অফলাইন ব্যাকআপ)"
    override val requiredSecretKey: String = "নেই (ডিভাইস ইঞ্জিন)"
    override val isConfigured: Boolean = true

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeBnIn = Locale.forLanguageTag("bn-IN")
                textToSpeech?.setLanguage(localeBnIn)
                isInitialized = true
            }
        }
    }

    override suspend fun synthesizeSpeech(
        context: Context,
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset,
        speed: Float,
        pitch: Float
    ): Result<File> = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "tts_cache").apply { mkdirs() }
        val outputFile = File(cacheDir, "local_tts_${System.currentTimeMillis()}.wav")

        if (!isInitialized || textToSpeech == null) {
            return@withContext Result.failure(Exception("লোকাল TTS প্রস্তুত নয়।"))
        }

        try {
            textToSpeech?.setPitch(pitch)
            textToSpeech?.setSpeechRate(speed)
            val utteranceId = "LocalTts_${System.currentTimeMillis()}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech?.synthesizeToFile(text, null, outputFile, utteranceId)
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun playAudioFile(file: File, onComplete: () -> Unit) {
        onComplete()
    }

    fun speakDirect(text: String, voiceStyle: VoiceStyle, speed: Float, pitch: Float, onDone: () -> Unit) {
        if (!isInitialized || textToSpeech == null) {
            onDone()
            return
        }
        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(speed)
        val uid = "Speak_${System.currentTimeMillis()}"
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onDone() }
        })
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, uid)
    }

    override fun stopAudio() {
        textToSpeech?.stop()
    }

    override fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

class DefaultVoiceService(private val context: Context) : VoiceService {
    val neuralTtsProvider = GeminiNeuralTtsProvider()
    val fallbackProvider = AndroidLocalTtsFallbackProvider(context)

    private val _isSpeakingFlow = MutableStateFlow(false)
    override val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    private val _lastSynthesizedFileFlow = MutableStateFlow<File?>(null)
    override val lastSynthesizedFileFlow: StateFlow<File?> = _lastSynthesizedFileFlow.asStateFlow()

    private val _isFallbackEnabledFlow = MutableStateFlow(false)
    override val isFallbackEnabledFlow: StateFlow<Boolean> = _isFallbackEnabledFlow.asStateFlow()

    override val isRealNeuralApiConnected: Boolean
        get() = neuralTtsProvider.isConfigured

    override val activeProviderNameBn: String
        get() = if (neuralTtsProvider.isConfigured) neuralTtsProvider.providerNameBn else fallbackProvider.providerNameBn

    override fun setFallbackEnabled(enabled: Boolean) {
        _isFallbackEnabledFlow.value = enabled
    }

    override suspend fun synthesizeSceneVoice(
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset,
        speed: Float,
        pitch: Float
    ): Result<File> {
        if (neuralTtsProvider.isConfigured) {
            val result = neuralTtsProvider.synthesizeSpeech(context, text, voiceStyle, preset, speed, pitch)
            if (result.isSuccess) {
                _lastSynthesizedFileFlow.value = result.getOrNull()
                return result
            }
        }

        if (_isFallbackEnabledFlow.value) {
            return fallbackProvider.synthesizeSpeech(context, text, voiceStyle, preset, speed, pitch)
        }

        return Result.failure(
            IllegalStateException("AI নিউরাল ভয়েসের জন্য AI Studio Secrets প্যানেলে 'GEMINI_API_KEY' প্রয়োজন।")
        )
    }

    override fun previewVoice(
        text: String,
        voiceStyle: VoiceStyle,
        preset: VoiceEmotionPreset,
        speed: Float,
        pitch: Float,
        onStatus: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        stopVoice()
        _isSpeakingFlow.value = true

        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
            if (neuralTtsProvider.isConfigured) {
                onStatus("Gemini AI নিউরাল ভয়েস জেনারেট হচ্ছে...")
                val result = neuralTtsProvider.synthesizeSpeech(context, text, voiceStyle, preset, speed, pitch)
                val file = result.getOrNull()
                if (file != null && file.exists()) {
                    onStatus("প্লে হচ্ছে (${preset.titleBn})...")
                    neuralTtsProvider.playAudioFile(file) {
                        _isSpeakingFlow.value = false
                        onComplete()
                    }
                    return@launch
                } else {
                    val err = result.exceptionOrNull()?.message ?: "ত্রুটি"
                    onStatus("AI ভয়েস ব্যর্থ: $err")
                }
            } else {
                onStatus("⚠️ GEMINI_API_KEY কনফিগার করা নেই।")
            }

            // Explicit fallback check
            if (_isFallbackEnabledFlow.value) {
                onStatus("ডিভাইস লোকাল ব্যাকআপ TTS প্লে হচ্ছে...")
                fallbackProvider.speakDirect(text, voiceStyle, speed, pitch) {
                    _isSpeakingFlow.value = false
                    onComplete()
                }
            } else {
                _isSpeakingFlow.value = false
                onComplete()
            }
        }
    }

    override fun stopVoice() {
        neuralTtsProvider.stopAudio()
        fallbackProvider.stopAudio()
        _isSpeakingFlow.value = false
    }

    override fun release() {
        stopVoice()
        neuralTtsProvider.release()
        fallbackProvider.release()
    }
}
