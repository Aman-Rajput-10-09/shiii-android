package com.aman.shiii_android.domain.repository

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.model.CoupleStatus
import com.aman.shiii_android.domain.model.DirectMessage
import com.aman.shiii_android.domain.model.GroupMessage
import com.aman.shiii_android.domain.model.MasterBriefing
import com.aman.shiii_android.domain.model.UserRole

interface ShiiiRepository {
    suspend fun login(username: String, password: String): Result<AuthUser>
    suspend fun register(username: String, password: String, role: UserRole, displayName: String): Result<AuthUser>
    suspend fun getCoupleStatus(token: String): Result<CoupleStatus>
    suspend fun pairCouple(token: String, pairCode: String): Result<CoupleStatus>
    suspend fun sendChatMessage(token: String, message: String): Result<ChatMessage>
    suspend fun getMasterBriefings(token: String): Result<List<MasterBriefing>>
    suspend fun resolveConcern(token: String, concernId: Int, reply: String): Result<ChatMessage>
    suspend fun getChatMessages(token: String, afterId: Int? = null): Result<List<ChatMessage>>
    suspend fun getGroupMessages(token: String, afterId: Int? = null): Result<List<GroupMessage>>
    suspend fun sendGroupMessage(token: String, message: String): Result<List<GroupMessage>>
    suspend fun getDirectMessages(token: String, afterId: Int? = null, markRead: Boolean = true): Result<List<DirectMessage>>
    suspend fun sendDirectMessage(token: String, message: String): Result<DirectMessage>
    suspend fun markDirectMessagesRead(token: String): Result<Unit>
    suspend fun clearMessages(token: String, scope: String): Result<Unit>
}
