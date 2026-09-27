package com.aman.shiii_android.data.remote

import com.aman.shiii_android.data.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ShiiiApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): TokenResponseDto

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto

    @GET("api/v1/couple/status")
    suspend fun getCoupleStatus(
        @Header("Authorization") token: String
    ): CoupleStatusDto

    @POST("api/v1/couple/pair")
    suspend fun pairCouple(
        @Header("Authorization") token: String,
        @Body request: PairRequestDto
    ): PairResponseDto

    @POST("api/v1/shiii/chat")
    suspend fun sendChatMessage(
        @Header("Authorization") token: String,
        @Body request: ChatRequestDto
    ): ChatResponseDto

    @GET("api/v1/shiii/master/briefings")
    suspend fun getMasterBriefings(
        @Header("Authorization") token: String
    ): List<MasterBriefingDto>

    @POST("api/v1/shiii/master/resolve")
    suspend fun resolveConcern(
        @Header("Authorization") token: String,
        @Body request: MasterResolutionRequestDto
    ): MasterResolutionResponseDto

    @GET("api/v1/shiii/messages")
    suspend fun getChatMessages(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("after_id") afterId: Int? = null
    ): List<ChatMessageDto>

    @GET("api/v1/shiii/group/messages")
    suspend fun getGroupMessages(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("after_id") afterId: Int? = null
    ): List<GroupMessageDto>

    @POST("api/v1/shiii/group/messages")
    suspend fun sendGroupMessage(
        @Header("Authorization") token: String,
        @Body request: GroupChatRequestDto
    ): List<GroupMessageDto>

    @GET("api/v1/shiii/direct/messages")
    suspend fun getDirectMessages(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("after_id") afterId: Int? = null,
        @retrofit2.http.Query("mark_read") markRead: Boolean = true
    ): List<DirectMessageDto>

    @POST("api/v1/shiii/direct/messages")
    suspend fun sendDirectMessage(
        @Header("Authorization") token: String,
        @Body request: DirectMessageRequestDto
    ): DirectMessageDto

    @POST("api/v1/shiii/direct/messages/read")
    suspend fun markDirectMessagesRead(
        @Header("Authorization") token: String
    ): Map<String, Any>

    @DELETE("api/v1/shiii/messages/clear")
    suspend fun clearMessages(
        @Header("Authorization") token: String,
        @Query("scope") scope: String
    ): Map<String, Any>
}

