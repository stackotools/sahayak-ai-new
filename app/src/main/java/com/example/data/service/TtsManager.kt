package com.example.data.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Hindi first, fallback to English
                val hindiLocale = Locale("hi", "IN")
                val langResult = textToSpeech?.setLanguage(hindiLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.language = Locale.ENGLISH
                }
                textToSpeech?.setSpeechRate(0.92f) // slightly slower for rural clarity
                _isInitialized.value = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            } else {
                Log.e("TtsManager", "TTS Initialization failed with status: $status")
            }
        }
    }

    fun speak(text: String, languageCode: String = "hi") {
        if (!_isInitialized.value || textToSpeech == null) return
        stop()

        if (languageCode == "hi") {
            val hindi = Locale("hi", "IN")
            val avail = textToSpeech?.isLanguageAvailable(hindi) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (avail >= TextToSpeech.LANG_AVAILABLE) {
                textToSpeech?.language = hindi
            }
        } else {
            textToSpeech?.language = Locale.ENGLISH
        }

        // Clean markdown characters before reading aloud
        val cleanText = text
            .replace(Regex("[*#_`>]"), "")
            .replace(Regex("₹"), "Rupees ")
            .trim()

        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "SahayakTTS_${System.currentTimeMillis()}")
    }

    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
