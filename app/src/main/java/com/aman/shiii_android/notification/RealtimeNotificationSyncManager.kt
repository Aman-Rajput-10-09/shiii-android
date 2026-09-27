package com.aman.shiii_android.notification

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.DirectMessage
import com.aman.shiii_android.domain.model.GroupMessage
import com.aman.shiii_android.domain.repository.ShiiiRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeNotificationSyncManager @Inject constructor(
    private val repository: ShiiiRepository,
    private val notificationHelper: ShiiiNotificationHelper
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var lastSeenPrivateId: Int? = null
    private var lastSeenGroupId: Int? = null
    private var lastSeenDirectId: Int? = null
    private var isFirstSync = true

    fun startSync(user: AuthUser) {
        syncJob?.cancel()
        isFirstSync = true
        lastSeenPrivateId = null
        lastSeenGroupId = null
        lastSeenDirectId = null

        syncJob = scope.launch {
            while (isActive) {
                try {
                    pollPrivateMessages(user)
                    if (user.coupleId != null) {
                        pollGroupMessages(user)
                        pollDirectMessages(user)
                    }
                    isFirstSync = false
                } catch (e: Exception) {
                    // silent fail for transient network glitches
                }
                delay(3000) // Real-time polling every 3.0 seconds
            }
        }
    }

    private suspend fun pollPrivateMessages(user: AuthUser) {
        val result = repository.getChatMessages(user.token, afterId = lastSeenPrivateId)
        result.onSuccess { messages ->
            if (messages.isNotEmpty()) {
                val maxId = messages.mapNotNull { it.numericId }.maxOrNull()
                if (maxId != null) {
                    lastSeenPrivateId = maxId
                }

                if (!isFirstSync) {
                    val activeScreen = ScreenStateTracker.currentScreen.value
                    if (activeScreen != AppScreen.PRIVATE_CHAT) {
                        val shiiiMsg = messages.lastOrNull { it.senderRole == "shiii" }
                        if (shiiiMsg != null) {
                            notificationHelper.showPrivateChatNotification("Shiii 🌸", shiiiMsg.content)
                        }
                    }
                }
            }
        }
    }

    private suspend fun pollGroupMessages(user: AuthUser) {
        val result: Result<List<GroupMessage>> = repository.getGroupMessages(user.token, afterId = lastSeenGroupId)
        result.onSuccess { messages: List<GroupMessage> ->
            if (messages.isNotEmpty()) {
                val maxId = messages.mapNotNull { it.numericId }.maxOrNull()
                if (maxId != null) {
                    lastSeenGroupId = maxId
                }

                if (!isFirstSync) {
                    val activeScreen = ScreenStateTracker.currentScreen.value
                    if (activeScreen != AppScreen.GROUP_CHAT) {
                        // Only notify if message is NOT sent by current user
                        val incomingMsg = messages.lastOrNull { it.senderId != user.id }
                        if (incomingMsg != null) {
                            notificationHelper.showGroupChatNotification(incomingMsg.senderName, incomingMsg.content)
                        }
                    }
                }
            }
        }
    }

    private suspend fun pollDirectMessages(user: AuthUser) {
        // markRead = false so background check doesn't falsely mark as read before user opens screen!
        val result: Result<List<DirectMessage>> = repository.getDirectMessages(user.token, afterId = lastSeenDirectId, markRead = false)
        result.onSuccess { messages: List<DirectMessage> ->
            if (messages.isNotEmpty()) {
                val maxId = messages.mapNotNull { it.numericId }.maxOrNull()
                if (maxId != null) {
                    lastSeenDirectId = maxId
                }

                if (!isFirstSync) {
                    val activeScreen = ScreenStateTracker.currentScreen.value
                    if (activeScreen != AppScreen.DIRECT_CHAT) {
                        // Only notify for incoming messages from partner
                        val incomingMsg = messages.lastOrNull { it.senderId != user.id }
                        if (incomingMsg != null) {
                            val partnerDisplay = user.partnerName ?: (if (user.role.value == "master") "Mistress 💕" else "Master 🎩")
                            notificationHelper.showDirectChatNotification(partnerDisplay, incomingMsg.content)
                        }
                    }
                }
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }
}
