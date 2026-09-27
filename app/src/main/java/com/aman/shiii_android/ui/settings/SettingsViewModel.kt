package com.aman.shiii_android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.domain.repository.ShiiiRepository
import com.aman.shiii_android.notification.ShiiiNotificationHelper
import com.aman.shiii_android.player.VoicePlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val voiceManager: VoicePlaybackManager,
    private val repository: ShiiiRepository,
    val notificationHelper: ShiiiNotificationHelper
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun testAnimeVoice() {
        voiceManager.speakWithTts("Master! Mistress! Shiii's anime voice is ready and working smoothly! 💕")
    }

    fun sendTestNotification() {
        notificationHelper.showPrivateChatNotification(
            sender = "Shiii 🌸",
            message = "Yay! Notifications are working perfectly! Shiii will keep you updated 💕"
        )
        _statusMessage.value = if (notificationHelper.hasPermission()) {
            "Test notification sent! Check your notification drawer ✨"
        } else {
            "Notification permission not granted. Please allow notifications in App Settings."
        }
    }

    fun clearHistory(token: String, scope: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.clearMessages(token, scope)
                _statusMessage.value = when (scope) {
                    "shiii" -> "Shiii AI messages deleted."
                    "direct" -> "1-on-1 Direct messages deleted."
                    "group" -> "3-Way Lounge messages deleted."
                    else -> "All your messages deleted across all rooms."
                }
                onDone()
            } catch (e: Exception) {
                _statusMessage.value = "Failed to clear: ${e.message}"
            }
        }
    }

    fun dismissStatus() {
        _statusMessage.value = null
    }
}
