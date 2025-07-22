package com.kapilagro.sasyak.presentation.common.mediaplayer
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.kapilagro.sasyak.presentation.common.components.ZoomableImage
import kotlinx.coroutines.delay
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ImageSlideshow(imageUrls: List<String>) {
    var currentImageIndex by rememberSaveable { mutableStateOf(0) }
    var showPreview by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val numberOfImages = imageUrls.size

    if (imageUrls.isEmpty()) return

    // Auto-scroll effect
    if (numberOfImages > 1) {
        LaunchedEffect(currentImageIndex) {
            delay(4000)
            val nextIndex = (currentImageIndex + 1) % numberOfImages
            if (currentImageIndex != nextIndex) {
                currentImageIndex = nextIndex
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.Black)
            .clickable { showPreview = true }
            // --- CORRECTED SWIPE LOGIC ---
            .pointerInput(numberOfImages) {
                if (numberOfImages > 1) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                        onDragEnd = {
                            val swipeThreshold = 50f
                            if (totalDrag > swipeThreshold) {
                                // Swiped right to left
                                currentImageIndex = (currentImageIndex - 1 + numberOfImages) % numberOfImages
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else if (totalDrag < -swipeThreshold) {
                                // Swiped left to right
                                currentImageIndex = (currentImageIndex + 1) % numberOfImages
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Animate between images using a simple crossfade
        Crossfade(
            targetState = currentImageIndex,
            animationSpec = tween(durationMillis = 300),
            label = "slideshowCrossfade"
        ) { index ->
            val url = imageUrls[index]
            val isVideo = url.endsWith(".mp4", ignoreCase = true)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Slideshow item ${index + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // Left and Right Navigation Arrows
        if (numberOfImages > 1) {
            IconButton(
                onClick = {
                    currentImageIndex = (currentImageIndex - 1 + numberOfImages) % numberOfImages
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Previous Image",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    currentImageIndex = (currentImageIndex + 1) % numberOfImages
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Next Image",
                    tint = Color.White
                )
            }
        }

        // Indicator dots
        if (numberOfImages > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                imageUrls.forEachIndexed { index, _ ->
                    val isSelected = index == currentImageIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (isSelected) 1f else 0.5f))
                    )
                }
            }
        }
    }

    // Full-screen preview dialog
    if (showPreview) {

        val playerViewModel: PlayerViewModel = viewModel()


        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                var previewIndex by rememberSaveable { mutableStateOf(currentImageIndex) }
                val selectedUrl = imageUrls[previewIndex]
                val isVideo = selectedUrl.endsWith(".mp4", ignoreCase = true)

                if (isVideo) {
                    VideoPlayer(
                        videoUri = Uri.parse(selectedUrl),
                        playerViewModel = playerViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ZoomableImage(
                        model = selectedUrl,
                        contentDescription = "Preview image ${previewIndex + 1}",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = { showPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = Color.White
                    )
                }

                if (numberOfImages > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(numberOfImages) { index ->
                            val isSelected = index == previewIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = if (isSelected) 1f else 0.5f))
                                    .clickable { previewIndex = index }
                            )
                        }
                    }
                }
            }
        }
    }
}