package com.kapilagro.sasyak.presentation.common.mediaplayer

// In your ViewModel file (e.g., PlayerViewModel.kt)
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import androidx.media3.common.MediaItem

class PlayerViewModel : ViewModel() {

    // Keep the player instance here
    var player: ExoPlayer? = null
        private set

    fun initializePlayer(context: Context) {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
    }

    fun setVideoUri(uri: android.net.Uri) {
        player?.setMediaItem(MediaItem.fromUri(uri))
        player?.prepare()
    }

    // Release the player when the ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}