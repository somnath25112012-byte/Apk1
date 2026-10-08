package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.VoiceStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

interface VoiceService {
    val isRealApiConnected: Boolean
    val providerName: String
    val isSpeakingFlow: StateFlow<Boolean>

    fun previewVoice(text: String, voiceStyle: VoiceStyle, onComplete: () -> Unit = {})
    fun stopVoice()
    fun release()
}

class DefaultVoiceService(private val context: Context) : VoiceService {
    override val isRealApiConnected: Boolean = false
    override val providerName: String = "Android Native TTS (বাংলা ভয়েস ইঞ্জিন)"

    private val _isSpeakingFlow = MutableStateFlow(false)
    override val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeBnIn = Locale("bn", "IN")
                val result = textToSpeech?.setLanguage(localeBnIn)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default locale if Bengali is not downloaded yet
                    textToSpeech?.language = Locale.getDefault()
                }
                isInitialized = true
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeakingFlow.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeakingFlow.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeakingFlow.value = false
            }
        })
    }

    override fun previewVoice(text: String, voiceStyle: VoiceStyle, onComplete: () -> Unit) {
        if (!isInitialized || textToSpeech == null) {
            onComplete()
            return
        }

        // Adjust pitch and speech rate based on voice style
        val (pitch, rate) = when (voiceStyle) {
            VoiceStyle.BENGALI_FEMALE_NATURAL -> Pair(1.2f, 1.0f)
            VoiceStyle.BENGALI_FEMALE_PROFESSIONAL -> Pair(1.15f, 0.95f)
            VoiceStyle.BENGALI_FEMALE_EMOTIONAL -> Pair(1.25f, 0.90f)
            VoiceStyle.BENGALI_FEMALE_PROMOTIONAL -> Pair(1.3f, 1.15f)
            VoiceStyle.BENGALI_MALE_NATURAL -> Pair(0.85f, 1.0f)
            VoiceStyle.BENGALI_MALE_PROFESSIONAL -> Pair(0.8f, 0.95f)
            VoiceStyle.BENGALI_MALE_EMOTIONAL -> Pair(0.75f, 0.90f)
            VoiceStyle.BENGALI_MALE_PROMOTIONAL -> Pair(0.9f, 1.15f)
        }

        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(rate)

        val utteranceId = "VoicePreview_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stopVoice() {
        textToSpeech?.stop()
        _isSpeakingFlow.value = false
    }

    override fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isSpeakingFlow.value = false
    }
}
