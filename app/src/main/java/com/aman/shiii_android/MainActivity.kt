package com.aman.shiii_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aman.shiii_android.ui.navigation.ShiiiAppNavigation
import com.aman.shiii_android.ui.theme.ShiiiandroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.aman.shiii_android.ui.character.ShiiiOverlaySettings.init(this)
        com.aman.shiii_android.ui.theme.AppThemeManager.init(this)
        requestNotificationPermissionIfNeeded()
        setContent {
            val isDark = com.aman.shiii_android.ui.theme.AppThemeManager.isDark()
            ShiiiandroidTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShiiiAppNavigation()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        com.aman.shiii_android.notification.ScreenStateTracker.updateScreen(
            com.aman.shiii_android.notification.AppScreen.BACKGROUND
        )
    }

    override fun onStop() {
        super.onStop()
        com.aman.shiii_android.notification.ScreenStateTracker.updateScreen(
            com.aman.shiii_android.notification.AppScreen.BACKGROUND
        )
    }
}