package com.aman.shiii_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.data.local.AuthPreferences
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.domain.repository.ShiiiRepository
import com.aman.shiii_android.notification.RealtimeNotificationSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: ShiiiRepository,
    private val authPreferences: AuthPreferences,
    val notificationSyncManager: RealtimeNotificationSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun getSavedUser(): AuthUser? = authPreferences.getUser()

    fun saveUser(user: AuthUser) = authPreferences.saveUser(user)

    fun clearSavedUser() = authPreferences.clear()

    suspend fun getCoupleStatus(token: String) = repository.getCoupleStatus(token)

    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, error = null) }
    fun onRoleChange(role: UserRole) = _uiState.update { it.copy(selectedRole = role) }
    fun toggleMode() = _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) }

    fun submit() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in username and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (state.isRegisterMode) {
                repository.register(
                    username = state.username.trim(),
                    password = state.password,
                    role = state.selectedRole,
                    displayName = state.displayName.ifBlank { state.username.trim() }
                )
            } else {
                repository.login(
                    username = state.username.trim(),
                    password = state.password
                )
            }

            result.fold(
                onSuccess = { user ->
                    authPreferences.saveUser(user)
                    _uiState.update { it.copy(isLoading = false, authenticatedUser = user) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.localizedMessage ?: "Authentication failed"
                        )
                    }
                }
            )
        }
    }

    fun consumeAuth() {
        _uiState.update { it.copy(authenticatedUser = null) }
    }

    fun resetAuth() {
        _uiState.value = AuthUiState()
    }
}
