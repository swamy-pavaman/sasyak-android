package com.kapilagro.sasyak

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SasyakApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var taskRepository: TaskRepository // no error in log

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .also { Log.d("SasyakApplication", "Providing HiltWorkerFactory: $workerFactory") }
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("SasyakApplication", "Application onCreate, Hilt initialized")
    }
}
