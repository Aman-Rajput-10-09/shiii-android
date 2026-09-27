package com.aman.shiii_android.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen {
    BACKGROUND,
    PRIVATE_CHAT,
    DIRECT_CHAT,
    GROUP_CHAT,
    MASTER_BRIEFINGS,
    OTHER
}

object ScreenStateTracker {
    private val _currentScreen = MutableStateFlow(AppScreen.OTHER)
    val currentScreen = _currentScreen.asStateFlow()

    fun updateScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }
}
