package com.aman.shiii_android.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.ui.chat.ChatScreen
import com.aman.shiii_android.ui.chat.ChatViewModel
import com.aman.shiii_android.ui.direct.DirectChatScreen
import com.aman.shiii_android.ui.direct.DirectChatViewModel
import com.aman.shiii_android.ui.group.GroupChatScreen
import com.aman.shiii_android.ui.group.GroupChatViewModel
import com.aman.shiii_android.ui.master.MasterScreen
import com.aman.shiii_android.ui.master.MasterViewModel
import com.aman.shiii_android.notification.AppScreen
import com.aman.shiii_android.notification.ScreenStateTracker
import com.aman.shiii_android.ui.settings.SettingsScreen
import com.aman.shiii_android.ui.theme.*

enum class CoupleTab {
    SHIII_AI,
    DIRECT_CHAT,
    GROUP_LOUNGE
}

@Composable
fun MainContainerScreen(
    user: AuthUser,
    onLogout: () -> Unit,
    initialTab: CoupleTab = CoupleTab.SHIII_AI,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val chatViewModel: ChatViewModel = hiltViewModel()
    val masterViewModel: MasterViewModel = hiltViewModel()
    val directChatViewModel: DirectChatViewModel = hiltViewModel()
    val groupChatViewModel: GroupChatViewModel = hiltViewModel()

    val isMaster = (user.role == UserRole.MASTER)
    val colors = AppThemeManager.currentColors()

    val handleClearAllHistory: (String) -> Unit = { scope ->
        chatViewModel.clearMyMessages(scope)
        masterViewModel.clearMyMessages(scope)
        directChatViewModel.clearMyMessages(scope)
        groupChatViewModel.clearMyMessages(scope)
    }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            CoupleTab.SHIII_AI -> ScreenStateTracker.updateScreen(
                if (isMaster) AppScreen.MASTER_BRIEFINGS else AppScreen.PRIVATE_CHAT
            )
            CoupleTab.DIRECT_CHAT -> ScreenStateTracker.updateScreen(AppScreen.DIRECT_CHAT)
            CoupleTab.GROUP_LOUNGE -> ScreenStateTracker.updateScreen(AppScreen.GROUP_CHAT)
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            user = user,
            onBack = { isSettingsOpen = false },
            onLogout = onLogout,
            onClearHistory = handleClearAllHistory
        )
    } else {
        Scaffold(
            bottomBar = {
                Surface(
                    color = colors.bottomNavBg,
                    shadowElevation = 10.dp,
                    border = if (!colors.isDark) BorderStroke(1.dp, Color(0xFFFFE4EC)) else BorderStroke(1.dp, Color(0xFF232838)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    NavigationBar(
                        containerColor = colors.bottomNavBg,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Tab 1: Shiii AI
                        NavigationBarItem(
                            selected = selectedTab == CoupleTab.SHIII_AI,
                            onClick = { selectedTab = CoupleTab.SHIII_AI },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == CoupleTab.SHIII_AI) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                    contentDescription = "Shiii AI",
                                    tint = if (selectedTab == CoupleTab.SHIII_AI) CherryBlossomPink else colors.bottomNavUnselected
                                )
                            },
                            label = {
                                Text(
                                    text = if (isMaster) "Intel & Shiii" else "Shiii AI",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == CoupleTab.SHIII_AI) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == CoupleTab.SHIII_AI) (if (colors.isDark) SoftRose else DeepViolet) else colors.bottomNavUnselected
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = colors.bottomNavIndicator
                            )
                        )

                        // Tab 2: Direct 1-on-1 Chat
                        NavigationBarItem(
                            selected = selectedTab == CoupleTab.DIRECT_CHAT,
                            onClick = { selectedTab = CoupleTab.DIRECT_CHAT },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == CoupleTab.DIRECT_CHAT) Icons.Filled.Forum else Icons.Outlined.Forum,
                                    contentDescription = "Direct Chat",
                                    tint = if (selectedTab == CoupleTab.DIRECT_CHAT) CherryBlossomPink else colors.bottomNavUnselected
                                )
                            },
                            label = {
                                Text(
                                    text = "Direct 💌",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == CoupleTab.DIRECT_CHAT) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == CoupleTab.DIRECT_CHAT) (if (colors.isDark) SoftRose else DeepViolet) else colors.bottomNavUnselected
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = colors.bottomNavIndicator
                            )
                        )

                        // Tab 3: 3-Way Peace Lounge
                        NavigationBarItem(
                            selected = selectedTab == CoupleTab.GROUP_LOUNGE,
                            onClick = { selectedTab = CoupleTab.GROUP_LOUNGE },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == CoupleTab.GROUP_LOUNGE) Icons.Filled.Diversity1 else Icons.Outlined.Diversity1,
                                    contentDescription = "Peace Lounge",
                                    tint = if (selectedTab == CoupleTab.GROUP_LOUNGE) CoralTender else colors.bottomNavUnselected
                                )
                            },
                            label = {
                                Text(
                                    text = "3-Way Lounge 🕊️",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == CoupleTab.GROUP_LOUNGE) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == CoupleTab.GROUP_LOUNGE) (if (colors.isDark) CoralTender else DeepViolet) else colors.bottomNavUnselected
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CoralTender.copy(alpha = 0.22f)
                            )
                        )
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(220),
                    label = "tab_crossfade"
                ) { tab ->
                    when (tab) {
                        CoupleTab.SHIII_AI -> {
                            if (isMaster) {
                                MasterScreen(
                                    user = user,
                                    viewModel = masterViewModel,
                                    onLogout = onLogout,
                                    onOpenGroupChat = { selectedTab = CoupleTab.GROUP_LOUNGE },
                                    onOpenSettings = { isSettingsOpen = true }
                                )
                            } else {
                                ChatScreen(
                                    user = user,
                                    viewModel = chatViewModel,
                                    onLogout = onLogout,
                                    onOpenGroupChat = { selectedTab = CoupleTab.GROUP_LOUNGE },
                                    onOpenSettings = { isSettingsOpen = true }
                                )
                            }
                        }
                        CoupleTab.DIRECT_CHAT -> {
                            DirectChatScreen(
                                user = user,
                                viewModel = directChatViewModel,
                                onLogout = onLogout,
                                onOpenSettings = { isSettingsOpen = true }
                            )
                        }
                        CoupleTab.GROUP_LOUNGE -> {
                            GroupChatScreen(
                                user = user,
                                viewModel = groupChatViewModel,
                                onBack = { selectedTab = CoupleTab.SHIII_AI },
                                onLogout = onLogout,
                                onOpenSettings = { isSettingsOpen = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
