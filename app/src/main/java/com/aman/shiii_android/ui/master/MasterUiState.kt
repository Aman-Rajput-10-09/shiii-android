package com.aman.shiii_android.ui.master

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.model.MasterBriefing
import com.aman.shiii_android.ui.character.CharacterState

data class MasterUiState(
    val user: AuthUser? = null,
    val selectedTab: Int = 0, // 0: Diplomatic Briefings, 1: Confide in Shiii
    val briefings: List<MasterBriefing> = emptyList(),
    val isLoading: Boolean = false,
    val selectedConcern: MasterBriefing? = null,
    val replyText: String = "",
    val isResolving: Boolean = false,
    val resolutionResult: String? = null,
    val resolutionAudioUrl: String? = null,
    val characterState: CharacterState = CharacterState.IDLE,
    val mouthShape: String = "closed",
    val pendingSpeech: ChatMessage? = null,
    val isSpeechActive: Boolean = false,
    val playingAudioUrl: String? = null,
    val error: String? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val chatInputText: String = "",
    val isSendingChat: Boolean = false,
    val isRefreshing: Boolean = false
)
