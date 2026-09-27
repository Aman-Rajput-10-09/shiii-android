package com.aman.shiii_android.ui.auth

import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val displayName: String = "",
    val selectedRole: UserRole = UserRole.MISTRESS,
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val authenticatedUser: AuthUser? = null
)
