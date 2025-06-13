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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.presentation.common.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
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
            .background(LemonLight) // Updated to lemon light background
            .border(1.dp, YieldColor, RoundedCornerShape(12.dp)) // Updated border to YieldColor
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Kapil Agro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryAccent
            )
            Text(
                "Scouting Hub",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, SowingColor, RoundedCornerShape(12.dp)), // Updated border to YieldColor
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LemonLight // Updated to lemon light background
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
                        color = TextPrimary
                    )
                    Text(
                        "Enter your credentials to access your account",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        placeholder = { Text("example@email.com", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (email.isNotEmpty()) PrimaryAccent else TextSecondary // Updated to YieldColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = WarningAccent,
                            focusedBorderColor = TeamIcon, // Updated to YieldColor
                            unfocusedBorderColor = TextSecondary,
                            errorBorderColor = ErrorAccent,
                            unfocusedPlaceholderColor = TextSecondary,
                            focusedLeadingIconColor = YieldColor, // Updated to YieldColor
                            unfocusedLeadingIconColor = TextSecondary,
                            focusedContainerColor = LemonLight.copy(alpha = 0.5f), // Updated to LemonLight
                            unfocusedContainerColor = LemonLight.copy(alpha = 0.5f), // Updated to LemonLight
                            errorContainerColor = LemonLight.copy(alpha = 0.5f) // Updated to LemonLight
                        ),
                        isError = isError
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.trim() },
                        placeholder = { Text("password", color = TextSecondary) },
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
                                tint = if (password.isNotEmpty()) PrimaryAccent else TextSecondary // Updated to YieldColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (passwordVisible) PrimaryAccent else TextSecondary, // Updated to YieldColor
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = WarningAccent,
                            focusedBorderColor = TeamIcon, // Updated to YieldColor
                            unfocusedBorderColor = TextSecondary,
                            errorBorderColor = ErrorAccent,
                            unfocusedPlaceholderColor = TextSecondary,
                            focusedLeadingIconColor = YieldColor, // Updated to YieldColor
                            unfocusedLeadingIconColor = TextSecondary,
                            focusedTrailingIconColor = YieldColor, // Updated to YieldColor
                            unfocusedTrailingIconColor = TextSecondary,
                            focusedContainerColor = LemonLight.copy(alpha = 0.5f), // Updated to LemonLight
                            unfocusedContainerColor = LemonLight.copy(alpha = 0.5f), // Updated to LemonLight
                            errorContainerColor = LemonLight.copy(alpha = 0.5f) // Updated to LemonLight
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
                                color = TeamIcon, // Updated to YieldColor
                                fontSize = 12.sp
                            )
                        }
                        if (isError) {
                            val errorMessage = (loginState as AuthViewModel.LoginState.Error).message
                            Text(
                                text = errorMessage,
                                color = ErrorAccent,
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
                            .border(0.5.dp, YieldColor, RoundedCornerShape(8.dp)), // Updated border to YieldColor
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TeamIcon, // Updated to YieldColor
                            contentColor = White,
                            disabledContainerColor = TextSecondary.copy(alpha = 0.5f),
                            disabledContentColor = TextSecondary
                        )
                    ) {
                        if (loginState is AuthViewModel.LoginState.Loading) {
                            CircularProgressIndicator(
                                color = White,
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
                color = TextSecondary.copy(alpha = 0.3f),
                thickness = 0.5.dp,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2025 Kapil Agro Scouting Hub. All Rights Reserved.",
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 12.sp
            )
        }
    }
}