package com.aman.shiii_android.ui.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.model.MasterBriefing
import com.aman.shiii_android.domain.repository.ShiiiRepository
import com.aman.shiii_android.player.VoicePlaybackManager
import com.aman.shiii_android.ui.character.CharacterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MasterViewModel @Inject constructor(
    private val repository: ShiiiRepository,
    private val voiceManager: VoicePlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasterUiState())
    val uiState: StateFlow<MasterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            voiceManager.currentMouthState.collect { mouth ->
                _uiState.update { it.copy(mouthShape = mouth) }
            }
        }
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
        voiceManager.stop()
        _uiState.update {
            it.copy(
                user = user,
                pendingSpeech = null
            )
        }
        loadBriefings()
        loadChatMessages(user)
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

    private fun loadChatMessages(user: AuthUser) {
        viewModelScope.launch {
            repository.getChatMessages(user.token).fold(
                onSuccess = { msgs ->
                    if (msgs.isNotEmpty()) {
                        val (merged, _) = mergeMessages(_uiState.value.chatMessages, msgs)
                        _uiState.update { it.copy(chatMessages = merged) }
                    }
                },
                onFailure = { /* silent */ }
            )
        }
    }

    private fun startRealtimeSync(token: String) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(3500)
                pollBriefingsAndMessages(token)
            }
        }
    }

    private suspend fun pollBriefingsAndMessages(token: String) {
        // Poll briefings
        repository.getMasterBriefings(token).fold(
            onSuccess = { list ->
                val prevBriefings = _uiState.value.briefings
                if (list != prevBriefings) {
                    _uiState.update { state ->
                        state.copy(briefings = list)
                    }
                }
            },
            onFailure = { /* silent */ }
        )

        // Poll chat messages
        val currentMsgs = _uiState.value.chatMessages
        val maxNumericId = currentMsgs.mapNotNull { it.numericId }.maxOrNull()
        repository.getChatMessages(token, afterId = maxNumericId).fold(
            onSuccess = { newMsgs ->
                if (newMsgs.isNotEmpty()) {
                    val (merged, newlyAdded) = mergeMessages(_uiState.value.chatMessages, newMsgs)
                    if (merged != _uiState.value.chatMessages) {
                        _uiState.update { state ->
                            state.copy(
                                chatMessages = merged,
                                pendingSpeech = newlyAdded.lastOrNull { it.senderRole == "shiii" } ?: state.pendingSpeech
                            )
                        }
                    }
                }
            },
            onFailure = { /* silent */ }
        )
    }

    fun loadBriefings() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getMasterBriefings(user.token).fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(
                            briefings = list,
                            isLoading = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }

    fun refresh() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            // Refresh briefings
            val briefingResult = repository.getMasterBriefings(user.token)
            briefingResult.fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(briefings = list) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.localizedMessage) }
                }
            )
            // Refresh chat messages
            val chatResult = repository.getChatMessages(user.token)
            chatResult.fold(
                onSuccess = { msgs ->
                    if (msgs.isNotEmpty()) {
                        val (merged, _) = mergeMessages(_uiState.value.chatMessages, msgs)
                        _uiState.update { it.copy(chatMessages = merged) }
                    }
                },
                onFailure = { /* silent */ }
            )
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }


    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onChatInputChanged(text: String) {
        _uiState.update { it.copy(chatInputText = text) }
    }

    fun sendChatMessage() {
        val state = _uiState.value
        val user = state.user ?: return
        val messageText = state.chatInputText.trim()
        if (messageText.isBlank() || state.isSendingChat) return

        val localId = "local_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}"
        val userMsg = ChatMessage(
            id = localId,
            numericId = null,
            senderRole = "master",
            content = messageText
        )

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                chatInputText = "",
                isSendingChat = true
            )
        }

        viewModelScope.launch {
            isContinuousChat = true
            repository.sendChatMessage(user.token, messageText).fold(
                onSuccess = { replyMsg ->
                    voiceManager.playVoice(replyMsg.content, replyMsg.audioUrl, replyMsg.visemes)
                    _uiState.update { s ->
                        val (merged, _) = mergeMessages(s.chatMessages, listOf(replyMsg))
                        s.copy(
                            chatMessages = merged,
                            isSendingChat = false,
                            characterState = CharacterState.TALKING,
                            pendingSpeech = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isSendingChat = false, error = err.localizedMessage)
                    }
                }
            )
        }
    }

    fun playBriefing(briefing: MasterBriefing) {
        val textToSpeak = briefing.masterBriefing
        val speech = ChatMessage(
            senderRole = "shiii",
            content = textToSpeak,
            audioUrl = briefing.audioUrl
        )
        voiceManager.togglePlay(textToSpeak, briefing.audioUrl, emptyList())
        _uiState.update {
            it.copy(
                pendingSpeech = speech,
                characterState = if (voiceManager.isPlaying.value) CharacterState.TALKING else CharacterState.IDLE
            )
        }
    }

    fun playResolution(resolutionText: String, audioUrl: String? = null) {
        val targetAudio = audioUrl ?: _uiState.value.resolutionAudioUrl
        val speech = ChatMessage(
            senderRole = "shiii",
            content = resolutionText,
            audioUrl = targetAudio
        )
        voiceManager.togglePlay(resolutionText, targetAudio, emptyList())
        _uiState.update {
            it.copy(
                pendingSpeech = speech,
                characterState = if (voiceManager.isPlaying.value) CharacterState.TALKING else CharacterState.IDLE
            )
        }
    }

    fun selectConcern(briefing: MasterBriefing?) {
        _uiState.update { it.copy(selectedConcern = briefing, replyText = "", resolutionResult = null, resolutionAudioUrl = null) }
    }

    fun onReplyTextChange(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun resolveConcern() {
        val state = _uiState.value
        val user = state.user ?: return
        val concern = state.selectedConcern ?: return
        val reply = state.replyText.trim()
        if (reply.isBlank() || state.isResolving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResolving = true, error = null) }
            repository.resolveConcern(user.token, concern.id, reply).fold(
                onSuccess = { message ->
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            resolutionResult = message.content,
                            resolutionAudioUrl = message.audioUrl,
                            replyText = "",
                            pendingSpeech = message
                        )
                    }
                    loadBriefings()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isResolving = false, error = err.localizedMessage)
                    }
                }
            )
        }
    }

    fun playPendingSpeech() {
        val speech = _uiState.value.pendingSpeech ?: return
        isContinuousChat = true
        voiceManager.playVoice(speech.content, speech.audioUrl, speech.visemes)
        _uiState.update { it.copy(pendingSpeech = null) }
    }

    fun playMessage(message: ChatMessage) {
        voiceManager.togglePlay(message.content, message.audioUrl, message.visemes)
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
                _uiState.update { it.copy(chatMessages = it.chatMessages.filter { m -> m.senderRole != "user" && m.senderRole != "master" }) }
            }
            repository.clearMessages(user.token, scope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stop()
    }
}
