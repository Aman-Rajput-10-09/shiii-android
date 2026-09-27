package com.aman.shiii_android.notification

import android.util.Log
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
    companion object {
        private const val TAG = "RealtimeNotifSync"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var lastSeenPrivateId: Int? = null
    private var lastSeenGroupId: Int? = null
    private var lastSeenDirectId: Int? = null
    private var isFirstSync = true
    private var currentUser: AuthUser? = null

    fun startSync(user: AuthUser) {
        currentUser = user
        syncJob?.cancel()
        isFirstSync = true
        lastSeenPrivateId = null
        lastSeenGroupId = null
        lastSeenDirectId = null

        Log.i(TAG, "Starting Realtime Notification Sync for user: ${user.username}, role: ${user.role.value}, coupleId: ${user.coupleId}")

        syncJob = scope.launch {
            var cycleCount = 0
            while (isActive) {
                val activeUser = currentUser ?: break
                try {
                    // Check if couple status changed in the background (e.g. partner paired)
                    if (activeUser.coupleId == null || !activeUser.isPaired) {
                        cycleCount++
                        if (cycleCount % 4 == 0) { // Check every ~12 seconds if not yet paired
                            repository.getCoupleStatus(activeUser.token).onSuccess { status ->
                                if (status.isPaired || status.coupleId != null) {
                                    currentUser = activeUser.copy(
                                        isPaired = status.isPaired,
                                        coupleId = status.coupleId,
                                        partnerName = status.partnerName ?: activeUser.partnerName
                                    )
                                    Log.i(TAG, "Detected couple pairing in background! coupleId: ${status.coupleId}")
                                }
                            }
                        }
                    }

                    pollPrivateMessages(activeUser)
                    if (activeUser.coupleId != null) {
                        pollGroupMessages(activeUser)
                        pollDirectMessages(activeUser)
                    }
                    isFirstSync = false
                } catch (e: Exception) {
                    Log.w(TAG, "Polling loop error: ${e.message}")
                }
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    fun updateUser(user: AuthUser) {
        currentUser = user
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
                    val isPrivateScreen = (activeScreen == AppScreen.PRIVATE_CHAT) ||
                            (user.role.value == "master" && activeScreen == AppScreen.MASTER_BRIEFINGS)
                    val shiiiMsgs = messages.filter { it.senderRole == "shiii" }
                    if (shiiiMsgs.isNotEmpty() && !isPrivateScreen) {
                        for (msg in shiiiMsgs) {
                            Log.i(TAG, "New private message from Shiii detected: ${msg.content}")
                            notificationHelper.showPrivateChatNotification("Shiii 🌸", msg.content, msg.numericId)
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
                    val isLookingAtGroup = (activeScreen == AppScreen.GROUP_CHAT)
                    val incoming = messages.filter { it.senderId != user.id }
                    if (incoming.isNotEmpty() && !isLookingAtGroup) {
                        for (msg in incoming) {
                            Log.i(TAG, "New group message from ${msg.senderName} detected: ${msg.content}")
                            notificationHelper.showGroupChatNotification(msg.senderName, msg.content, msg.numericId)
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
                    val isLookingAtDirectChat = (activeScreen == AppScreen.DIRECT_CHAT)
                    val incoming = messages.filter { it.senderId != user.id }
                    if (incoming.isNotEmpty() && !isLookingAtDirectChat) {
                        for (msg in incoming) {
                            val partnerDisplay = user.partnerName ?: (if (user.role.value == "master") "Mistress 💕" else "Master 🎩")
                            Log.i(TAG, "New direct message from $partnerDisplay detected: ${msg.content}")
                            notificationHelper.showDirectChatNotification(partnerDisplay, msg.content, msg.numericId)
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

