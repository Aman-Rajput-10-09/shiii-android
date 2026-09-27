package com.aman.shiii_android.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.repository.ShiiiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val repository: ShiiiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private var currentUser: AuthUser? = null

    fun initUser(user: AuthUser) {
        currentUser = user
        loadStatus()
    }

    fun loadStatus() {
        val user = currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getCoupleStatus(user.token)
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPaired = status.isPaired,
                            myPairCode = status.pairCode,
                            partnerName = status.partnerName,
                            partnerRole = status.partnerRole,
                            coupleId = status.coupleId
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Failed to load pair status"
                        )
                    }
                }
        }
    }

    fun onPartnerCodeChanged(code: String) {
        _uiState.update { it.copy(partnerCodeInput = code.uppercase().trim(), errorMessage = null) }
    }

    fun pairWithPartner(onSuccess: (String) -> Unit) {
        val user = currentUser ?: return
        val code = _uiState.value.partnerCodeInput.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter partner's pair code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPairing = true, errorMessage = null) }
            repository.pairCouple(user.token, code)
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            isPairing = false,
                            isPaired = true,
                            partnerName = status.partnerName,
                            partnerRole = status.partnerRole,
                            coupleId = status.coupleId,
                            successMessage = "Connected with ${status.partnerName ?: "your partner"}! 💕"
                        )
                    }
                    onSuccess(status.partnerName ?: "Partner")
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isPairing = false,
                            errorMessage = err.message ?: "Pairing failed. Please verify code!"
                        )
                    }
                }
        }
    }

    fun setCopied(copied: Boolean) {
        _uiState.update { it.copy(codeCopied = copied) }
    }
}
