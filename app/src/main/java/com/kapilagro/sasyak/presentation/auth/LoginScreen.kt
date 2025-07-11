//package com.kapilagro.sasyak.presentation.auth
//
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Email
//import androidx.compose.material.icons.filled.Lock
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.kapilagro.sasyak.presentation.common.theme.AgroLight
//import androidx.hilt.navigation.compose.hiltViewModel
//
////fun LoginScreen(
////    onLoginSuccess: () -> Unit,
////    viewModel: AuthViewModel = hiltViewModel()
////) {
////    val loginState by viewModel.loginState.collectAsState()
////    var email by remember { mutableStateOf("") }
////    var password by remember { mutableStateOf("") }
////    var passwordVisible by remember { mutableStateOf(false) }
////
////    val colorScheme = MaterialTheme.colorScheme
////
////    LaunchedEffect(loginState) {
////        if (loginState is AuthViewModel.LoginState.Success) {
////            onLoginSuccess()
////            viewModel.clearLoginState()
////        }
////    }
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(colorScheme.background)
////            .padding(horizontal = 16.dp),
////        contentAlignment = Alignment.Center
////    ) {
////        Column(horizontalAlignment = Alignment.CenterHorizontally) {
////            Text(
////                "Kapil Agro",
////                fontSize = 26.sp,
////                fontWeight = FontWeight.Bold,
////                color = colorScheme.onBackground
////            )
////            Text(
////                "Scouting Hub",
////                fontSize = 14.sp,
////                color = colorScheme.primary
////            )
////
////            Spacer(modifier = Modifier.height(16.dp))
////
////            Card(
////                modifier = Modifier.fillMaxWidth(),
////                shape = RoundedCornerShape(16.dp),
////                elevation = CardDefaults.cardElevation(8.dp),
////                colors = CardDefaults.cardColors(
////                    containerColor = AgroLight,
////                    contentColor = colorScheme.onSurface
////                )
////            ) {
////                Column(
////                    modifier = Modifier
////                        .padding(16.dp)
////                        .verticalScroll(rememberScrollState()),
////                    horizontalAlignment = Alignment.CenterHorizontally
////                ) {
////                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
////                    Text(
////                        "Enter your credentials to access your account",
////                        fontSize = 12.sp,
////                        color = colorScheme.onSurfaceVariant
////                    )
////
////                    Spacer(modifier = Modifier.height(16.dp))
////
////                    Text(
////                        "Email",
////                        fontSize = 12.sp,
////                        fontWeight = FontWeight.Medium,
////                        modifier = Modifier.align(Alignment.Start)
////                    )
////                    OutlinedTextField(
////                        value = email,
////                        onValueChange = { email = it.trim() }, // 🔧 trim() applied here
////                        placeholder = { Text("example@email.com") },
////                        singleLine = true,
////                        keyboardOptions = KeyboardOptions.Default.copy(
////                            keyboardType = KeyboardType.Email,
////                            imeAction = ImeAction.Next
////                        ),
////                        leadingIcon = {
////                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
////                        },
////                        modifier = Modifier.fillMaxWidth(),
////                        shape = RoundedCornerShape(8.dp),
////                        colors = OutlinedTextFieldDefaults.colors(
////                            focusedBorderColor = colorScheme.primary,
////                            unfocusedBorderColor = colorScheme.outline,
////                            focusedLabelColor = colorScheme.primary
////                        )
////                    )
////
////                    Spacer(modifier = Modifier.height(12.dp))
////
////                    Text(
////                        "Password",
////                        fontSize = 12.sp,
////                        fontWeight = FontWeight.Medium,
////                        modifier = Modifier.align(Alignment.Start)
////                    )
////                    OutlinedTextField(
////                        value = password,
////                        onValueChange = { password = it.trim() }, // 🔧 trim() applied here too (optional but helpful)
////                        placeholder = { Text("password") },
////                        singleLine = true,
////                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
////                        keyboardOptions = KeyboardOptions.Default.copy(
////                            keyboardType = KeyboardType.Password,
////                            imeAction = ImeAction.Done
////                        ),
////                        leadingIcon = {
////                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
////                        },
////                        trailingIcon = {
////                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
////                                Icon(
////                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
////                                    contentDescription = null,
////                                    modifier = Modifier.size(18.dp)
////                                )
////                            }
////                        },
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .height(56.dp),
////                        shape = RoundedCornerShape(10.dp),
////                        colors = OutlinedTextFieldDefaults.colors(
////                            focusedBorderColor = colorScheme.primary,
////                            unfocusedBorderColor = colorScheme.outline
////                        )
////                    )
////
////                    Spacer(modifier = Modifier.height(12.dp))
////
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.End
////                    ) {
////                        TextButton(onClick = { /* TODO: Forgot Password */ }) {
////                            Text(
////                                "Forgot password?",
////                                color = colorScheme.primary,
////                                fontSize = 12.sp
////                            )
////                        }
////                    }
////
////                    Spacer(modifier = Modifier.height(16.dp))
////
////                    Button(
////                        onClick = {
////                            viewModel.login(email.trim(), password.trim()) // 🔧 ensures trimmed values sent
////                        },
////                        enabled = email.isNotBlank() && password.isNotBlank() && loginState !is AuthViewModel.LoginState.Loading,
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .height(45.dp),
////                        shape = RoundedCornerShape(10.dp),
////                        colors = ButtonDefaults.buttonColors(
////                            containerColor = colorScheme.primary,
////                            contentColor = colorScheme.onPrimary
////                        )
////                    ) {
////                        if (loginState is AuthViewModel.LoginState.Loading) {
////                            CircularProgressIndicator(
////                                color = colorScheme.onPrimary,
////                                modifier = Modifier.size(18.dp)
////                            )
////                        } else {
////                            Text("Sign in", fontSize = 14.sp)
////                        }
////                    }
////                }
////            }
////
////            Spacer(modifier = Modifier.height(16.dp))
////            Text(
////                text = "© 2025 Kapil Agro Scouting Hub. All Rights Reserved.",
////                fontSize = 10.sp,
////                color = colorScheme.onSurfaceVariant,
////                modifier = Modifier.align(Alignment.CenterHorizontally),
////                lineHeight = 12.sp
////            )
////        }
////    }
////}
//
//
//
//@Composable
//fun LoginScreen(
//    onLoginSuccess: () -> Unit,
//    viewModel: AuthViewModel = hiltViewModel()
//) {
//    val loginState by viewModel.loginState.collectAsState()
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//    val focusManager = LocalFocusManager.current
//
//    val colorScheme = MaterialTheme.colorScheme
//
//    LaunchedEffect(loginState) {
//        if (loginState is AuthViewModel.LoginState.Success) {
//            onLoginSuccess()
//            viewModel.clearLoginState()
//        }
//    }
//
//    // Handle back button to dismiss keyboard
//    /*BackHandler {
//        focusManager.clearFocus()
//    }*/
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(colorScheme.background)
//            .padding(horizontal = 16.dp)
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null
//            ) {
//                focusManager.clearFocus() // Dismiss keyboard on tap
//            },
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(
//                "Kapil Agro",
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                color = colorScheme.onBackground
//            )
//            Text(
//                "Scouting Hub",
//                fontSize = 14.sp,
//                color = colorScheme.primary
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp),
//                elevation = CardDefaults.cardElevation(8.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = AgroLight,
//                    contentColor = colorScheme.onSurface
//                )
//            ) {
//                Column(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .verticalScroll(rememberScrollState()),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//                    Text(
//                        "Enter your credentials to access your account",
//                        fontSize = 12.sp,
//                        color = colorScheme.onSurfaceVariant
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        "Email",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.align(Alignment.Start)
//                    )
//                    OutlinedTextField(
//                        value = email,
//                        onValueChange = { email = it.trim() },
//                        placeholder = { Text("example@email.com") },
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Email,
//                            imeAction = ImeAction.Next
//                        ),
//                        leadingIcon = {
//                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = colorScheme.primary,
//                            unfocusedBorderColor = colorScheme.outline,
//                            focusedLabelColor = colorScheme.primary
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Text(
//                        "Password",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.align(Alignment.Start)
//                    )
//                    OutlinedTextField(
//                        value = password,
//                        onValueChange = { password = it.trim() },
//                        placeholder = { Text("password") },
//                        singleLine = true,
//                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Password,
//                            imeAction = ImeAction.Done
//                        ),
//                        leadingIcon = {
//                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
//                        },
//                        trailingIcon = {
//                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                                Icon(
//                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp)
//                                )
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = colorScheme.primary,
//                            unfocusedBorderColor = colorScheme.outline
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.End
//                    ) {
//                        TextButton(onClick = { /* TODO: Forgot Password */ }) {
//                            Text(
//                                "Forgot password?",
//                                color = colorScheme.primary,
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    if (loginState is AuthViewModel.LoginState.Error) {
//                        val errorMessage = (loginState as AuthViewModel.LoginState.Error).message
//                        Text(
//                            text = errorMessage,
//                            color = MaterialTheme.colorScheme.error,
//                            fontSize = 12.sp,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(bottom = 8.dp)
//                        )
//                    }
//
//                    Button(
//                        onClick = {
//                            viewModel.login(email.trim(), password.trim())
//                        },
//                        enabled = email.isNotBlank() && password.isNotBlank() && loginState !is AuthViewModel.LoginState.Loading,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(45.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = colorScheme.primary,
//                            contentColor = colorScheme.onPrimary
//                        )
//                    ) {
//                        if (loginState is AuthViewModel.LoginState.Loading) {
//                            CircularProgressIndicator(
//                                color = colorScheme.onPrimary,
//                                modifier = Modifier.size(18.dp)
//                            )
//                        } else {
//                            Text("Sign in", fontSize = 14.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = "© 2025 Kapil Agro Scouting Hub. All Rights Reserved.",
//                fontSize = 10.sp,
//                color = colorScheme.onSurfaceVariant,
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                lineHeight = 12.sp
//            )
//        }
//    }
//}
package com.kapilagro.sasyak.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapilagro.sasyak.presentation.common.theme.AgroLight
import androidx.hilt.navigation.compose.hiltViewModel

//fun LoginScreen(
//    onLoginSuccess: () -> Unit,
//    viewModel: AuthViewModel = hiltViewModel()
//) {
//    val loginState by viewModel.loginState.collectAsState()
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//
//    val colorScheme = MaterialTheme.colorScheme
//
//    LaunchedEffect(loginState) {
//        if (loginState is AuthViewModel.LoginState.Success) {
//            onLoginSuccess()
//            viewModel.clearLoginState()
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(colorScheme.background)
//            .padding(horizontal = 16.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(
//                "Kapil Agro",
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                color = colorScheme.onBackground
//            )
//            Text(
//                "Scouting Hub",
//                fontSize = 14.sp,
//                color = colorScheme.primary
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp),
//                elevation = CardDefaults.cardElevation(8.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = AgroLight,
//                    contentColor = colorScheme.onSurface
//                )
//            ) {
//                Column(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .verticalScroll(rememberScrollState()),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//                    Text(
//                        "Enter your credentials to access your account",
//                        fontSize = 12.sp,
//                        color = colorScheme.onSurfaceVariant
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        "Email",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.align(Alignment.Start)
//                    )
//                    OutlinedTextField(
//                        value = email,
//                        onValueChange = { email = it.trim() }, // 🔧 trim() applied here
//                        placeholder = { Text("example@email.com") },
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Email,
//                            imeAction = ImeAction.Next
//                        ),
//                        leadingIcon = {
//                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = colorScheme.primary,
//                            unfocusedBorderColor = colorScheme.outline,
//                            focusedLabelColor = colorScheme.primary
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Text(
//                        "Password",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.align(Alignment.Start)
//                    )
//                    OutlinedTextField(
//                        value = password,
//                        onValueChange = { password = it.trim() }, // 🔧 trim() applied here too (optional but helpful)
//                        placeholder = { Text("password") },
//                        singleLine = true,
//                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Password,
//                            imeAction = ImeAction.Done
//                        ),
//                        leadingIcon = {
//                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
//                        },
//                        trailingIcon = {
//                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                                Icon(
//                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp)
//                                )
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = colorScheme.primary,
//                            unfocusedBorderColor = colorScheme.outline
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.End
//                    ) {
//                        TextButton(onClick = { /* TODO: Forgot Password */ }) {
//                            Text(
//                                "Forgot password?",
//                                color = colorScheme.primary,
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Button(
//                        onClick = {
//                            viewModel.login(email.trim(), password.trim()) // 🔧 ensures trimmed values sent
//                        },
//                        enabled = email.isNotBlank() && password.isNotBlank() && loginState !is AuthViewModel.LoginState.Loading,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(45.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = colorScheme.primary,
//                            contentColor = colorScheme.onPrimary
//                        )
//                    ) {
//                        if (loginState is AuthViewModel.LoginState.Loading) {
//                            CircularProgressIndicator(
//                                color = colorScheme.onPrimary,
//                                modifier = Modifier.size(18.dp)
//                            )
//                        } else {
//                            Text("Sign in", fontSize = 14.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = "© 2025 Kapil Agro Scouting Hub. All Rights Reserved.",
//                fontSize = 10.sp,
//                color = colorScheme.onSurfaceVariant,
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                lineHeight = 12.sp
//            )
//        }
//    }
//}



@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") } // State for email input in forgot password dialog
    var showForgotPasswordDialog by remember { mutableStateOf(false) } // Control dialog visibility

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(loginState) {
        if (loginState is AuthViewModel.LoginState.Success) {
            onLoginSuccess()
            viewModel.clearLoginState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Kapil Agro",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Text(
                "Scouting Hub",
                fontSize = 14.sp,
                color = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AgroLight,
                    contentColor = colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Enter your credentials to access your account",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        placeholder = { Text("example@email.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.trim() },
                        placeholder = { Text("password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text(
                                "Forgot password?",
                                color = colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (loginState is AuthViewModel.LoginState.Error) {
                        val errorMessage = (loginState as AuthViewModel.LoginState.Error).message
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.login(email.trim(), password.trim())
                        },
                        enabled = email.isNotBlank() && password.isNotBlank() && loginState !is AuthViewModel.LoginState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        if (loginState is AuthViewModel.LoginState.Loading) {
                            CircularProgressIndicator(
                                color = colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Sign in", fontSize = 14.sp)
                        }
                    }
                }
            }
            // Forgot Password Dialog
            if (showForgotPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showForgotPasswordDialog = false },
                    title = { Text("Forgot Password") },
                    text = {
                        Column {
                            Text("Enter your registered email to reset your password:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = forgotPasswordEmail,
                                onValueChange = { forgotPasswordEmail = it.trim() },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (forgotPasswordEmail.isNotBlank()) {
                                    showForgotPasswordDialog = false
                                }
                            },
                            enabled = forgotPasswordEmail.isNotBlank()
                        ) {
                            Text("Send Reset Link")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForgotPasswordDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "© 2025 Kapil Agro Scouting Hub. All Rights Reserved.",
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 12.sp
            )
        }
    }
}
