package com.aman.shiii_android.ui.direct

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.DirectMessage

data class DirectChatUiState(
    val messages: List<DirectMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val user: AuthUser? = null,
    val partnerName: String = "Partner",
    val error: String? = null
)
