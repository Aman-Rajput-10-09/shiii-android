package com.aman.shiii_android.ui.direct

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.DirectMessage
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    user: AuthUser,
    viewModel: DirectChatViewModel,
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

    // Auto scroll on new messages
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val isMaster = (user.role == UserRole.MASTER)
    val partnerDisplay = if (isMaster) {
        user.partnerName ?: "Mistress 💕"
    } else {
        user.partnerName ?: "Master 🎩"
    }
    val colors = AppThemeManager.currentColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isMaster) CherryBlossomPink.copy(alpha = 0.2f) else Color(0xFF2196F3).copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isMaster) "💕" else "🎩",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = partnerDisplay,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.topBarTitle
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Active green dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = if (colors.isDark) SoftRose else colors.accentPink,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Private Couple Channel • Real-time",
                                    fontSize = 11.sp,
                                    color = colors.topBarSubtitle
                                )
                            }
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
                // Messages List with Pull To Refresh
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (state.messages.isEmpty() && !state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("💌", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Your Private Couple Haven",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Only you and $partnerDisplay are here.\nDirect, private, and real-time.",
                                            fontSize = 13.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(state.messages, key = { it.id }) { msg ->
                            val isCurrentUser = (msg.senderRole != null && msg.senderRole == user.role.value) ||
                                    (msg.senderId != 0 && msg.senderId == user.id)

                            DirectMessageBubble(
                                message = msg,
                                isCurrentUser = isCurrentUser,
                                userRole = user.role,
                                partnerName = partnerDisplay,
                                colors = colors
                            )
                        }
                    }
                }

                // Bottom Input Area
                DirectChatInputBar(
                    text = state.inputText,
                    onTextChanged = { viewModel.onInputTextChanged(it) },
                    onSend = { viewModel.sendMessage() },
                    isSending = state.isSending,
                    partnerName = partnerDisplay,
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun DirectMessageBubble(
    message: DirectMessage,
    isCurrentUser: Boolean,
    userRole: UserRole,
    partnerName: String,
    colors: AppThemeColors
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }
    val isMaster = (userRole == UserRole.MASTER)

    // Bubble alignment: Current user is on RIGHT, Partner is on LEFT!
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 285.dp)
        ) {
            // Partner sender label on top of left bubble
            if (!isCurrentUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp, bottom = 3.dp)
                ) {
                    Text(
                        text = if (isMaster) "💕 $partnerName" else "🎩 $partnerName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMaster) CoralTender else (if (colors.isDark) Color(0xFF64B5F6) else Color(0xFF1976D2))
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isCurrentUser) 16.dp else 3.dp,
                    bottomEnd = if (isCurrentUser) 3.dp else 16.dp
                ),
                color = if (isCurrentUser) colors.sentBubbleBg else colors.receivedBubbleBg,
                border = if (!isCurrentUser) BorderStroke(1.dp, colors.receivedBubbleBorder) else null,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                ) {
                    // Message content
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = if (isCurrentUser) colors.sentBubbleText else colors.receivedBubbleText,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // WhatsApp-style bottom right row: Time + Checkmarks
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = if (isCurrentUser) Color.White.copy(alpha = 0.78f) else colors.textSecondary
                        )

                        // WhatsApp-style Sent & Seen receipts for sender
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            if (message.isRead) {
                                // WhatsApp signature double blue ticks (Seen / Read)
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Seen",
                                    tint = Color(0xFF34B7F1), // WhatsApp signature cyan blue
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                // Single gray/white tick (Sent)
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    partnerName: String,
    colors: AppThemeColors
) {
    Surface(
        color = colors.inputBarBg,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        "Message $partnerName...",
                        fontSize = 14.sp,
                        color = colors.inputFieldHint
                    )
                },
                modifier = Modifier
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.inputFieldText,
                    unfocusedTextColor = colors.inputFieldText,
                    focusedBorderColor = colors.accentPink,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedContainerColor = colors.inputFieldBg,
                    unfocusedContainerColor = colors.inputFieldBg
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isSending) {
                            colors.accentPink
                        } else {
                            Color.LightGray.copy(alpha = 0.5f)
                        }
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
