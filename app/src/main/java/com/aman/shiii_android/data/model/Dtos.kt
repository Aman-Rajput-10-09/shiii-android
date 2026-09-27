package com.aman.shiii_android.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("display_name") val displayName: String? = null
)

data class TokenResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("role") val role: String,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("couple_id") val coupleId: Int? = null,
    @SerializedName("is_paired") val isPaired: Boolean = false,
    @SerializedName("pair_code") val pairCode: String? = null,
    @SerializedName("partner_name") val partnerName: String? = null
)

data class CoupleStatusDto(
    @SerializedName("is_paired") val isPaired: Boolean,
    @SerializedName("pair_code") val pairCode: String,
    @SerializedName("partner_name") val partnerName: String?,
    @SerializedName("partner_role") val partnerRole: String?,
    @SerializedName("couple_id") val coupleId: Int?
)

data class PairRequestDto(
    @SerializedName("pair_code") val pairCode: String
)

data class PairResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("partner_name") val partnerName: String,
    @SerializedName("couple_id") val coupleId: Int
)

data class ChatRequestDto(
    @SerializedName("message") val message: String
)

data class VisemeCueDto(
    @SerializedName("time_ms") val timeMs: Long,
    @SerializedName("mouth") val mouth: String
)

data class ChatResponseDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("reply_text") val replyText: String,
    @SerializedName("english_text") val englishText: String? = null,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("visemes") val visemes: List<VisemeCueDto>?,
    @SerializedName("sender_role") val senderRole: String
)

data class ChatMessageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("sender_role") val senderRole: String,
    @SerializedName("content") val content: String,
    @SerializedName("english_text") val englishText: String? = null,
    @SerializedName("audio_url") val audioUrl: String? = null,
    @SerializedName("visemes") val visemes: List<VisemeCueDto>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)


data class MasterBriefingDto(
    @SerializedName("id") val id: Int,
    @SerializedName("original_mistress_text") val originalMistressText: String,
    @SerializedName("emotional_sentiment") val emotionalSentiment: String,
    @SerializedName("urgency_score") val urgencyScore: Float,
    @SerializedName("master_briefing") val masterBriefing: String?,
    @SerializedName("status") val status: String,
    @SerializedName("audio_url") val audioUrl: String? = null
)

data class MasterResolutionRequestDto(
    @SerializedName("concern_id") val concernId: Int,
    @SerializedName("master_reply") val masterReply: String
)

data class MasterResolutionResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("diplomatic_message_for_mistress") val diplomaticMessage: String,
    @SerializedName("audio_url") val audioUrl: String?
)

data class GroupChatRequestDto(
    @SerializedName("message") val message: String
)

data class GroupMessageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("couple_id") val coupleId: Int,
    @SerializedName("sender_id") val senderId: Int?,
    @SerializedName("sender_role") val senderRole: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("content") val content: String,
    @SerializedName("english_text") val englishText: String? = null,
    @SerializedName("audio_url") val audioUrl: String? = null,
    @SerializedName("visemes") val visemes: List<VisemeCueDto>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class DirectMessageRequestDto(
    @SerializedName("content") val content: String
)

data class DirectMessageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("couple_id") val coupleId: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    @SerializedName("sender_role") val senderRole: String? = null,
    @SerializedName("content") val content: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
