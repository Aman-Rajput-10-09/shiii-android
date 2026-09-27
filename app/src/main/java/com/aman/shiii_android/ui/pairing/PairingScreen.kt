package com.aman.shiii_android.ui.pairing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.ui.character.AnimatedShiiiCharacter
import com.aman.shiii_android.ui.character.CharacterState
import com.aman.shiii_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    user: AuthUser,
    viewModel: PairingViewModel,
    onContinue: (AuthUser) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(user) {
        viewModel.initUser(user)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Couple Bonding 💕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepViolet
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Switch Account", color = DeepViolet, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SakuraBlush)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SakuraBlush, Color.White, LavenderMist.copy(alpha = 0.5f))
                    )
                )
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Little Shiii Mascot
            AnimatedShiiiCharacter(
                state = if (state.isPaired) CharacterState.HAPPY else CharacterState.IDLE,
                mouthShape = if (state.isPairing) "O" else "closed",
                modifier = Modifier.size(130.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val roleBadge = if (user.role == UserRole.MASTER) "🎩 Master Mode" else "👑 Mistress Mode"
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SoftRose.copy(alpha = 0.35f),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = roleBadge,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepViolet,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "Welcome, ${user.displayName}!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DeepViolet
            )

            Text(
                text = if (state.isPaired)
                    "You and ${state.partnerName ?: "your partner"} are happily linked! 💕"
                else
                    "Connect with your ${if (user.role == UserRole.MASTER) "Mistress" else "Master"} to activate Shiii's diplomatic channel.",
                fontSize = 13.sp,
                color = TextDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (state.isPaired) {
                // Paired Celebration Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Paired",
                            tint = SoftRose,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Linked with ${state.partnerName} ✨",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepViolet
                        )
                        Text(
                            text = "Shiii is ready to ferry sweet messages and smooth out any worries between you two.",
                            fontSize = 12.sp,
                            color = TextDark.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onContinue(
                                    user.copy(
                                        isPaired = true,
                                        partnerName = state.partnerName,
                                        coupleId = state.coupleId
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CherryBlossomPink),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enter Shiii's World 💕", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Share My Pair Code Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Secret Invite Code",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepViolet
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SakuraBlush.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (state.isLoading) "Loading..." else state.myPairCode.ifBlank { "SHIII-...." },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = DeepViolet,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                if (state.myPairCode.isNotBlank()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Shiii Pair Code", state.myPairCode))
                                    Toast.makeText(context, "Pair code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    viewModel.setCopied(true)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (state.codeCopied) Icons.Default.Check else Icons.Default.Share,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (state.codeCopied) "Code Copied!" else "Copy Pair Code")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enter Partner Code Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Or Enter Partner's Code",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepViolet
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.partnerCodeInput,
                            onValueChange = viewModel::onPartnerCodeChanged,
                            placeholder = { Text("e.g. SHIII-7X9K", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = err,
                                color = CoralTender,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.pairWithPartner { partnerName ->
                                    Toast.makeText(context, "Connected with $partnerName! 💕", Toast.LENGTH_LONG).show()
                                    onContinue(
                                        user.copy(
                                            isPaired = true,
                                            partnerName = partnerName,
                                            coupleId = state.coupleId
                                        )
                                    )
                                }
                            },
                            enabled = state.partnerCodeInput.isNotBlank() && !state.isPairing,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CherryBlossomPink),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isPairing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Link Hearts ✨", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Skip / Continue Solo Option
                TextButton(
                    onClick = {
                        onContinue(
                            user.copy(
                                isPaired = false,
                                pairCode = state.myPairCode
                            )
                        )
                    }
                ) {
                    Text(
                        text = "Skip for Now (Continue Solo) ➔",
                        color = DeepViolet.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
