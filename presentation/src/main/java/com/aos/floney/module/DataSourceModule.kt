package com.aos.floney.module

import com.aos.data.api.AlarmService
import com.aos.data.api.AnalyzeService
import com.aos.data.api.BookService
import com.aos.data.api.NaverShortenUrlService
import com.aos.data.api.SubscribeService
import com.aos.data.api.UserService
import com.aos.data.repository.remote.alarm.AlarmRemoteDataSourceImpl
import com.aos.data.repository.remote.analyze.AnalyzeRemoteDataSourceImpl
import com.aos.data.repository.remote.book.BookRemoteDataSourceImpl
import com.aos.data.repository.remote.subscribe.SubscribeRemoteDataSourceImpl
import com.aos.data.repository.remote.user.UserRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Singleton
    @Provides
    fun provideUserRemoteDataSourceImpl(userService: UserService) = UserRemoteDataSourceImpl(userService)

    @Singleton
    @Provides
    fun provideBookRemoteDataSourceImpl(bookService: BookService, naverShortenUrlService : NaverShortenUrlService) = BookRemoteDataSourceImpl(bookService, naverShortenUrlService)

    @Singleton
    @Provides
    fun provideAnalyzeRemoteDataSourceImpl(analyzeService: AnalyzeService) = AnalyzeRemoteDataSourceImpl(analyzeService)

    @Singleton
    @Provides
    fun provideAlarmRemoteDataSourceImpl(alarmService: AlarmService) = AlarmRemoteDataSourceImpl(alarmService)

    @Singleton
    @Provides
    fun provideSubscribeRemoteDataSourceImpl(subscribeService: SubscribeService) = SubscribeRemoteDataSourceImpl(subscribeService)

}