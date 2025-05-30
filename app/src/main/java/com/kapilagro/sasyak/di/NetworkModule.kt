package com.kapilagro.sasyak.di

import android.content.SharedPreferences
import com.kapilagro.sasyak.BuildConfig
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.OpenWeatherApiService
import com.kapilagro.sasyak.data.api.interceptors.AuthInterceptor
import com.kapilagro.sasyak.data.api.interceptors.NetworkConnectivityInterceptor
import com.kapilagro.sasyak.data.api.interceptors.ResponseInterceptor
import com.kapilagro.sasyak.data.api.interceptors.TokenAuthenticator
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_API_URL = "https://sasyak-backend.onrender.com/"
    private const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/"
    const val OPENWEATHER_API_KEY = "07c824948b0cc53ffa41bd3b23a71847"

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Singleton
    @Provides
    fun provideTokenAuthenticator(
        authRepository: dagger.Lazy<AuthRepository>
    ): TokenAuthenticator {
        return TokenAuthenticator(authRepository)
    }

    @Singleton
    @Provides
    @Named("mainClient")
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        networkConnectivityInterceptor: NetworkConnectivityInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        responseInterceptor: ResponseInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(networkConnectivityInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(responseInterceptor)
            .authenticator(tokenAuthenticator) // Add the authenticator here
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }



    @Singleton
    @Provides
    fun provideResponseInterceptor(
        authRepository: dagger.Lazy<AuthRepository>
    ): ResponseInterceptor {
        return ResponseInterceptor(authRepository)
    }


    @Singleton
    @Provides
    @Named("weatherClient")
    fun provideWeatherOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        networkConnectivityInterceptor: NetworkConnectivityInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(networkConnectivityInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    @Named("mainRetrofit")
    fun provideRetrofit(@Named("mainClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @Named("weatherRetrofit")
    fun provideWeatherRetrofit(@Named("weatherClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPENWEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }




    @Singleton
    @Provides
    fun provideApiService(@Named("mainRetrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideOpenWeatherApiService(@Named("weatherRetrofit") retrofit: Retrofit): OpenWeatherApiService {
        return retrofit.create(OpenWeatherApiService::class.java)
    }
}