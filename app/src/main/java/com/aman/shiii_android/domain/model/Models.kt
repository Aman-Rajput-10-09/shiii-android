package com.aman.shiii_android.domain.model

enum class UserRole(val value: String) {
    MASTER("master"),
    MISTRESS("mistress");

    companion object {
        fun fromValue(value: String): UserRole =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: MISTRESS
    }
}

enum class ConcernStatus(val value: String) {
    PENDING("pending"),
    BRIEFED("briefed"),
    RESOLVED("resolved")
}

data class CoupleStatus(
    val isPaired: Boolean,
    val pairCode: String,
    val partnerName: String? = null,
    val partnerRole: String? = null,
    val coupleId: Int? = null
)

data class AuthUser(
    val id: Int = 0,
    val username: String,
    val role: UserRole,
    val displayName: String,
    val token: String,
    val coupleId: Int? = null,
    val isPaired: Boolean = false,
    val pairCode: String? = null,
    val partnerName: String? = null
)

data class VisemeFrame(
    val timeMs: Long,
    val mouth: String // "closed", "A", "I", "U", "E", "O"
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val numericId: Int? = null,
    val senderRole: String, // "master", "mistress", "shiii"
    val content: String,
    val audioUrl: String? = null,
    val visemes: List<VisemeFrame> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class MasterBriefing(
    val id: Int,
    val originalMistressText: String,
    val emotionalSentiment: String,
    val urgencyScore: Float,
    val masterBriefing: String,
    val status: ConcernStatus,
    val audioUrl: String? = null
)

data class GroupMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val numericId: Int? = null,
    val coupleId: Int,
    val senderId: Int? = null,
    val senderRole: String, // "master", "mistress", "shiii"
    val senderName: String,
    val content: String,
    val audioUrl: String? = null,
    val visemes: List<VisemeFrame> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class DirectMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val numericId: Int? = null,
    val coupleId: Int,
    val senderId: Int,
    val receiverId: Int,
    val senderRole: String? = null, // "master" or "mistress"
    val content: String,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
