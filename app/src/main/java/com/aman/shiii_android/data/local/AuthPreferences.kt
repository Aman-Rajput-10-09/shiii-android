package com.aman.shiii_android.data.local

import android.content.Context
import android.content.SharedPreferences
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: AuthUser) {
        prefs.edit()
            .putInt(KEY_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_ROLE, user.role.value)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_TOKEN, user.token)
            .putInt(KEY_COUPLE_ID, user.coupleId ?: -1)
            .putBoolean(KEY_IS_PAIRED, user.isPaired)
            .putString(KEY_PAIR_CODE, user.pairCode ?: "")
            .putString(KEY_PARTNER_NAME, user.partnerName ?: "")
            .apply()
    }

    fun getUser(): AuthUser? {
        val token = prefs.getString(KEY_TOKEN, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val roleStr = prefs.getString(KEY_ROLE, null)

        if (token.isNullOrBlank() || username.isNullOrBlank() || roleStr.isNullOrBlank()) {
            return null
        }

        val role = if (roleStr.equals(UserRole.MASTER.value, ignoreCase = true)) {
            UserRole.MASTER
        } else {
            UserRole.MISTRESS
        }

        val coupleIdRaw = prefs.getInt(KEY_COUPLE_ID, -1)
        val pairCodeRaw = prefs.getString(KEY_PAIR_CODE, null)
        val partnerNameRaw = prefs.getString(KEY_PARTNER_NAME, null)

        return AuthUser(
            id = prefs.getInt(KEY_ID, 0),
            username = username,
            role = role,
            displayName = prefs.getString(KEY_DISPLAY_NAME, username) ?: username,
            token = token,
            coupleId = if (coupleIdRaw > 0) coupleIdRaw else null,
            isPaired = prefs.getBoolean(KEY_IS_PAIRED, false),
            pairCode = if (pairCodeRaw.isNullOrBlank()) null else pairCodeRaw,
            partnerName = if (partnerNameRaw.isNullOrBlank()) null else partnerNameRaw
        )
    }

    fun updatePairStatus(isPaired: Boolean, coupleId: Int?, partnerName: String?) {
        val editor = prefs.edit().putBoolean(KEY_IS_PAIRED, isPaired)
        if (coupleId != null) {
            editor.putInt(KEY_COUPLE_ID, coupleId)
        }
        if (partnerName != null) {
            editor.putString(KEY_PARTNER_NAME, partnerName)
        }
        editor.apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "shiii_auth_prefs"
        private const val KEY_ID = "key_id"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_ROLE = "key_role"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_COUPLE_ID = "key_couple_id"
        private const val KEY_IS_PAIRED = "key_is_paired"
        private const val KEY_PAIR_CODE = "key_pair_code"
        private const val KEY_PARTNER_NAME = "key_partner_name"
    }
}
