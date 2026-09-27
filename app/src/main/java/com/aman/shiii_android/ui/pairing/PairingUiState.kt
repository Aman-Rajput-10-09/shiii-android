package com.aman.shiii_android.ui.pairing

data class PairingUiState(
    val myPairCode: String = "",
    val partnerCodeInput: String = "",
    val isPaired: Boolean = false,
    val partnerName: String? = null,
    val partnerRole: String? = null,
    val coupleId: Int? = null,
    val isLoading: Boolean = false,
    val isPairing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val codeCopied: Boolean = false
)
