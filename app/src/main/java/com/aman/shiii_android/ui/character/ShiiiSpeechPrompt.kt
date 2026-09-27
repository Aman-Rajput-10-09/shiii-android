package com.aman.shiii_android.ui.character

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.ui.theme.*

/**
 * Interactive "May I speak? 💬" prompt & dialogue card.
 *
 * Ensures Shiii NEVER blurts out speech uninvited; instead:
 * - Shows an animated "May I say? 💬" badge when Shiii has something to report or reply.
 * - When tapped, triggers Shiii's voice and displays the speech in full text so no word is missed.
 * - Allows the user to pause, replay, or close at will.
 */
@Composable
fun ShiiiSpeechPrompt(
    pendingSpeech: ChatMessage?,
    isPlaying: Boolean,
    onPlayOrToggle: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    if (pendingSpeech == null) return

    var isExpanded by remember(pendingSpeech.id) { mutableStateOf(false) }

    // Pulsing animation for the "May I say?" badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating "May I say?" Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isPlaying) CherryBlossomPink else (if (isDarkTheme) DeepViolet else Color.White),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .scale(if (!isExpanded && !isPlaying) pulseScale else 1f)
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    if (!isExpanded) isExpanded = true
                    onPlayOrToggle()
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon indicator
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) Color.White.copy(alpha = 0.3f)
                            else CherryBlossomPink.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                        contentDescription = "Voice Prompt",
                        tint = if (isPlaying) Color.White else CherryBlossomPink,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isPlaying) "Shiii is speaking... 🔊"
                               else if (isExpanded) "Shiii: Tap to listen again 🔁"
                               else "May I speak? 💬🎀",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color.White
                               else (if (isDarkTheme) SoftRose else DeepViolet)
                    )
                    Text(
                        text = if (isPlaying) "Tap to pause" else "Tap to listen & read words",
                        fontSize = 10.sp,
                        color = if (isPlaying) Color.White.copy(alpha = 0.85f)
                               else (if (isDarkTheme) LavenderMist.copy(alpha = 0.8f) else TextDark.copy(alpha = 0.6f))
                    )
                }

                // Expand/collapse toggle arrow or dismiss
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        fontSize = 11.sp,
                        color = if (isPlaying) Color.White else CherryBlossomPink
                    )
                }
            }
        }

        // Expanded Speech Dialogue Card (Shows full text so no word is missed!)
        AnimatedVisibility(
            visible = isExpanded || isPlaying,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CardDark.copy(alpha = 0.95f) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Shiii 💕",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CherryBlossomPink
                            )
                            if (isPlaying) {
                                Text(
                                    text = "• Speaking now",
                                    fontSize = 10.sp,
                                    color = MintPastel,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Replay / Pause icon
                            IconButton(
                                onClick = onPlayOrToggle,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Refresh,
                                    contentDescription = "Toggle Audio",
                                    tint = CherryBlossomPink,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // Dismiss icon
                            IconButton(
                                onClick = {
                                    isExpanded = false
                                    onDismiss()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = if (isDarkTheme) LavenderMist else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Readable Message Content
                    Text(
                        text = pendingSpeech.content,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = if (isDarkTheme) TextLight else TextDark,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPlayOrToggle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) CoralTender else CherryBlossomPink
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPlaying) "Pause" else "Speak to me 💬",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
