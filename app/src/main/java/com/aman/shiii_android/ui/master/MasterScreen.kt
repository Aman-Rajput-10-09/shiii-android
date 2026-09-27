package com.aman.shiii_android.ui.master

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.aman.shiii_android.ui.components.ShiiiTopBarMenu
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.domain.model.MasterBriefing
import com.aman.shiii_android.notification.AppScreen
import com.aman.shiii_android.notification.ScreenStateTracker
import com.aman.shiii_android.ui.character.DraggableShiiiOverlay
import com.aman.shiii_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterScreen(
    user: AuthUser,
    viewModel: MasterViewModel,
    onLogout: () -> Unit,
    onOpenGroupChat: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val colors = AppThemeManager.currentColors()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                ScreenStateTracker.updateScreen(AppScreen.MASTER_BRIEFINGS)
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                ScreenStateTracker.updateScreen(AppScreen.BACKGROUND)
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(user) {
        ScreenStateTracker.updateScreen(AppScreen.MASTER_BRIEFINGS)
        viewModel.initUser(user)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = colors.accentPink.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎩", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Master Command",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.topBarTitle
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.accentPink.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "VIP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.accentPink,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (!user.partnerName.isNullOrBlank()) "Paired with Mistress ${user.partnerName} 💕" else "Master ${user.displayName}",
                                fontSize = 11.sp,
                                color = colors.topBarSubtitle
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (colors.isDark) colors.accentPink else DeepViolet
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.topBarBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(colors = colors.backgroundGradient))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Sleek Pill Navigation Tab Selector
                Surface(
                    color = if (colors.isDark) Color(0xFF161922) else Color(0xFFFFF0F5).copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Briefings Tab Button
                        val isBriefingsSelected = state.selectedTab == 0
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isBriefingsSelected) colors.accentPink else colors.cardBg,
                            border = if (!isBriefingsSelected) BorderStroke(1.dp, colors.cardBorder) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onTabSelected(0) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Briefings 📋",
                                    fontSize = 13.sp,
                                    fontWeight = if (isBriefingsSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isBriefingsSelected) Color.White else colors.textSecondary
                                )
                                if (state.briefings.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isBriefingsSelected) Color.White else CoralTender,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${state.briefings.size}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBriefingsSelected) DeepViolet else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Confide Tab Button
                        val isConfideSelected = state.selectedTab == 1
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isConfideSelected) colors.accentPink else colors.cardBg,
                            border = if (!isConfideSelected) BorderStroke(1.dp, colors.cardBorder) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onTabSelected(1) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Confide in Shiii 💬",
                                    fontSize = 13.sp,
                                    fontWeight = if (isConfideSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isConfideSelected) Color.White else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                if (state.selectedTab == 0) {
                    // Briefings Tab Content with Pull-To-Refresh
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = colors.accentPink)
                            }
                        } else if (state.briefings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = colors.cardBg,
                                    border = BorderStroke(1.dp, colors.cardBorder),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = colors.accentPink.copy(alpha = 0.15f),
                                            modifier = Modifier.size(64.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Favorite,
                                                    contentDescription = "Peace",
                                                    tint = colors.accentPink,
                                                    modifier = Modifier.size(34.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Peace in the Realm, Master! 🌸",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Mistress is feeling serene and happy right now. Shiii is keeping gentle watch.",
                                            fontSize = 13.sp,
                                            color = colors.textSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.onTabSelected(1) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentPink),
                                            border = BorderStroke(1.dp, colors.accentPink.copy(alpha = 0.5f))
                                        ) {
                                            Text("Talk to Shiii anyway 💬", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Relationship Intel & Concerns 🛡️",
                                            color = colors.accentPink,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${state.briefings.size} to review",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                items(state.briefings, key = { it.id }) { item ->
                                    val isItemPlaying = state.isSpeechActive && (
                                        state.pendingSpeech?.content == item.masterBriefing ||
                                        state.playingAudioUrl == item.audioUrl
                                    )
                                    BriefingCard(
                                        briefing = item,
                                        isPlaying = isItemPlaying,
                                        onPlayBriefing = { viewModel.playBriefing(item) },
                                        onRespond = { viewModel.selectConcern(item) }
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                } else {
                    // Confide in Shiii Chat Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Helpful Prompt Suggestion Chips for Master
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val suggestions = listOf(
                                "How is Mistress feeling today? 💕",
                                "Give me advice on comforting her 🌸",
                                "What is Mistress's love language? ✨",
                                "How can I surprise her tonight? 🎁"
                            )
                            suggestions.forEach { prompt ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.chipBg,
                                    border = BorderStroke(1.dp, colors.chipBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.onChatInputChanged(prompt)
                                        }
                                ) {
                                    Text(
                                        text = prompt,
                                        fontSize = 11.sp,
                                        color = colors.chipText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = viewModel::refresh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (state.chatMessages.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 28.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = colors.cardBg,
                                                border = BorderStroke(1.dp, colors.cardBorder),
                                                shadowElevation = 2.dp,
                                                modifier = Modifier.fillMaxWidth(0.92f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(20.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Shiii is at your service, Master! 🎩",
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary,
                                                        fontSize = 15.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Ask about Mistress's unspoken thoughts, relationship advice, or plan the sweetest surprises together.",
                                                        color = colors.textSecondary,
                                                        fontSize = 12.sp,
                                                        textAlign = TextAlign.Center,
                                                        lineHeight = 17.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                items(state.chatMessages, key = { it.id }) { msg ->
                                    val isShiii = msg.senderRole == "shiii"
                                    val isPlayingThis = state.isSpeechActive && (
                                        state.playingAudioUrl == msg.audioUrl ||
                                        state.pendingSpeech?.content == msg.content
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isShiii) Arrangement.Start else Arrangement.End
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isShiii) 4.dp else 18.dp,
                                                bottomEnd = if (isShiii) 18.dp else 4.dp
                                            ),
                                            color = if (isShiii) colors.receivedBubbleBg else colors.sentBubbleBg,
                                            border = if (isShiii) BorderStroke(1.dp, colors.receivedBubbleBorder) else null,
                                            shadowElevation = if (colors.isDark) 1.dp else 2.dp,
                                            modifier = Modifier.widthIn(max = 290.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (isShiii) "Shiii 🌸" else "Master 🎩",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isShiii) (if (colors.isDark) SoftRose else DeepViolet) else Color.White.copy(alpha = 0.9f)
                                                    )
                                                    if (isShiii) {
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isPlayingThis) colors.accentPink else (if (colors.isDark) Color(0xFF161922) else Color.White),
                                                            border = BorderStroke(0.5.dp, colors.cardBorder),
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable { viewModel.playMessage(msg) }
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = if (isPlayingThis) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                                                                    contentDescription = "Hear Shiii",
                                                                    tint = if (isPlayingThis) Color.White else (if (colors.isDark) SoftRose else DeepViolet),
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Text(
                                                                    text = if (isPlayingThis) "Speaking" else "Hear voice 🔊",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = if (isPlayingThis) Color.White else (if (colors.isDark) SoftRose else DeepViolet)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = msg.content,
                                                    fontSize = 13.5.sp,
                                                    color = if (isShiii) colors.receivedBubbleText else colors.sentBubbleText,
                                                    lineHeight = 19.sp
                                                )
                                                if (isShiii && !msg.englishText.isNullOrBlank() && msg.englishText != msg.content) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = (if (colors.isDark) Color(0x33A855F7) else Color(0x1AEA5E8C)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = "🇬🇧 ${msg.englishText}",
                                                            fontSize = 11.5.sp,
                                                            color = if (colors.isDark) Color(0xFFE9D5FF) else Color(0xFF6B21A8),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            lineHeight = 16.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }

                        // Master Chat Input Bar
                        Surface(
                            tonalElevation = 8.dp,
                            color = colors.inputBarBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = state.chatInputText,
                                    onValueChange = viewModel::onChatInputChanged,
                                    placeholder = { Text("Confide in Shiii, Master...", fontSize = 13.sp, color = colors.inputFieldHint) },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = colors.inputFieldText,
                                        unfocusedTextColor = colors.inputFieldText,
                                        focusedBorderColor = colors.accentPink,
                                        unfocusedBorderColor = colors.cardBorder,
                                        focusedContainerColor = colors.inputFieldBg,
                                        unfocusedContainerColor = colors.inputFieldBg
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    maxLines = 3
                                )

                                Button(
                                    onClick = viewModel::sendChatMessage,
                                    enabled = state.chatInputText.isNotBlank() && !state.isSendingChat,
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPink),
                                    contentPadding = PaddingValues(12.dp),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    if (state.isSendingChat) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Draggable Floating Shiii Overlay with Spring Bounce Drop Animation!
            // Cleanly placed at top layer of root Box with no conflicting inner clickables!
            DraggableShiiiOverlay(
                state = state.characterState,
                mouthShape = state.mouthShape,
                pendingSpeech = state.pendingSpeech,
                isPlaying = state.isSpeechActive,
                onSpeechClick = viewModel::toggleSpeech,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                isDarkTheme = colors.isDark
            )
        }
    }

    // Resolution Modal
    state.selectedConcern?.let { concern ->
        AlertDialog(
            onDismissRequest = { viewModel.selectConcern(null) },
            containerColor = colors.cardBg,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Comfort Mistress through Shiii 💕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            },
            text = {
                Column {
                    // Original Concern Quote Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (colors.isDark) Color(0xFF212534) else Color(0xFFFFF7F9),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Mistress's heart: 💭",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accentPink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${concern.originalMistressText}\"",
                                fontSize = 12.5.sp,
                                fontStyle = FontStyle.Italic,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reassurance Presets
                    Text("Quick comfort starters:", fontSize = 11.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val comfortOptions = listOf(
                            "I'm coming home with hugs 💕",
                            "You mean the world to me 🌸",
                            "Let's cuddle tonight, I'm all yours ✨"
                        )
                        comfortOptions.forEach { opt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.chipBg,
                                border = BorderStroke(1.dp, colors.chipBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.onReplyTextChange(opt) }
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 10.sp,
                                    color = colors.chipText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.replyText,
                        onValueChange = viewModel::onReplyTextChange,
                        placeholder = { Text("Your reassuring words to Mistress...", fontSize = 12.sp, color = colors.inputFieldHint) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.inputFieldText,
                            unfocusedTextColor = colors.inputFieldText,
                            focusedBorderColor = colors.accentPink,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedContainerColor = colors.inputFieldBg,
                            unfocusedContainerColor = colors.inputFieldBg
                        )
                    )

                    state.resolutionResult?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (colors.isDark) Color(0xFF212534) else Color(0xFFFFF0F5),
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                val isResPlaying = state.isSpeechActive && state.pendingSpeech?.content == res
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Shiii's Sweet Delivery 💕:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accentPink
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isResPlaying) colors.accentPink else (if (colors.isDark) Color(0xFF161922) else DeepViolet),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.playResolution(res) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isResPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                                                contentDescription = "Hear Delivery",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = if (isResPlaying) "Speaking" else "Hear voice 🔊",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res,
                                    fontSize = 12.5.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::resolveConcern,
                    enabled = state.replyText.isNotBlank() && !state.isResolving,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isResolving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send via Shiii 💕", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectConcern(null) }) {
                    Text("Close", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun BriefingCard(
    briefing: MasterBriefing,
    isPlaying: Boolean,
    onPlayBriefing: () -> Unit,
    onRespond: () -> Unit
) {
    val colors = AppThemeManager.currentColors()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 2.dp else 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Sentiment chip & Priority Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (colors.isDark) Color(0xFF242A3A) else Color(0xFFFFF0F5),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val sentimentEmoji = when {
                            briefing.emotionalSentiment.contains("sad", ignoreCase = true) -> "🥺"
                            briefing.emotionalSentiment.contains("lone", ignoreCase = true) -> "🌧️"
                            briefing.emotionalSentiment.contains("anx", ignoreCase = true) -> "💭"
                            briefing.emotionalSentiment.contains("upset", ignoreCase = true) -> "💔"
                            else -> "🌸"
                        }
                        Text(sentimentEmoji, fontSize = 11.sp)
                        Text(
                            text = briefing.emotionalSentiment.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            color = if (colors.isDark) SoftRose else DeepViolet,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (briefing.urgencyScore >= 2.0f) CoralTender else (if (colors.isDark) Color(0xFF323B4E) else Color(0xFF8E6FB8))
                ) {
                    Text(
                        text = if (briefing.urgencyScore >= 2.0f) "Urgent Care ⚡" else "Gentle Care 🌿",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Original Mistress Quote
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (colors.isDark) Color(0xFF161922) else Color(0xFFFFF7F9),
                border = BorderStroke(0.5.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentPink
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = briefing.originalMistressText,
                        fontSize = 12.5.sp,
                        fontStyle = FontStyle.Italic,
                        color = colors.textPrimary,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shiii's Strategic Analysis
            Text(
                text = briefing.masterBriefing,
                fontSize = 13.5.sp,
                color = colors.textPrimary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Hear Briefing Audio + Comfort Her
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hear Briefing Audio Button (Plays Cute Voice)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPlaying) colors.accentPink else (if (colors.isDark) Color(0xFF242A3C) else Color(0xFFFFF0F5)),
                    border = BorderStroke(1.dp, if (isPlaying) colors.accentPink else colors.cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPlayBriefing() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = "Hear Briefing",
                            tint = if (isPlaying) Color.White else (if (colors.isDark) Color.White else DeepViolet),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPlaying) "Speaking..." else "Hear Briefing 🔊",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPlaying) Color.White else (if (colors.isDark) Color.White else DeepViolet)
                        )
                    }
                }

                Button(
                    onClick = onRespond,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPink),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Comfort Her 💕", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
