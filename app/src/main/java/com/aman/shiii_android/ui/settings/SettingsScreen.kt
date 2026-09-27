package com.aman.shiii_android.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.ui.character.ShiiiOverlaySettings
import com.aman.shiii_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: AuthUser,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onClearHistory: (scope: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val colors = AppThemeManager.currentColors()
    val isShiiiVisible by ShiiiOverlaySettings.isShiiiVisible.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    var showClearScopeDialog by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var copiedCodeToast by remember { mutableStateOf(false) }

    val isMaster = (user.role == UserRole.MASTER)
    val roleTitle = if (isMaster) "Master 🎩" else "Mistress 💕"
    val partnerTitle = if (isMaster) "Mistress" else "Master"
    val partnerDisplayName = user.partnerName ?: "Not Paired"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.topBarTitle
                        )
                        Text(
                            text = "Profile & App Preferences",
                            fontSize = 12.sp,
                            color = colors.topBarSubtitle
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (colors.isDark) colors.accentPink else DeepViolet
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.topBarBg)
            )
        },
        snackbarHost = {
            if (statusMsg != null) {
                Snackbar(
                    containerColor = colors.cardBg,
                    contentColor = colors.textPrimary,
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissStatus() }) {
                            Text("OK", color = colors.accentPink)
                        }
                    }
                ) {
                    Text(statusMsg ?: "")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(colors = colors.backgroundGradient))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. Profile & Couple Status Card
            // ==========================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                border = BorderStroke(1.dp, colors.cardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        if (isMaster) listOf(Color(0xFF3A3F58), Color(0xFF6C63FF))
                                        else listOf(Color(0xFFFF8DA1), Color(0xFFFFB6C1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isMaster) "👑" else "🌸",
                                fontSize = 28.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.displayName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMaster) Color(0xFF6C63FF).copy(alpha = 0.18f) else Color(0xFFFF4081).copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = roleTitle,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMaster) Color(0xFF8C82FF) else Color(0xFFFF4081),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.6f))

                    // Partner info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Paired Partner",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "$partnerTitle $partnerDisplayName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }

                        val codeToDisplay = user.pairCode ?: user.coupleId?.toString()
                        if (!codeToDisplay.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (colors.isDark) Color(0xFF262C40) else Color(0xFFFFF0F5),
                                border = BorderStroke(1.dp, colors.cardBorder),
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString(codeToDisplay))
                                    copiedCodeToast = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Couple Code",
                                        tint = colors.accentPink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (copiedCodeToast) "Copied! ✨" else "Code: $codeToDisplay",
                                        fontSize = 11.sp,
                                        color = colors.accentPink,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. Shiii Companion Settings
            // ==========================================
            SettingsSectionCard(title = "🌸 Shiii Companion", colors = colors) {
                // Toggle Shiii Visibility
                SettingsToggleRow(
                    icon = if (isShiiiVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    iconTint = colors.accentPink,
                    title = "Interactive Shiii Sticker",
                    subtitle = if (isShiiiVisible) "Shiii is visible on your screen" else "Shiii is hidden from screen",
                    checked = isShiiiVisible,
                    onCheckedChange = { ShiiiOverlaySettings.toggleVisibility(context) },
                    colors = colors
                )

                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

                // Voice Tuning & Test Audio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accentPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Voice Tuning",
                                tint = colors.accentPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Anime Girl Voice (Android TTS)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Pitch 1.62x • Sweet & high tempo",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.testAnimeVoice() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.accentPink.copy(alpha = 0.18f),
                            contentColor = colors.accentPink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Test 🎵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // 2.5 Real-Time Notifications & Alerts
            // ==========================================
            val hasNotifPermission = viewModel.notificationHelper.hasPermission()
            SettingsSectionCard(title = "🔔 Notifications & Alerts", colors = colors) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (hasNotifPermission) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasNotifPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "Notification Status",
                                tint = if (hasNotifPermission) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (hasNotifPermission) "Notifications Active ✨" else "Notifications Disabled ⚠️",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (hasNotifPermission) "Direct, Group & Shiii alerts enabled" else "Permission required to receive message alerts",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            if (!hasNotifPermission) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    viewModel.sendTestNotification()
                                }
                            } else {
                                viewModel.sendTestNotification()
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.accentPink.copy(alpha = 0.18f),
                            contentColor = colors.accentPink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (hasNotifPermission) "Test 🔔" else "Enable ⚙️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // 3. Appearance & Theme
            // ==========================================
            SettingsSectionCard(title = "🎨 Appearance & Theme", colors = colors) {
                SettingsToggleRow(
                    icon = if (colors.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    iconTint = if (colors.isDark) AmberGold else Color(0xFF6C63FF),
                    title = if (colors.isDark) "Dark Mode (Obsidian Slate)" else "Light Mode (Blossom Garden)",
                    subtitle = if (colors.isDark) "Deep dark tones for late nights" else "Bright gentle daylight palette",
                    checked = colors.isDark,
                    onCheckedChange = { AppThemeManager.toggleTheme(context) },
                    colors = colors
                )
            }

            // ==========================================
            // 4. Chat Data & History Management
            // ==========================================
            SettingsSectionCard(title = "🗑️ Manage Chat History", colors = colors) {
                Text(
                    text = "You can delete only the messages you sent. Partner messages and Shiii responses will stay safe.",
                    fontSize = 11.5.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Option 1: Shiii AI Chat
                SettingsActionRow(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = CherryBlossomPink,
                    title = "Clear Shiii AI Chat",
                    subtitle = "Delete messages you sent to Shiii",
                    colors = colors,
                    onClick = { showClearScopeDialog = "shiii" }
                )

                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

                // Option 2: 1-on-1 Direct Chat
                SettingsActionRow(
                    icon = Icons.Default.Forum,
                    iconTint = Color(0xFF6C63FF),
                    title = "Clear 1-on-1 Direct Chat",
                    subtitle = "Delete messages you sent to your partner",
                    colors = colors,
                    onClick = { showClearScopeDialog = "direct" }
                )

                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

                // Option 3: 3-Way Lounge
                SettingsActionRow(
                    icon = Icons.Default.Diversity1,
                    iconTint = CoralTender,
                    title = "Clear 3-Way Peace Lounge",
                    subtitle = "Delete messages you sent in the Lounge",
                    colors = colors,
                    onClick = { showClearScopeDialog = "group" }
                )

                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

                // Option 4: Clear All Everywhere
                SettingsActionRow(
                    icon = Icons.Default.DeleteForever,
                    iconTint = Color(0xFFFF5252),
                    title = "Delete All My Messages Everywhere",
                    subtitle = "Deletes your messages across all 3 chat rooms",
                    isDestructive = true,
                    colors = colors,
                    onClick = { showClearScopeDialog = "all" }
                )
            }

            // ==========================================
            // 5. Account & Session
            // ==========================================
            SettingsSectionCard(title = "🚪 Account & Session", colors = colors) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (colors.isDark) Color(0xFF38151D) else Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFFF8A80).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showLogoutDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFF5252).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Log Out",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Log Out of Shiii",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                            Text(
                                text = "Your pairing and credentials will stay saved",
                                fontSize = 11.5.sp,
                                color = if (colors.isDark) Color(0xFFFFCDD2) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 6. About App & Version
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Shiii Couple Companion v1.0.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Text(
                    text = "Crafted with ❤️ for Master & Mistress",
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }

    // ==========================================
    // Clear Messages Confirmation Dialog
    // ==========================================
    val currentScope = showClearScopeDialog
    if (currentScope != null) {
        val scopeLabel = when (currentScope) {
            "shiii" -> "Shiii AI Chat messages"
            "direct" -> "1-on-1 Direct Chat messages"
            "group" -> "3-Way Peace Lounge messages"
            else -> "all your chat messages everywhere"
        }

        AlertDialog(
            onDismissRequest = { showClearScopeDialog = null },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Clear Messages?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete $scopeLabel sent by you? This action cannot be undone.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val token = user.token
                        viewModel.clearHistory(token, currentScope) {
                            onClearHistory(currentScope)
                        }
                        showClearScopeDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentScope == "all") Color(0xFFFF5252) else colors.accentPink
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearScopeDialog = null }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // ==========================================
    // Logout Confirmation Dialog
    // ==========================================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Log Out?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out? You can sign right back in anytime.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    colors: AppThemeColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AppThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = colors.textSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accentPink
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    colors: AppThemeColors,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDestructive && colors.isDark) Color(0xFF38151D)
                else if (isDestructive) Color(0xFFFFEBEE)
                else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = if (isDestructive) 8.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDestructive) Color(0xFFFF5252) else colors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isDestructive) Color(0xFFFF8A80) else colors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = if (isDestructive) Color(0xFFFF5252) else colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
