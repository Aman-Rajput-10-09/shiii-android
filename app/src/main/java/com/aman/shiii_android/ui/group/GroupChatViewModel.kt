package com.aman.shiii_android.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.model.GroupMessage
import com.aman.shiii_android.domain.repository.ShiiiRepository
import com.aman.shiii_android.notification.AppScreen
import com.aman.shiii_android.notification.ScreenStateTracker
import com.aman.shiii_android.player.VoicePlaybackManager
import com.aman.shiii_android.ui.character.CharacterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val repository: ShiiiRepository,
    private val voiceManager: VoicePlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChatUiState())
    val uiState: StateFlow<GroupChatUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

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

    fun initUser(user: AuthUser) {
        _uiState.update {
            it.copy(
                user = user,
                partnerName = user.partnerName ?: "Partner"
            )
        }
        loadInitialMessages(user)
        startRealtimePolling(user)
    }

    fun onScreenResumed() {
        ScreenStateTracker.updateScreen(AppScreen.GROUP_CHAT)
    }

    fun onScreenPaused() {
        ScreenStateTracker.updateScreen(AppScreen.BACKGROUND)
        voiceManager.stop()
    }

    private fun loadInitialMessages(user: AuthUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getGroupMessages(user.token).fold(
                onSuccess = { msgs: List<GroupMessage> ->
                    _uiState.update { it.copy(messages = msgs, isLoading = false) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
            )
        }
    }

    private fun startRealtimePolling(user: AuthUser) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val currentMsgs = _uiState.value.messages
                val maxId = currentMsgs.mapNotNull { it.numericId }.maxOrNull()
                repository.getGroupMessages(user.token, afterId = maxId).onSuccess { newMsgs: List<GroupMessage> ->
                    if (newMsgs.isNotEmpty()) {
                        val existingIds = _uiState.value.messages.mapNotNull { it.numericId }.toSet()
                        val trulyNew = newMsgs.filter { it.numericId == null || !existingIds.contains(it.numericId) }
                        if (trulyNew.isNotEmpty()) {
                            _uiState.update { state ->
                                state.copy(messages = state.messages + trulyNew)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val user = state.user ?: return
        val text = state.inputText.trim()
        if (text.isBlank() || state.isSending) return

        val localId = "local_${System.currentTimeMillis()}"
        val userMsg = GroupMessage(
            id = localId,
            numericId = null,
            coupleId = user.coupleId ?: 0,
            senderId = user.id,
            senderRole = user.role.value,
            senderName = user.displayName,
            content = text
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                inputText = "",
                isSending = true
            )
        }

        viewModelScope.launch {
            repository.sendGroupMessage(user.token, text).fold(
                onSuccess = { returnedList: List<GroupMessage> ->
                    val existing = _uiState.value.messages.filter { it.id != localId }
                    val existingIds = existing.mapNotNull { it.numericId }.toSet()
                    val toAdd = returnedList.filter { it.numericId == null || !existingIds.contains(it.numericId) }
                    _uiState.update {
                        it.copy(
                            messages = existing + toAdd,
                            isSending = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isSending = false, error = err.localizedMessage) }
                }
            )
        }
    }

    fun playMessage(msg: GroupMessage) {
        // Voice support removed from 3-way lounge as requested
    }

    fun stopSpeech() {
        // Voice support removed from 3-way lounge
    }

    fun toggleSpeech() {
        // Voice support removed from 3-way lounge
    }

    fun refresh() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result: Result<List<GroupMessage>> = repository.getGroupMessages(user.token)
            result.fold(
                onSuccess = { msgs: List<GroupMessage> ->
                    _uiState.update { it.copy(messages = msgs, isRefreshing = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            )
        }
    }

    fun clearMyMessages(scope: String) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            if (scope in listOf("group", "all")) {
                _uiState.update { it.copy(messages = it.messages.filter { m -> m.senderId != user.id && m.senderRole != user.role.value }) }
            }
            repository.clearMessages(user.token, scope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
        voiceManager.stop()
    }
}
