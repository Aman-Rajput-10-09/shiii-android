package com.aman.shiii_android.ui.group

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.model.GroupMessage
import com.aman.shiii_android.ui.character.CharacterState

data class GroupChatUiState(
    val user: AuthUser? = null,
    val partnerName: String = "Partner",
    val messages: List<GroupMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val characterState: CharacterState = CharacterState.IDLE,
    val mouthShape: String = "closed",
    val pendingSpeech: ChatMessage? = null,
    val isSpeechActive: Boolean = false,
    val playingAudioUrl: String? = null
)
