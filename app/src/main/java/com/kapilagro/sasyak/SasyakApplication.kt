package com.kapilagro.sasyak


import android.app.Application
import com.kapilagro.sasyak.data.repositories.WeatherRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SasyakApplication : Application()