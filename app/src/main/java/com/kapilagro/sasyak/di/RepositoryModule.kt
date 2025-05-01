package com.kapilagro.sasyak.di



import com.kapilagro.sasyak.data.api.OpenWeatherApiService
import com.kapilagro.sasyak.data.repositories.AuthRepositoryImpl
import com.kapilagro.sasyak.data.repositories.NotificationRepositoryImpl
import com.kapilagro.sasyak.data.repositories.TaskAdviceRepositoryImpl
import com.kapilagro.sasyak.data.repositories.TaskRepositoryImpl
import com.kapilagro.sasyak.data.repositories.UserRepositoryImpl
import com.kapilagro.sasyak.domain.repositories.WeatherRepository
import com.kapilagro.sasyak.data.repositories.WeatherRepositoryImpl
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import com.kapilagro.sasyak.utils.LocationService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindTaskAdviceRepository(
        taskAdviceRepositoryImpl: TaskAdviceRepositoryImpl
    ): TaskAdviceRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository



}