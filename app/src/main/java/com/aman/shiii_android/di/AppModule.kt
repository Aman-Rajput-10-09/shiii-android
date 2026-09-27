package com.aman.shiii_android.di

import com.aman.shiii_android.data.remote.ShiiiApiService
import com.aman.shiii_android.data.repository.ShiiiRepositoryImpl
import com.aman.shiii_android.domain.repository.ShiiiRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindShiiiRepository(impl: ShiiiRepositoryImpl): ShiiiRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Production backend deployed on Vercel
    const val BASE_URL = "https://shiii-backend.vercel.app/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideShiiiApiService(okHttpClient: OkHttpClient): ShiiiApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShiiiApiService::class.java)
    }
}
