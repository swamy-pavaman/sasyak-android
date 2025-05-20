package com.kapilagro.sasyak.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    // Animations
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val fieldDarkness = remember { Animatable(0f) }
    val progressLine = remember { Animatable(0f) }
    val fieldFill = remember { Animatable(0f) }

    val sunOffsetX = remember { Animatable(0f) }
    val sunOffsetY = remember { Animatable(0f) }
    val sunScale = remember { Animatable(1f) }

    var showPlant by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        // Animate logo
        logoScale.animateTo(1f, tween(500, easing = EaseOutBack))
        logoAlpha.animateTo(1f, tween(700))
        fieldDarkness.animateTo(0.6f, tween(1000))

        // Animate green fill inside circle
        fieldFill.animateTo(1f, tween(1200))

        // Sun animation (moves and scales)
        sunOffsetX.animateTo(-25f, tween(800))
        sunOffsetY.animateTo(-25f, tween(800))
        sunScale.animateTo(1.3f, tween(800))

        delay(600)
        showPlant = true

        // Animate progress bar below text
        progressLine.animateTo(1f, tween(1500))

        delay(600)

        if (authState) onNavigateToHome()
        else onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // full white background only
        contentAlignment = Alignment.Center
    )

    {

        Column(
            modifier = Modifier
                .scale(logoScale.value)
                .alpha(logoAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                // Green circle base + vertical fill
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fullSize = size
                    val fillHeight = fullSize.height * fieldFill.value

                    // Light green base circle
                    drawCircle(
                        color = Color(0xFF16A34A),
                        radius = size.minDimension / 2,
                        center = center
                    )
                    // Circle fill inside the animated Canvas
//                    drawRect(
//                        color = Color(0xFF16A34A).copy(alpha = 0.85f), // softer fill
//                        topLeft = Offset(0f, fullSize.height - fillHeight),
//                        size = Size(fullSize.width, fillHeight)
//                    )


                    // Darker green fill rising from bottom
//                    drawRect(
//                        color = Color(0xFF16A34A),
//                        topLeft = Offset(0f, fullSize.height - fillHeight),
//                        size = Size(fullSize.width, fillHeight)
//                    )
                }

                // 🌞 Sun icon (moving to top-left with scale)
                Image(
                    painter = painterResource(id = R.drawable.baseline_sunny_24), // Add this to drawable
                    contentDescription = "Sun",
                    colorFilter = ColorFilter.tint(Color.White),

                    modifier = Modifier
                        .size((32 * sunScale.value).dp)
                        .offset(x = Dp(sunOffsetX.value), y = Dp(sunOffsetY.value))

                )

                // 🌱 Plant icon appears after delay
                if (showPlant) {

                    Image(
                        painter = painterResource(id = R.drawable.ic_sprout), // Already available
                        contentDescription = "Plant",
                        modifier = Modifier.size(60.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFF1F1F1)),
                        contentScale = ContentScale.Fit // Softer white

                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Kapil Agro",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scouting Hub",
                fontSize = 18.sp,
                color = Color(0xFF16A34A).copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom progress bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(6.dp)
            ) {
                drawRoundRect(
                    color = Color(0xFF16A34A),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width * progressLine.value, size.height),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }
    }
}