package com.aman.shiii_android.player

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.aman.shiii_android.domain.model.VisemeFrame
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoicePlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollJob: Job? = null

    private val _currentMouthState = MutableStateFlow("closed")
    val currentMouthState: StateFlow<String> = _currentMouthState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingUrl = MutableStateFlow<String?>(null)
    val currentPlayingUrl: StateFlow<String?> = _currentPlayingUrl.asStateFlow()

    init {
        // Initialize native Android TextToSpeech tuned like an adorable Anime Girl!
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                applyAnimeVoiceTuning(isHindi = false)
                isTtsReady = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isPlaying.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentPlayingUrl.value = null
                        _currentMouthState.value = "closed"
                        pollJob?.cancel()
                    }

                    override fun onError(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentPlayingUrl.value = null
                        _currentMouthState.value = "closed"
                        pollJob?.cancel()
                    }
                })
            } else {
                Log.w("VoicePlaybackManager", "TextToSpeech init failed with status: $status")
            }
        }
    }

    /**
     * Finds and applies the sweetest high-pitched Anime Girl voice available on the device
     */
    private fun applyAnimeVoiceTuning(isHindi: Boolean) {
        val t = tts ?: return
        try {
            val targetLang = if (isHindi) "hi" else "en"
            val availableVoices = t.voices?.filter { it.locale.language == targetLang } ?: emptyList()
            
            val animeVoice = availableVoices.firstOrNull { v ->
                val name = v.name.lowercase()
                !v.isNetworkConnectionRequired && (
                    name.contains("female") ||
                    name.contains("sfg") ||
                    name.contains("iol") ||
                    name.contains("tpf")
                )
            } ?: availableVoices.firstOrNull { v ->
                val name = v.name.lowercase()
                name.contains("female") || name.contains("en-us-x")
            } ?: availableVoices.firstOrNull()

            if (animeVoice != null) {
                t.voice = animeVoice
                Log.i("VoicePlaybackManager", "Selected anime voice: ${animeVoice.name}")
            }
        } catch (e: Exception) {
            Log.w("VoicePlaybackManager", "Voice selection exception: ${e.message}")
        }

        // High, lively anime girl pitch & cute tempo
        t.setPitch(1.62f)
        t.setSpeechRate(1.08f)
    }

    /**
     * Speaks using Android native TextToSpeech engine with sweet Anime Girl voice tuning
     */
    fun speakWithTts(text: String) {
        val cleanText = text
            .replace(Regex("\\*[^*]+\\*"), "") // Remove stage directions like *hugs you*
            .replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "") // Clean up emojis
            .trim()
        if (cleanText.isBlank()) return

        stop()
        _isPlaying.value = true
        _currentPlayingUrl.value = "tts://$cleanText"

        val isHindi = cleanText.any { it in '\u0900'..'\u097F' }
        if (isHindi) {
            tts?.language = Locale("hi", "IN")
            applyAnimeVoiceTuning(isHindi = true)
        } else {
            tts?.language = Locale.US
            applyAnimeVoiceTuning(isHindi = false)
        }

        val utteranceId = "shiii_${System.currentTimeMillis()}"
        tts?.setPitch(1.62f)
        tts?.setSpeechRate(1.08f)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        // Animate character mouth shapes while speaking
        pollJob?.cancel()
        pollJob = scope.launch {
            val mouthShapes = listOf("A", "I", "U", "E", "O", "closed")
            var idx = 0
            while (isActive && _isPlaying.value) {
                _currentMouthState.value = mouthShapes[idx % mouthShapes.size]
                idx++
                delay(120)
            }
            _currentMouthState.value = "closed"
        }
    }

    /**
     * Plays voice using native Android TTS (configured like an Anime Girl).
     */
    fun playVoice(text: String, audioUrl: String? = null, visemes: List<VisemeFrame> = emptyList()) {
        if (text.isNotBlank()) {
            speakWithTts(text)
        }
    }

    fun playVoice(audioUrl: String, visemes: List<VisemeFrame>) {
        // No-op without text when using pure Android TTS
    }

    fun togglePlay(text: String, audioUrl: String? = null, visemes: List<VisemeFrame> = emptyList()) {
        val cleanText = text
            .replace(Regex("\\*[^*]+\\*"), "")
            .replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "")
            .trim()
        val isSameTts = _currentPlayingUrl.value == "tts://$cleanText"
        if (_isPlaying.value && isSameTts) {
            stop()
        } else {
            playVoice(text, audioUrl, visemes)
        }
    }

    fun togglePlay(audioUrl: String, visemes: List<VisemeFrame>) {
        if (_isPlaying.value) {
            stop()
        }
    }

    fun stop() {
        pollJob?.cancel()
        tts?.stop()
        _currentMouthState.value = "closed"
        _isPlaying.value = false
        _currentPlayingUrl.value = null
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
