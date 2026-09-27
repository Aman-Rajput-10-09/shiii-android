package com.aman.shiii_android.ui.direct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.DirectMessage
import com.aman.shiii_android.domain.repository.ShiiiRepository
import com.aman.shiii_android.notification.AppScreen
import com.aman.shiii_android.notification.ScreenStateTracker
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
class DirectChatViewModel @Inject constructor(
    private val repository: ShiiiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DirectChatUiState())
    val uiState: StateFlow<DirectChatUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    fun initUser(user: AuthUser) {
        ScreenStateTracker.updateScreen(AppScreen.DIRECT_CHAT)
        val partner = user.partnerName ?: if (user.role.value == "master") "Mistress 💕" else "Master 🎩"
        _uiState.update {
            it.copy(
                user = user,
                partnerName = partner
            )
        }
        loadInitialMessages(user)
        startRealtimePolling(user)
    }

    fun onScreenResumed() {
        ScreenStateTracker.updateScreen(AppScreen.DIRECT_CHAT)
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            repository.markDirectMessagesRead(user.token)
        }
    }

    fun onScreenPaused() {
        ScreenStateTracker.updateScreen(AppScreen.BACKGROUND)
    }

    private fun loadInitialMessages(user: AuthUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result: Result<List<DirectMessage>> = repository.getDirectMessages(user.token, markRead = true)
            result.fold(
                onSuccess = { msgs: List<DirectMessage> ->
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
            var cycleCount = 0
            while (isActive) {
                delay(600) // Ultra-fast 600ms responsive polling for real-time instant chatting vibe
                cycleCount++

                val currentMsgs = _uiState.value.messages
                val maxId = currentMsgs.mapNotNull { it.numericId }.maxOrNull()
                
                // Fetch newly arrived messages and automatically mark them read
                val pollResult: Result<List<DirectMessage>> = repository.getDirectMessages(user.token, afterId = maxId, markRead = true)
                pollResult.onSuccess { newMsgs: List<DirectMessage> ->
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

                // Check read receipt status updates every 4th cycle (~2.4s) to avoid network queueing
                if (cycleCount % 4 == 0) {
                    val hasUnreadSentMsgs = _uiState.value.messages.any { it.senderId == user.id && !it.isRead }
                    if (hasUnreadSentMsgs) {
                        val statusCheck: Result<List<DirectMessage>> = repository.getDirectMessages(user.token, markRead = false)
                        statusCheck.onSuccess { refreshed: List<DirectMessage> ->
                            val readMap = refreshed.associate { it.id to it.isRead }
                            _uiState.update { state ->
                                state.copy(
                                    messages = state.messages.map { msg ->
                                        val serverIsRead = readMap[msg.id] ?: msg.isRead
                                        if (serverIsRead != msg.isRead) msg.copy(isRead = serverIsRead) else msg
                                    }
                                )
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
        if (text.isBlank()) return

        val localId = "local_${System.currentTimeMillis()}_${(1..999).random()}"
        val userMsg = DirectMessage(
            id = localId,
            numericId = null,
            coupleId = user.coupleId ?: 0,
            senderId = user.id,
            receiverId = 0,
            senderRole = user.role.value,
            content = text,
            isRead = false
        )

        // Instant 0ms optimistic UI rendering: message appears in the chat immediately!
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                inputText = ""
            )
        }

        viewModelScope.launch {
            val result: Result<DirectMessage> = repository.sendDirectMessage(user.token, text)
            result.fold(
                onSuccess = { serverMsg: DirectMessage ->
                    val updated = _uiState.value.messages.map {
                        if (it.id == localId) serverMsg else it
                    }
                    _uiState.update {
                        it.copy(messages = updated)
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.localizedMessage) }
                }
            )
        }
    }

    fun refresh() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result: Result<List<DirectMessage>> = repository.getDirectMessages(user.token, markRead = true)
            result.fold(
                onSuccess = { msgs: List<DirectMessage> ->
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
            if (scope in listOf("direct", "all")) {
                _uiState.update { it.copy(messages = it.messages.filter { m -> m.senderId != user.id && m.senderRole != user.role.value }) }
            }
            repository.clearMessages(user.token, scope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
