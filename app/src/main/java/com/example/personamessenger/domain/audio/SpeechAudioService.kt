package com.example.personamessenger.domain.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechAudioService(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSpokenText = MutableStateFlow("")
    val currentSpokenText = _currentSpokenText.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isPlaying.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isPlaying.value = false
                _currentSpokenText.value = ""
            }

            override fun onError(utteranceId: String?) {
                _isPlaying.value = false
                _currentSpokenText.value = ""
            }
        })
    }

    fun speak(text: String, isHinglish: Boolean = false) {
        if (text.isBlank()) return
        _currentSpokenText.value = text
        _isPlaying.value = true

        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "persona_msg_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _currentSpokenText.value = ""
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
