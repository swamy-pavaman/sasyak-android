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
    playerViewModel: PlayerViewModel
) {
    val context = LocalContext.current

    // This effect is still correct. It prepares the player without starting it.
    DisposableEffect(videoUri) {
        playerViewModel.initializePlayer(context)
        playerViewModel.setVideoUri(videoUri)
        playerViewModel.player?.playWhenReady = false // KEEP THIS

        onDispose {
            playerViewModel.player?.stop()
        }
    }

    // --- KEY CHANGE IN AndroidView ---
    AndroidView(
        factory = { ctx ->
            // 1. In factory, just CREATE the view.
            PlayerView(ctx).apply {
                useController = true
            }
        },
        update = { playerView ->
            // 2. In update, assign the player. This is safer because it runs
            // after the view is inflated and ready.
            playerView.player = playerViewModel.player
        },
        modifier = modifier
    )
}