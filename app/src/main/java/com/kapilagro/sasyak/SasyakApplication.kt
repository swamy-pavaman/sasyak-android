package com.kapilagro.sasyak

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
// This is the standard and recommended way to provide a custom WorkManager configuration.
// It relies on the provider in AndroidManifest.xml being correctly removed, which we are
// now doing with the androidx.startup library.
class SasyakApplication : Application(), Configuration.Provider,ImageLoaderFactory {

    // Hilt will inject the factory for us.
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // This property provides the custom configuration to WorkManager when it initializes.
    // This is the correct implementation for modern versions of the WorkManager library.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Add the VideoFrameDecoder to handle .mp4 and other video formats
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}