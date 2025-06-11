package com.kapilagro.sasyak.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Define new colors
    val SoftTeal = Color(0xFF40C4FF)
    val NeutralGray = Color(0xFFF5F7FA)
    val PrimaryText = Color(0xFF1A1A1A)
    val SecondaryText = Color(0xFF6B7280)
    val AccentBlue = Color(0xFF3B82F6)
    val CursorOrange = Color(0xFFF97316)
    val ButtonText = Color(0xFFFFFFFF)
    val ErrorRed = Color(0xFFEF4444)
    val OffWhite = Color(0xFFF9FAFB)

    val loginState by viewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isError = loginState is AuthViewModel.LoginState.Error

    LaunchedEffect(loginState) {
        if (loginState is AuthViewModel.LoginState.Success) {
            onLoginSuccess()
            viewModel.clearLoginState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .border(1.dp, SoftTeal, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Kapil Agro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                "Scouting Hub",
                fontSize = 14.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, SoftTeal, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralGray
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Text(
                        "Enter your credentials to access your account",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        placeholder = { Text("example@email.com", color = SecondaryText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (email.isNotEmpty()) AccentBlue else SecondaryText
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            cursorColor = CursorOrange,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = SecondaryText,
                            errorBorderColor = ErrorRed,
                            unfocusedPlaceholderColor = SecondaryText,
                            focusedLeadingIconColor = AccentBlue,
                            unfocusedLeadingIconColor = SecondaryText,
                            focusedContainerColor = NeutralGray.copy(alpha = 0.5f),
                            unfocusedContainerColor = NeutralGray.copy(alpha = 0.5f),
                            errorContainerColor = NeutralGray.copy(alpha = 0.5f)
                        ),
                        isError = isError
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.trim() },
                        placeholder = { Text("password", color = SecondaryText) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (password.isNotEmpty()) AccentBlue else SecondaryText
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (passwordVisible) AccentBlue else SecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            cursorColor = CursorOrange,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = SecondaryText,
                            errorBorderColor = ErrorRed,
                            unfocusedPlaceholderColor = SecondaryText,
                            focusedLeadingIconColor = AccentBlue,
                            unfocusedLeadingIconColor = SecondaryText,
                            focusedTrailingIconColor = AccentBlue,
                            unfocusedTrailingIconColor = SecondaryText,
                            focusedContainerColor = NeutralGray.copy(alpha = 0.5f),
                            unfocusedContainerColor = NeutralGray.copy(alpha = 0.5f),
                            errorContainerColor = NeutralGray.copy(alpha = 0.5f)
                        ),
                        isError = isError
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { /* TODO: Forgot Password */ }) {
                            Text(
                                "Forgot password?",
                                color = AccentBlue,
                                fontSize = 12.sp
                            )
                        }
                        if (isError) {
                            val errorMessage = (loginState as AuthViewModel.LoginState.Error).message
                            Text(
                                text = errorMessage,
                                color = ErrorRed,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.login(email.trim(), password.trim())
                        },
                        enabled = email.isNotBlank() && password.isNotBlank() && loginState !is AuthViewModel.LoginState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(0.5.dp, SoftTeal, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = ButtonText,
                            disabledContainerColor = SecondaryText.copy(alpha = 0.5f),
                            disabledContentColor = SecondaryText
                        )
                    ) {
                        if (loginState is AuthViewModel.LoginState.Loading) {
                            CircularProgressIndicator(
                                color = ButtonText,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Sign In", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(
                color = SecondaryText.copy(alpha = 0.3f),
                thickness = 0.5.dp,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2025 Kapil Agro Scouting Hub. All Rights Reserved.",
                fontSize = 10.sp,
                color = SecondaryText,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 12.sp
            )
        }
    }
}