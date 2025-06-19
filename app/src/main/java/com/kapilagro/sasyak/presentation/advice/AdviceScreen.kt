package com.kapilagro.sasyak.presentation.advice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kapilagro.sasyak.R
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.presentation.common.theme.AdviceContainer
import com.kapilagro.sasyak.presentation.common.theme.AdviceIcon
import com.kapilagro.sasyak.presentation.common.theme.Card
import com.kapilagro.sasyak.presentation.common.theme.Foreground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdviceScreen(
    onTaskClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AdviceViewModel = hiltViewModel()
) {
    val state by viewModel.adviceState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advice") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is AdviceState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AdviceState.Success -> {
                val advices = (state as AdviceState.Success).advices
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(advices) { advice ->
                        AdviceCard(
                            advice = advice,
                            onClick = { onTaskClick(advice.taskId) }
                        )
                    }
                }
            }
            is AdviceState.Error -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet))
                val lottieAnimatable = rememberLottieAnimatable()

                // Play the Lottie animation continuously once it's loaded
                LaunchedEffect(composition) {
                    if (composition != null) {
                        lottieAnimatable.animate(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            speed = 1f
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieAnimatable.progress },
                            modifier = Modifier.size(300.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdviceCard(
    advice: TaskAdvice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AdviceContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "Advice Icon",
                tint = AdviceIcon,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Task #${advice.taskId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Foreground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = advice.adviceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Foreground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By: ${advice.managerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = advice.createdAt.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}