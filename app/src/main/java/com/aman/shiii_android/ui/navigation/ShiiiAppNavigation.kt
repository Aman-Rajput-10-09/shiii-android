package com.aman.shiii_android.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.ui.auth.AuthScreen
import com.aman.shiii_android.ui.auth.AuthViewModel
import com.aman.shiii_android.ui.main.MainContainerScreen
import com.aman.shiii_android.ui.pairing.PairingScreen
import com.aman.shiii_android.ui.pairing.PairingViewModel

@Composable
fun ShiiiAppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val savedUser = remember { authViewModel.getSavedUser() }
    var currentUser by remember { mutableStateOf(savedUser) }
    var showPairingScreen by remember { mutableStateOf(savedUser?.isPaired != true) }

    // Real-time Notification Background Sync: Polls for new private, group & direct messages
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null && user.isPaired) {
            authViewModel.notificationSyncManager.startSync(user)
        } else {
            authViewModel.notificationSyncManager.stopSync()
        }
    }

    val handleLogout: () -> Unit = {
        authViewModel.notificationSyncManager.stopSync()
        authViewModel.clearSavedUser()
        authViewModel.resetAuth()
        currentUser = null
        showPairingScreen = true
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            viewModel = authViewModel,
            onAuthSuccess = { authUser ->
                authViewModel.saveUser(authUser)
                currentUser = authUser
                showPairingScreen = !authUser.isPaired
            }
        )
    } else if (showPairingScreen) {
        val pairingViewModel: PairingViewModel = hiltViewModel()
        PairingScreen(
            user = user,
            viewModel = pairingViewModel,
            onContinue = { updatedUser ->
                authViewModel.saveUser(updatedUser)
                currentUser = updatedUser
                showPairingScreen = false
            },
            onLogout = handleLogout
        )
    } else {
        MainContainerScreen(
            user = user,
            onLogout = handleLogout
        )
    }
}

