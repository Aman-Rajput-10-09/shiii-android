package com.aman.shiii_android.data.repository

import com.aman.shiii_android.data.model.*
import com.aman.shiii_android.data.remote.ShiiiApiService
import com.aman.shiii_android.domain.model.*
import com.aman.shiii_android.domain.repository.ShiiiRepository
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiiiRepositoryImpl @Inject constructor(
    private val api: ShiiiApiService
) : ShiiiRepository {

    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = runCatching {
        block()
    }.recoverCatching { throwable ->
        if (throwable is HttpException) {
            val errorBody = throwable.response()?.errorBody()?.string()
            val parsedDetail = try {
                if (!errorBody.isNullOrBlank()) {
                    val json = JSONObject(errorBody)
                    json.optString("detail", throwable.message())
                } else throwable.message()
            } catch (e: Exception) {
                throwable.message()
            }
            throw Exception(parsedDetail ?: throwable.message(), throwable)
        } else {
            throw throwable
        }
    }

    override suspend fun login(username: String, password: String): Result<AuthUser> = safeApiCall {
        val dto = api.login(LoginRequestDto(username, password))
        AuthUser(
            id = dto.userId ?: 0,
            username = username,
            role = UserRole.fromValue(dto.role),
            displayName = dto.displayName ?: username,
            token = dto.accessToken,
            coupleId = dto.coupleId,
            isPaired = dto.isPaired,
            pairCode = dto.pairCode,
            partnerName = dto.partnerName
        )
    }

    override suspend fun register(
        username: String,
        password: String,
        role: UserRole,
        displayName: String
    ): Result<AuthUser> = safeApiCall {
        // Register user
        api.register(
            RegisterRequestDto(
                username = username,
                password = password,
                role = role.value,
                displayName = displayName
            )
        )
        // Automatically login after registration
        val loginDto = api.login(LoginRequestDto(username, password))
        AuthUser(
            id = loginDto.userId ?: 0,
            username = username,
            role = UserRole.fromValue(loginDto.role),
            displayName = loginDto.displayName ?: username,
            token = loginDto.accessToken,
            coupleId = loginDto.coupleId,
            isPaired = loginDto.isPaired,
            pairCode = loginDto.pairCode,
            partnerName = loginDto.partnerName
        )
    }

    override suspend fun getCoupleStatus(token: String): Result<CoupleStatus> = safeApiCall {
        val authHeader = "Bearer $token"
        val dto = api.getCoupleStatus(authHeader)
        CoupleStatus(
            isPaired = dto.isPaired,
            pairCode = dto.pairCode,
            partnerName = dto.partnerName,
            partnerRole = dto.partnerRole,
            coupleId = dto.coupleId
        )
    }

    override suspend fun pairCouple(token: String, pairCode: String): Result<CoupleStatus> = safeApiCall {
        val authHeader = "Bearer $token"
        val res = api.pairCouple(authHeader, PairRequestDto(pairCode))
        // Fetch updated couple status
        val statusDto = api.getCoupleStatus(authHeader)
        CoupleStatus(
            isPaired = statusDto.isPaired,
            pairCode = statusDto.pairCode,
            partnerName = statusDto.partnerName ?: res.partnerName,
            partnerRole = statusDto.partnerRole,
            coupleId = statusDto.coupleId ?: res.coupleId
        )
    }

    override suspend fun sendChatMessage(token: String, message: String): Result<ChatMessage> = safeApiCall {
        val authHeader = "Bearer $token"
        val dto = api.sendChatMessage(authHeader, ChatRequestDto(message))
        val visemes = dto.visemes?.map { VisemeFrame(it.timeMs, it.mouth) } ?: emptyList()
        ChatMessage(
            id = dto.id?.toString() ?: java.util.UUID.randomUUID().toString(),
            numericId = dto.id,
            senderRole = dto.senderRole,
            content = dto.replyText,
            englishText = dto.englishText,
            audioUrl = dto.audioUrl,
            visemes = visemes
        )
    }

    override suspend fun getMasterBriefings(token: String): Result<List<MasterBriefing>> = safeApiCall {
        val authHeader = "Bearer $token"
        val list = api.getMasterBriefings(authHeader)
        list.map {
            MasterBriefing(
                id = it.id,
                originalMistressText = it.originalMistressText,
                emotionalSentiment = it.emotionalSentiment,
                urgencyScore = it.urgencyScore,
                masterBriefing = it.masterBriefing ?: "",
                status = when (it.status.lowercase()) {
                    "resolved" -> ConcernStatus.RESOLVED
                    "briefed" -> ConcernStatus.BRIEFED
                    else -> ConcernStatus.PENDING
                },
                audioUrl = it.audioUrl
            )
        }
    }

    override suspend fun resolveConcern(
        token: String,
        concernId: Int,
        reply: String
    ): Result<ChatMessage> = safeApiCall {
        val authHeader = "Bearer $token"
        val dto = api.resolveConcern(
            authHeader,
            MasterResolutionRequestDto(concernId, reply)
        )
        ChatMessage(
            senderRole = "shiii",
            content = dto.diplomaticMessage,
            audioUrl = dto.audioUrl
        )
    }

    override suspend fun getChatMessages(token: String, afterId: Int?): Result<List<ChatMessage>> = safeApiCall {
        val authHeader = "Bearer $token"
        val list = api.getChatMessages(authHeader, afterId)
        list.map { dto ->
            val visemes = dto.visemes?.map { VisemeFrame(it.timeMs, it.mouth) } ?: emptyList()
            ChatMessage(
                id = dto.id.toString(),
                numericId = dto.id,
                senderRole = dto.senderRole,
                content = dto.content,
                englishText = dto.englishText,
                audioUrl = dto.audioUrl,
                visemes = visemes
            )
        }
    }

    override suspend fun getGroupMessages(token: String, afterId: Int?): Result<List<GroupMessage>> = safeApiCall {
        val authHeader = "Bearer $token"
        val list = api.getGroupMessages(authHeader, afterId)
        list.map { dto ->
            val visemes = dto.visemes?.map { VisemeFrame(it.timeMs, it.mouth) } ?: emptyList()
            GroupMessage(
                id = dto.id.toString(),
                numericId = dto.id,
                coupleId = dto.coupleId,
                senderId = dto.senderId,
                senderRole = dto.senderRole,
                senderName = dto.senderName,
                content = dto.content,
                englishText = dto.englishText,
                audioUrl = dto.audioUrl,
                visemes = visemes
            )
        }
    }

    override suspend fun sendGroupMessage(token: String, message: String): Result<List<GroupMessage>> = safeApiCall {
        val authHeader = "Bearer $token"
        val list = api.sendGroupMessage(authHeader, GroupChatRequestDto(message))
        list.map { dto ->
            val visemes = dto.visemes?.map { VisemeFrame(it.timeMs, it.mouth) } ?: emptyList()
            GroupMessage(
                id = dto.id.toString(),
                numericId = dto.id,
                coupleId = dto.coupleId,
                senderId = dto.senderId,
                senderRole = dto.senderRole,
                senderName = dto.senderName,
                content = dto.content,
                englishText = dto.englishText,
                audioUrl = dto.audioUrl,
                visemes = visemes
            )
        }
    }

    override suspend fun getDirectMessages(token: String, afterId: Int?, markRead: Boolean): Result<List<DirectMessage>> = safeApiCall {
        val authHeader = "Bearer $token"
        val list = api.getDirectMessages(authHeader, afterId, markRead)
        list.map { dto ->
            DirectMessage(
                id = dto.id.toString(),
                numericId = dto.id,
                coupleId = dto.coupleId,
                senderId = dto.senderId,
                receiverId = dto.receiverId,
                senderRole = dto.senderRole,
                content = dto.content,
                isRead = dto.isRead,
                readAt = dto.readAt
            )
        }
    }

    override suspend fun sendDirectMessage(token: String, message: String): Result<DirectMessage> = safeApiCall {
        val authHeader = "Bearer $token"
        val dto = api.sendDirectMessage(authHeader, DirectMessageRequestDto(message))
        DirectMessage(
            id = dto.id.toString(),
            numericId = dto.id,
            coupleId = dto.coupleId,
            senderId = dto.senderId,
            receiverId = dto.receiverId,
            senderRole = dto.senderRole,
            content = dto.content,
            isRead = dto.isRead,
            readAt = dto.readAt
        )
    }

    override suspend fun markDirectMessagesRead(token: String): Result<Unit> = safeApiCall {
        val authHeader = "Bearer $token"
        api.markDirectMessagesRead(authHeader)
        Unit
    }

    override suspend fun clearMessages(token: String, scope: String): Result<Unit> = safeApiCall {
        val authHeader = "Bearer $token"
        api.clearMessages(authHeader, scope)
        Unit
    }
}
