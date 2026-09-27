package com.aman.shiii_android.ui.character

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aman.shiii_android.R
import com.aman.shiii_android.domain.model.ChatMessage
import com.aman.shiii_android.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Interactive Draggable Floating Shiii Overlay.
 * Features:
 * - Immediate crying animation when clicked or tapped!
 * - Frequent, lively animation rotation every 4 seconds (Cycling, Walking, Waving/Talking, Idle)!
 * - Walking while speaking with footsteps & speech articulation when voice plays.
 * - Smooth drag & drop anywhere on the screen with spring physics.
 */
@Composable
fun DraggableShiiiOverlay(
    state: CharacterState,
    mouthShape: String,
    pendingSpeech: ChatMessage?,
    isPlaying: Boolean,
    onSpeechClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val isVisible by ShiiiOverlaySettings.isShiiiVisible.collectAsState()
    if (!isVisible) return

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var userScale by remember { mutableFloatStateOf(1f) }

    // Drop bounce animation (squash & stretch on landing)
    val dropScaleX = remember { Animatable(1f) }
    val dropScaleY = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Interactive Tap and Idle Animation State Machine
    var interactiveState by remember { mutableStateOf<CharacterState?>(null) }

    // Drag pickup scale
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dragScale"
    )

    // Gentle floating idle animation
    val infiniteTransition = rememberInfiniteTransition(label = "overlay_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Lively idle animation scheduler: smoothly cycles through activities every 4.5 seconds!
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            val funAnimations = listOf(
                CharacterState.CYCLING,
                CharacterState.WALKING,
                CharacterState.DANCING, // Waving & Talking
                CharacterState.HAPPY,
                CharacterState.CYCLING, // Ensure cycling appears frequently
                CharacterState.IDLE
            )
            var idx = 0
            while (isActive) {
                delay(4500L) // Frequent 4.5s transition so user sees all stickers!
                if (interactiveState != CharacterState.CRYING && !isDragging && !isPlaying) {
                    idx = (idx + 1) % funAnimations.size
                    interactiveState = funAnimations[idx]
                }
            }
        } else {
            interactiveState = null
        }
    }

    // Trigger crying immediately on tap
    val triggerCrying = {
        if (interactiveState == CharacterState.CRYING) {
            // Comfort her on second tap
            interactiveState = CharacterState.HAPPY
        } else {
            interactiveState = CharacterState.CRYING
            coroutineScope.launch {
                delay(4000)
                if (interactiveState == CharacterState.CRYING) {
                    interactiveState = null
                }
            }
        }
    }

    // Resolve active character state
    val resolvedState = when {
        interactiveState == CharacterState.CRYING -> CharacterState.CRYING
        isPlaying -> CharacterState.TALKING // Walking while saying!
        interactiveState != null -> interactiveState!!
        state == CharacterState.WALKING -> CharacterState.WALKING
        state == CharacterState.TALKING -> CharacterState.TALKING
        state == CharacterState.HAPPY -> CharacterState.HAPPY
        else -> CharacterState.IDLE
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), (offsetY + (if (!isDragging) floatY else 0f)).roundToInt()) }
            .graphicsLayer {
                scaleX = dragScale * dropScaleX.value * userScale
                scaleY = dragScale * dropScaleY.value * userScale
                rotationZ = if (isDragging) 4f else 0f
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var isDrag = false
                    var isPinching = false
                    var totalDragDist = 0f
                    val touchSlop = viewConfiguration.touchSlop
                    var previousSpan = -1f

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Main)
                        val activeChanges = event.changes.filter { it.pressed }
                        if (activeChanges.isEmpty()) {
                            // All fingers lifted!
                            if (!isDrag && !isPinching) {
                                event.changes.forEach { it.consume() }
                                triggerCrying()
                            } else if (isDrag) {
                                isDragging = false
                                event.changes.forEach { it.consume() }
                                coroutineScope.launch {
                                    dropScaleX.animateTo(1.18f, tween(80, easing = FastOutLinearInEasing))
                                    dropScaleY.animateTo(0.82f, tween(80, easing = FastOutLinearInEasing))
                                    launch {
                                        dropScaleX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                    }
                                    launch {
                                        dropScaleY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                    }
                                }
                            }
                            break
                        }

                        if (activeChanges.size >= 2) {
                            // Two fingers detected: PINCH TO ZOOM IN / OUT (small and big)!
                            isPinching = true
                            isDragging = false
                            val p0 = activeChanges[0].position
                            val p1 = activeChanges[1].position
                            val currentSpan = hypot(p0.x - p1.x, p0.y - p1.y)

                            if (previousSpan > 0f && currentSpan > 0f) {
                                val zoomChange = currentSpan / previousSpan
                                userScale = (userScale * zoomChange).coerceIn(0.45f, 3.2f)
                            }
                            previousSpan = currentSpan
                            activeChanges.forEach { it.consume() }
                        } else {
                            // Single finger: Drag tracking
                            previousSpan = -1f
                            val change = activeChanges.firstOrNull { it.id == down.id } ?: activeChanges.first()
                            val dragDelta = change.positionChange()
                            totalDragDist += hypot(dragDelta.x, dragDelta.y)

                            if (!isPinching && !isDrag && totalDragDist > touchSlop) {
                                isDrag = true
                                isDragging = true
                            }

                            if (isDrag) {
                                change.consume()
                                offsetX += dragDelta.x
                                offsetY += dragDelta.y
                            }
                        }
                    }
                }
            }
            .wrapContentSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Speech Sound Indicator Badge (active only during speech playback)
            if (isPlaying) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CherryBlossomPink,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSpeechClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speech",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "Shiii speaking... 🔊",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else if (resolvedState == CharacterState.CRYING) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralTender.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Waaah! Don't touch me! (╥﹏╥)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else if (resolvedState == CharacterState.CYCLING) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CherryBlossomPink.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Cycling Shiii! 🚲💨",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else if (resolvedState == CharacterState.WALKING) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftRose.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Out on a stroll~ 🌸",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else if (resolvedState == CharacterState.DANCING) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DeepViolet.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Yay! Hello! ✨",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftRose,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else if (resolvedState == CharacterState.HAPPY) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CherryBlossomPink.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Happy Shiii! 💕",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isDarkTheme) Color(0xFF1E2332) else Color(0xFFFFF0F5)).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDarkTheme) Color(0xFF333B52) else Color(0xFFFFD6E2)),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Shiii is with you 🌸",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) SoftRose else DeepViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // High-visibility animated Shiii avatar container (compact & petite by default)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.92f),
                                SakuraBlush.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedShiiiCharacter(
                    state = resolvedState,
                    mouthShape = mouthShape,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Super-Cute Chibi Anime Girl "Shiii".
 * Uses authentic, studio-quality anime illustration assets rendered with smooth Compose animations:
 * - IDLE: Gentle breathing and floating sparkles
 * - WALKING / TALKING: Walking while saying with bouncing footsteps, arm sway, and speech articulation
 * - CYCLING: Cute anime girl pedaling on a pink bicycle with breeze physics
 * - CRYING: Dramatic waterfall anime tears with trembling body and tear splashes
 * - DANCING / SNOOZING: Cheerful hops with musical stars / gentle sleeping bubbles
 */
@Composable
fun AnimatedShiiiCharacter(
    state: CharacterState,
    mouthShape: String, // "closed", "A", "I", "U", "E", "O"
    modifier: Modifier = Modifier
) {
    // Internal state for interactive tap-to-cry and lively animations on all screens
    var internalState by remember { mutableStateOf<CharacterState?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Lively idle rotation every 4 seconds when in IDLE
    LaunchedEffect(state) {
        if (state == CharacterState.IDLE) {
            val funAnimations = listOf(
                CharacterState.CYCLING,
                CharacterState.WALKING,
                CharacterState.DANCING, // Waving & Talking
                CharacterState.CYCLING, // Make cycling frequent
                CharacterState.HAPPY,
                CharacterState.IDLE
            )
            var idx = 0
            while (isActive) {
                delay(4000L)
                if (internalState != CharacterState.CRYING) {
                    idx = (idx + 1) % funAnimations.size
                    internalState = funAnimations[idx]
                }
            }
        } else {
            internalState = null
        }
    }

    val triggerCrying = {
        if (internalState == CharacterState.CRYING) {
            internalState = CharacterState.HAPPY
        } else {
            internalState = CharacterState.CRYING
            coroutineScope.launch {
                delay(4000)
                if (internalState == CharacterState.CRYING) {
                    internalState = null
                }
            }
        }
    }

    val effectiveState = when {
        internalState == CharacterState.CRYING -> CharacterState.CRYING
        state != CharacterState.IDLE -> state
        internalState != null -> internalState!!
        else -> CharacterState.IDLE
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shiii_anime_fx")

    // Breathing Bob
    val breathBob by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathBob"
    )

    // Walking stride / step bounce (alternating waddle)
    val walkStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walkStep"
    )

    // Cycling road bounce
    val cyclingBounce by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cyclingBounce"
    )

    // Crying rapid body tremble shake
    val cryingTremble by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cryingTremble"
    )

    // Dancing joyful hop & tilt
    val danceBounce by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "danceBounce"
    )
    val danceTilt by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "danceTilt"
    )

    // Ambient floating particle progress (0f to 1f)
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    val isTalking = effectiveState == CharacterState.TALKING || (mouthShape != "closed" && effectiveState != CharacterState.CRYING)
    val isWalking = effectiveState == CharacterState.WALKING
    val isCycling = effectiveState == CharacterState.CYCLING
    val isCrying = effectiveState == CharacterState.CRYING
    val isDancing = effectiveState == CharacterState.DANCING

    // Select the appropriate high-res anime sprite
    val drawableRes = when {
        isCrying -> R.drawable.shiii_crying
        isCycling -> R.drawable.shiii_cycling
        isTalking -> {
            val stepRad = Math.toRadians(walkStep.toDouble()).toFloat()
            if (sin(stepRad) > 0f) R.drawable.shiii_talking else R.drawable.shiii_walking
        }
        isWalking -> R.drawable.shiii_walking
        isDancing -> R.drawable.shiii_talking
        else -> R.drawable.shiii_idle
    }

    // Dynamic animation offsets
    val walkRad = Math.toRadians(walkStep.toDouble()).toFloat()
    val animatedOffsetY = when {
        isCrying -> cryingTremble * 0.4f
        isDancing -> danceBounce
        isTalking || isWalking -> -abs(sin(walkRad * 2f)) * 6.5f
        isCycling -> cyclingBounce
        else -> breathBob
    }
    val animatedOffsetX = when {
        isCrying -> cryingTremble
        isTalking || isWalking -> sin(walkRad) * 3f
        else -> 0f
    }
    val animatedRotation = when {
        isDancing -> danceTilt
        isTalking || isWalking -> sin(walkRad) * 3.5f
        isCycling -> 3f // Leaning forward into the ride!
        else -> 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                triggerCrying()
            },
        contentAlignment = Alignment.Center
    ) {
        // Overlay particle effects (Tears splash, speed lines, musical stars)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val s = size.minDimension / 100f

            when {
                isCrying -> {
                    // Crying tear splashes around her feet!
                    val tearColor = Color(0xFF48CAE4).copy(alpha = 0.85f)
                    val splashY = cy + (44f * s)
                    val splashOffset = (particleProgress * 14f * s)
                    drawCircle(tearColor.copy(alpha = 1f - particleProgress), radius = 3.5f * s, center = Offset(cx - (24f * s) - splashOffset, splashY - (splashOffset * 0.4f)))
                    drawCircle(tearColor.copy(alpha = 1f - particleProgress), radius = 3f * s, center = Offset(cx + (24f * s) + splashOffset, splashY - (splashOffset * 0.4f)))
                    drawCircle(tearColor.copy(alpha = 0.9f - particleProgress * 0.9f), radius = 4f * s, center = Offset(cx - (12f * s), splashY + (splashOffset * 0.3f)))
                    drawCircle(tearColor.copy(alpha = 0.9f - particleProgress * 0.9f), radius = 4f * s, center = Offset(cx + (12f * s), splashY + (splashOffset * 0.3f)))
                }
                isCycling -> {
                    // Wind rush speed lines behind bicycle
                    val windColor = Color(0xFFFFB3C6).copy(alpha = 0.75f)
                    val dashX = (particleProgress * 22f * s)
                    drawLine(windColor, Offset(cx - (50f * s) + dashX, cy + (10f * s)), Offset(cx - (32f * s) + dashX, cy + (10f * s)), strokeWidth = 2.5f * s)
                    drawLine(windColor, Offset(cx - (46f * s) + dashX, cy + (22f * s)), Offset(cx - (30f * s) + dashX, cy + (22f * s)), strokeWidth = 2f * s)
                }
                isDancing -> {
                    // Floating musical stars
                    val starColor = Color(0xFFFFD166).copy(alpha = 1f - (particleProgress * 0.5f))
                    val floatOffset = particleProgress * 18f * s
                    drawCircle(starColor, radius = 3f * s, center = Offset(cx - (36f * s), cy - (20f * s) - floatOffset))
                    drawCircle(Color(0xFFFF7597), radius = 2.5f * s, center = Offset(cx + (36f * s), cy - (16f * s) - floatOffset))
                }
                state == CharacterState.HAPPY -> {
                    // Floating heart
                    val heartColor = Color(0xFFFF5D8F).copy(alpha = 1f - particleProgress)
                    val heartY = cy - (24f * s) - (particleProgress * 20f * s)
                    drawCircle(heartColor, radius = 3.8f * s, center = Offset(cx + (26f * s), heartY))
                    drawCircle(heartColor, radius = 3.8f * s, center = Offset(cx + (31f * s), heartY))
                }
            }
        }

        // High-Quality Anime Girl Sprite
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Shiii Anime Girl",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize(0.95f)
                .graphicsLayer {
                    translationX = animatedOffsetX * density
                    translationY = animatedOffsetY * density
                    rotationZ = animatedRotation
                }
        )
    }
}
