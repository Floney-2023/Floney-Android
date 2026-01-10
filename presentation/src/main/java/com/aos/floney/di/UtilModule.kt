package com.aos.floney.di

import com.aos.floney.util.LottieLoadingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {

    @Singleton
    @Provides
    fun provideLottieLoadingManager(): LottieLoadingManager {
        return LottieLoadingManager()
    }
}