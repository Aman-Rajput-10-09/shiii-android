package com.aman.shiii_android.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.notification.AppScreen
import com.aman.shiii_android.notification.ScreenStateTracker
import com.aman.shiii_android.ui.character.DraggableShiiiOverlay
import com.aman.shiii_android.ui.theme.*

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.aman.shiii_android.ui.components.ShiiiTopBarMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    user: AuthUser,
    viewModel: ChatViewModel,
    onLogout: () -> Unit,
    onOpenGroupChat: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val colors = AppThemeManager.currentColors()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                ScreenStateTracker.updateScreen(AppScreen.PRIVATE_CHAT)
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
        ScreenStateTracker.updateScreen(AppScreen.PRIVATE_CHAT)
        viewModel.initUser(user)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shiii 💕",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.topBarTitle
                        )
                        Text(
                            text = "Accompanying Mistress ${user.displayName}",
                            fontSize = 12.sp,
                            color = colors.topBarSubtitle
                        )
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
                // Chat Dialogue History with Pull to Refresh
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(state.messages, key = { it.id }) { msg ->
                        ChatBubble(
                            message = msg,
                            colors = colors,
                            isSpeechActive = state.isSpeechActive,
                            playingAudioUrl = state.playingAudioUrl,
                            onPlayAudio = { viewModel.playMessage(msg) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                }

                // Input Bar
                Surface(
                    tonalElevation = 6.dp,
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
                            onValueChange = viewModel::onInputTextChange,
                            placeholder = { Text("Tell Shiii your feelings...", fontSize = 14.sp, color = colors.inputFieldHint) },
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

                        IconButton(
                            onClick = viewModel::sendMessage,
                            enabled = state.inputText.isNotBlank() && !state.isSending,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (state.inputText.isNotBlank()) colors.accentPink else Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Draggable Floating Shiii Overlay with Spring Bounce Drop Animation!
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
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    colors: AppThemeColors,
    isSpeechActive: Boolean,
    playingAudioUrl: String?,
    onPlayAudio: () -> Unit
) {
    val isShiii = message.senderRole == "shiii"
    val isPlayingThis = isSpeechActive && (playingAudioUrl == message.audioUrl || playingAudioUrl?.contains(message.content.take(15)) == true)

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
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isShiii) "Shiii ✨" else "You",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShiii) colors.receivedBubbleText else Color.White.copy(alpha = 0.9f)
                    )
                    if (isShiii) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPlayingThis) colors.accentPink else (if (colors.isDark) Color(0xFF2C3246) else LavenderMist.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPlayAudio() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingThis) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                                    contentDescription = "Play voice",
                                    tint = if (isPlayingThis) Color.White else colors.receivedBubbleText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isPlayingThis) "Speaking" else "Hear voice",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isPlayingThis) Color.White else colors.receivedBubbleText
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    fontSize = 13.5.sp,
                    color = if (isShiii) colors.receivedBubbleText else Color.White,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

