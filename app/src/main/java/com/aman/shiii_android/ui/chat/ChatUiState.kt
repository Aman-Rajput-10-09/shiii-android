package com.aman.shiii_android.ui.chat

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.ui.character.CharacterState

data class ChatUiState(
    val user: AuthUser? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val characterState: CharacterState = CharacterState.IDLE,
    val mouthShape: String = "closed",
    val pendingSpeech: ChatMessage? = null,
    val isSpeechActive: Boolean = false,
    val playingAudioUrl: String? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
