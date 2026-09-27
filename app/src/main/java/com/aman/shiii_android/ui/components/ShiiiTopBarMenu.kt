package com.aman.shiii_android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.ui.character.ShiiiOverlaySettings
import com.aman.shiii_android.ui.theme.*

@Composable
fun ShiiiTopBarMenu(
    user: AuthUser,
    onLogout: () -> Unit,
    onClearHistory: (scope: String) -> Unit,
    modifier: Modifier = Modifier,
    isMistressTheme: Boolean = false
) {
    val context = LocalContext.current
    val colors = AppThemeManager.currentColors()
    val isShiiiVisible by ShiiiOverlaySettings.isShiiiVisible.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val tintColor = if (colors.isDark) colors.accentPink else DeepViolet

    Box(modifier = modifier) {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options Menu",
                tint = tintColor
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(colors.cardBg)
        ) {
            // 1. Hide / Show Shiii Sticker
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (isShiiiVisible) "Hide Shiii 🙈" else "Show Shiii 🌸",
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isShiiiVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Shiii",
                        tint = colors.accentPink
                    )
                },
                onClick = {
                    showMenu = false
                    ShiiiOverlaySettings.toggleVisibility(context)
                }
            )

            // 2. Light / Dark Mode Toggle
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (colors.isDark) "Light Mode ☀️" else "Dark Mode 🌙",
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (colors.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (colors.isDark) AmberGold else Color(0xFF6C63FF)
                    )
                },
                onClick = {
                    showMenu = false
                    AppThemeManager.toggleTheme(context)
                }
            )

            // 3. Clear Chats Option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Clear Chats 🗑️",
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Chats",
                        tint = CoralTender
                    )
                },
                onClick = {
                    showMenu = false
                    showClearDialog = true
                }
            )

            HorizontalDivider(color = colors.cardBorder)

            // 4. Log Out Option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Log Out 🚪",
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log Out",
                        tint = Color(0xFFFF5252)
                    )
                },
                onClick = {
                    showMenu = false
                    showLogoutDialog = true
                }
            )
        }
    }

    // Clear Chats Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Clear Your Chat Messages 🗑️",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Select which chat history sent by you to delete:",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )

                    // Option A: Shiii Private Chat
                    ClearOptionRow(
                        title = "🌸 Shiii AI Private Chat",
                        description = "Delete messages you sent to Shiii",
                        colors = colors,
                        onClick = {
                            showClearDialog = false
                            onClearHistory("shiii")
                        }
                    )

                    // Option B: 1-on-1 Direct Chat
                    ClearOptionRow(
                        title = "💌 1-on-1 Direct Chat",
                        description = "Delete messages you sent to your partner",
                        colors = colors,
                        onClick = {
                            showClearDialog = false
                            onClearHistory("direct")
                        }
                    )

                    // Option C: 3-Way Peace Lounge
                    ClearOptionRow(
                        title = "🕊️ 3-Way Peace Lounge",
                        description = "Delete messages you sent in the Lounge",
                        colors = colors,
                        onClick = {
                            showClearDialog = false
                            onClearHistory("group")
                        }
                    )

                    // Option D: Clear All Chats Everywhere
                    ClearOptionRow(
                        title = "⚠️ Delete All My Chats Everywhere",
                        description = "Clears all your messages across all 3 rooms",
                        colors = colors,
                        isDestructive = true,
                        onClick = {
                            showClearDialog = false
                            onClearHistory("all")
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Logout Confirmation Dialog
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
                    text = "Are you sure you want to log out? Your pairing and account remain safely saved.",
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
private fun ClearOptionRow(
    title: String,
    description: String,
    colors: AppThemeColors,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val bg = when {
        isDestructive && colors.isDark -> Color(0xFF4A1820)
        isDestructive -> Color(0xFFFFEBEE)
        colors.isDark -> Color(0xFF222638)
        else -> Color(0xFFFFF0F5)
    }

    val border = when {
        isDestructive -> Color(0xFFFF8A80).copy(alpha = 0.4f)
        colors.isDark -> Color(0xFF333B50)
        else -> Color(0xFFFFD6E0)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) Color(0xFFFF5252) else colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = if (isDestructive) Color(0xFFFF8A80) else colors.textSecondary
            )
        }
    }
}
