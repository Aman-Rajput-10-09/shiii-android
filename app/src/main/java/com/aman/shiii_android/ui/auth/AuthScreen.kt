package com.aman.shiii_android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aman.shiii_android.domain.model.AuthUser
import com.aman.shiii_android.domain.model.UserRole
import com.aman.shiii_android.ui.character.AnimatedShiiiCharacter
import com.aman.shiii_android.ui.character.CharacterState
import com.aman.shiii_android.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.authenticatedUser) {
        val user = state.authenticatedUser
        if (user != null) {
            viewModel.consumeAuth()
            onAuthSuccess(user)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SakuraBlush, LavenderMist, Color(0xFFF0E6FF))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Little Shiii Greeting Avatar
            AnimatedShiiiCharacter(
                state = CharacterState.IDLE,
                mouthShape = if (state.isLoading) "O" else "closed",
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Shiii (シー)",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DeepViolet
            )
            Text(
                text = "Your Little Diplomatic Couple Emissary 💕",
                fontSize = 14.sp,
                color = TextDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.isRegisterMode) "Create Account" else "Welcome Back",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepViolet
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Username") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.isRegisterMode) {
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.displayName,
                            onValueChange = viewModel::onDisplayNameChange,
                            label = { Text("Display Name (e.g. My Queen / Papa)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Who are you?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedRole == UserRole.MISTRESS,
                                onClick = { viewModel.onRoleChange(UserRole.MISTRESS) },
                                label = { Text("Mistress 👑") }
                            )
                            FilterChip(
                                selected = state.selectedRole == UserRole.MASTER,
                                onClick = { viewModel.onRoleChange(UserRole.MASTER) },
                                label = { Text("Master 🎩") }
                            )
                        }
                    }

                    state.error?.let { err ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = err,
                            color = CoralTender,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CherryBlossomPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = if (state.isRegisterMode) "Register" else "Enter",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = viewModel::toggleMode) {
                        Text(
                            text = if (state.isRegisterMode) "Already have an account? Sign In" else "New to Shiii? Create Account",
                            color = DeepViolet,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
