package com.kapilagro.sasyak.presentation.common.mediaplayer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel // We will now use this
) {
    val context = LocalContext.current

    // --- 1. REMOVED the local ExoPlayer creation ---
    // val exoPlayer = remember { ... } has been deleted.

    // --- 2. ADDED DisposableEffect to manage the ViewModel's player ---
    // This effect will correctly set up and tear down the player from the ViewModel.
    DisposableEffect(videoUri) {
        playerViewModel.initializePlayer(context)
        playerViewModel.setVideoUri(videoUri)
        playerViewModel.player?.playWhenReady = true

        onDispose {
            // Stop playback, but do NOT release the player.
            // The ViewModel owns the player and will release it in onCleared().
            playerViewModel.player?.stop()
        }
    }

    // --- 3. CHANGED AndroidView to use the player from the ViewModel ---
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = playerViewModel.player // Use the ViewModel's player
                useController = true
            }
        },
        modifier = modifier
    )
}