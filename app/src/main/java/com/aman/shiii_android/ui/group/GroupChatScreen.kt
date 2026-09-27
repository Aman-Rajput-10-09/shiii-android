package com.aman.shiii_android.ui.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.GroupMessage
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.ui.character.DraggableShiiiOverlay
import com.aman.shiii_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    user: AuthUser,
    viewModel: GroupChatViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onScreenPaused()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(user) {
        viewModel.initUser(user)
    }

    // Auto-scroll on new message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val isMistress = (user.role == UserRole.MISTRESS)
    val colors = AppThemeManager.currentColors()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.accentPink
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = colors.accentPink.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🕊️", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Couple Peace Lounge",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.topBarTitle
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.accentPink.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "3-Way",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accentPink,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Master 🎩 + Mistress 💕 + Shiii 🌸",
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
                // Peacemaker Banner
                Surface(
                    color = if (colors.isDark) Color(0xFF161922) else Color(0xFFFFF0F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌸", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shiii is listening to both of you to cool tempers & keep love shining!",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Quick Peacemaking Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val promptStarters = listOf(
                        "Shiii, please help us solve this 💕",
                        "I'm sorry, I was just tired earlier 🌸",
                        "I love you, let's not fight 🕊️",
                        "Can you explain your feelings to me? ✨"
                    )
                    promptStarters.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.chipBg,
                            border = BorderStroke(1.dp, colors.chipBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.onInputTextChanged(prompt) }
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = colors.chipText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Messages Stream with Pull To Refresh
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.messages.isEmpty() && !state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
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
                                            Text("🕊️ Welcome to Couple Peace Lounge 💕", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Talk freely with your partner! If either of you feels upset, misunderstood, or angry, Shiii will step in to cool things down with sweet diplomacy.",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(state.messages, key = { it.id }) { msg ->
                            val isCurrentUser = (msg.senderId == user.id) || (msg.senderRole == user.role.value && msg.senderRole != "shiii")
                            val isShiii = msg.senderRole == "shiii"
                            val isMaster = msg.senderRole == "master"
                            val isMistress = msg.senderRole == "mistress"

                            GroupMessageBubble(
                                message = msg,
                                isCurrentUser = isCurrentUser,
                                isShiii = isShiii,
                                isMaster = isMaster,
                                isMistress = isMistress
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(70.dp))
                        }
                    }
                }

                // Chat Input Bar
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
                            value = state.inputText,
                            onValueChange = viewModel::onInputTextChanged,
                            placeholder = { Text("Speak in English or Hindi...", fontSize = 13.sp, color = colors.inputFieldHint) },
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
                            onClick = viewModel::sendMessage,
                            enabled = state.inputText.isNotBlank() && !state.isSending,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentPink),
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.size(46.dp)
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Draggable & Pinch-to-zoom Shiii Overlay (pure animation, no voice in 3-way lounge)
            DraggableShiiiOverlay(
                state = state.characterState,
                mouthShape = state.mouthShape,
                pendingSpeech = null,
                isPlaying = false,
                onSpeechClick = {},
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                isDarkTheme = colors.isDark
            )
        }
    }
}

@Composable
private fun GroupMessageBubble(
    message: GroupMessage,
    isCurrentUser: Boolean,
    isShiii: Boolean,
    isMaster: Boolean,
    isMistress: Boolean
) {
    val colors = AppThemeManager.currentColors()

    val bubbleColor = when {
        isShiii -> if (colors.isDark) Color(0xFF1E2332) else Color(0xFFFFF5F8)
        isCurrentUser -> colors.sentBubbleBg
        isMaster -> if (colors.isDark) Color(0xFF1A2234) else Color(0xFFF0F4FF)
        else -> if (colors.isDark) Color(0xFF281E2E) else Color(0xFFFFF0F5)
    }

    val bubbleBorder = when {
        isShiii -> BorderStroke(1.5.dp, if (colors.isDark) colors.accentPink.copy(alpha = 0.7f) else Color(0xFFFFB6C1))
        isCurrentUser -> null
        isMaster -> BorderStroke(1.dp, if (colors.isDark) Color(0xFF384A6E) else Color(0xFFD0DCFF))
        else -> BorderStroke(1.dp, if (colors.isDark) Color(0xFF5A3654) else Color(0xFFFFD6E2))
    }

    val textColor = when {
        isCurrentUser -> colors.sentBubbleText
        isShiii -> if (colors.isDark) Color.White else DeepViolet
        colors.isDark -> Color.White
        else -> DeepViolet
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = when {
            isShiii -> Arrangement.Center
            isCurrentUser -> Arrangement.End
            else -> Arrangement.Start
        }
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isCurrentUser) 18.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 18.dp
            ),
            color = bubbleColor,
            border = bubbleBorder,
            shadowElevation = if (colors.isDark) 1.dp else (if (isShiii) 4.dp else 2.dp),
            modifier = Modifier.widthIn(max = if (isShiii) 320.dp else 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header (Sender Name + Badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                isShiii -> "🌸 Shiii (Peacemaker)"
                                isMaster -> "🎩 ${message.senderName}"
                                else -> "💕 ${message.senderName}"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCurrentUser -> Color.White.copy(alpha = 0.9f)
                                isShiii -> if (colors.isDark) SoftRose else DeepViolet
                                isMaster -> if (colors.isDark) Color(0xFF8BB1FF) else Color(0xFF2B54AA)
                                else -> if (colors.isDark) SoftRose else Color(0xFFC73E67)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.content,
                    fontSize = 13.5.sp,
                    color = textColor,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
