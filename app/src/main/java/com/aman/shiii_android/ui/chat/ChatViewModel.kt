package com.aman.shiii_android.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.repository.ShiiiRepository
import com.aman.shiii_android.player.VoicePlaybackManager
import com.aman.shiii_android.ui.character.CharacterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ShiiiRepository,
    private val voiceManager: VoicePlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Observe mouth sync from VoicePlaybackManager
        viewModelScope.launch {
            voiceManager.currentMouthState.collect { mouth ->
                _uiState.update { it.copy(mouthShape = mouth) }
            }
        }
        // Observe playing status
        viewModelScope.launch {
            voiceManager.isPlaying.collect { playing ->
                _uiState.update {
                    it.copy(
                        characterState = if (playing) CharacterState.TALKING else CharacterState.IDLE,
                        isSpeechActive = playing
                    )
                }
            }
        }
        // Observe current playing audio URL
        viewModelScope.launch {
            voiceManager.currentPlayingUrl.collect { url ->
                _uiState.update {
                    it.copy(
                        playingAudioUrl = url,
                        isSpeechActive = (url != null)
                    )
                }
            }
        }
    }

    private var syncJob: kotlinx.coroutines.Job? = null
    private var isContinuousChat = false

    fun initUser(user: AuthUser) {
        isContinuousChat = false
        _uiState.update { it.copy(user = user) }
        loadInitialMessages(user)
        startRealtimeSync(user.token)
    }

    fun onAppBackgrounded() {
        isContinuousChat = false
        voiceManager.stop()
    }

    private fun mergeMessages(current: List<ChatMessage>, incoming: List<ChatMessage>): Pair<List<ChatMessage>, List<ChatMessage>> {
        val updatedList = current.toMutableList()
        val newlyAdded = mutableListOf<ChatMessage>()

        for (newMsg in incoming) {
            val exactIndex = updatedList.indexOfFirst {
                (it.numericId != null && newMsg.numericId != null && it.numericId == newMsg.numericId) ||
                it.id == newMsg.id
            }
            if (exactIndex != -1) {
                updatedList[exactIndex] = newMsg
                continue
            }

            val optimisticIndex = updatedList.indexOfFirst {
                it.numericId == null && it.senderRole == newMsg.senderRole && it.content.trim() == newMsg.content.trim()
            }
            if (optimisticIndex != -1) {
                updatedList[optimisticIndex] = newMsg
                continue
            }

            updatedList.add(newMsg)
            newlyAdded.add(newMsg)
        }

        return Pair(updatedList, newlyAdded)
    }

    private fun loadInitialMessages(user: AuthUser) {
        viewModelScope.launch {
            repository.getChatMessages(user.token).fold(
                onSuccess = { history ->
                    if (history.isNotEmpty()) {
                        val (merged, _) = mergeMessages(_uiState.value.messages, history)
                        _uiState.update { state ->
                            state.copy(
                                messages = merged,
                                pendingSpeech = null
                            )
                        }
                    } else {
                        val welcomeMsg = ChatMessage(
                            senderRole = "shiii",
                            content = "Mistress ${user.displayName}! Shiii is right here with you! Tell Shiii anything on your mind! 💕"
                        )
                        _uiState.update { state ->
                            state.copy(
                                messages = listOf(welcomeMsg),
                                pendingSpeech = null
                            )
                        }
                    }
                },
                onFailure = {
                    val welcomeMsg = ChatMessage(
                        senderRole = "shiii",
                        content = "Mistress ${user.displayName}! Shiii is right here with you! Tell Shiii anything on your mind! 💕"
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = listOf(welcomeMsg),
                            pendingSpeech = null
                        )
                    }
                }
            )
        }
    }

    private fun startRealtimeSync(token: String) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(3000)
                pollNewMessages(token)
            }
        }
    }

    private suspend fun pollNewMessages(token: String) {
        val current = _uiState.value.messages
        val maxNumericId = current.mapNotNull { it.numericId }.maxOrNull()
        repository.getChatMessages(token, afterId = maxNumericId).fold(
            onSuccess = { newMsgs ->
                if (newMsgs.isNotEmpty()) {
                    val (merged, newlyAdded) = mergeMessages(_uiState.value.messages, newMsgs)
                    if (merged != _uiState.value.messages) {
                        val latestShiii = newlyAdded.lastOrNull { it.senderRole == "shiii" }
                        if (latestShiii != null) {
                            val textToSpeak = latestShiii.englishText ?: latestShiii.content
                            if (isContinuousChat) {
                                // In continuous chat, speak automatically
                                voiceManager.playVoice(textToSpeak, latestShiii.audioUrl, latestShiii.visemes)
                                _uiState.update { state ->
                                    state.copy(
                                        messages = merged,
                                        pendingSpeech = latestShiii,
                                        characterState = CharacterState.TALKING
                                    )
                                }
                            } else {
                                // If opened or returned from background, prompt "May I speak?"
                                _uiState.update { state ->
                                    state.copy(
                                        messages = merged,
                                        pendingSpeech = latestShiii,
                                        characterState = if (!voiceManager.isPlaying.value) CharacterState.HAPPY else state.characterState
                                    )
                                }
                            }
                        } else {
                            _uiState.update { state ->
                                state.copy(messages = merged)
                            }
                        }
                    }
                }
            },
            onFailure = { /* Silent polling failure to avoid disrupting UX */ }
        )
    }

    fun refresh() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            repository.getChatMessages(user.token).fold(
                onSuccess = { list ->
                    if (list.isNotEmpty()) {
                        val (merged, _) = mergeMessages(_uiState.value.messages, list)
                        _uiState.update { state ->
                            state.copy(
                                messages = merged,
                                isRefreshing = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isRefreshing = false, error = err.localizedMessage) }
                }
            )
        }
    }


    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun setWalkingAnimation(walking: Boolean) {
        if (!voiceManager.isPlaying.value) {
            _uiState.update {
                it.copy(characterState = if (walking) CharacterState.WALKING else CharacterState.IDLE)
            }
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val user = state.user ?: return
        val text = state.inputText.trim()
        if (text.isBlank() || state.isSending) return

        isContinuousChat = true

        val localId = "local_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}"
        val userMessage = ChatMessage(
            id = localId,
            numericId = null,
            senderRole = user.role.value,
            content = text
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isSending = true,
                characterState = CharacterState.WALKING
            )
        }

        viewModelScope.launch {
            val result = repository.sendChatMessage(user.token, text)
            result.fold(
                onSuccess = { reply ->
                    // Continuous chat: speak automatically in fluent English TTS!
                    val textToSpeak = reply.englishText ?: reply.content
                    voiceManager.playVoice(textToSpeak, reply.audioUrl, reply.visemes)
                    _uiState.update { s ->
                        val (merged, _) = mergeMessages(s.messages, listOf(reply))
                        s.copy(
                            messages = merged,
                            isSending = false,
                            characterState = CharacterState.TALKING,
                            pendingSpeech = reply
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            characterState = CharacterState.IDLE,
                            error = err.localizedMessage
                        )
                    }
                }
            )
        }
    }

    /**
     * Plays the current pending speech when Mistress taps "May I speak?"
     */
    fun playPendingSpeech() {
        val speech = _uiState.value.pendingSpeech ?: return
        isContinuousChat = true
        val textToSpeak = speech.englishText ?: speech.content
        voiceManager.playVoice(textToSpeak, speech.audioUrl, speech.visemes)
    }

    /**
     * Plays or pauses voice for a specific message
     */
    fun playMessage(message: ChatMessage) {
        val textToSpeak = message.englishText ?: message.content
        voiceManager.togglePlay(textToSpeak, message.audioUrl, message.visemes)
        _uiState.update { it.copy(pendingSpeech = message) }
    }

    fun toggleSpeech() {
        if (_uiState.value.isSpeechActive) {
            voiceManager.stop()
        } else {
            playPendingSpeech()
        }
    }

    fun stopSpeech() {
        voiceManager.stop()
    }

    fun dismissPendingSpeech() {
        _uiState.update { it.copy(pendingSpeech = null) }
    }

    fun clearMyMessages(scope: String) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            if (scope in listOf("shiii", "all")) {
                _uiState.update { it.copy(messages = it.messages.filter { m -> m.senderRole != "user" && m.senderRole != "mistress" }) }
            }
            repository.clearMessages(user.token, scope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stop()
    }
}
