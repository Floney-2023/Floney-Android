package com.aos.floney.module

import android.content.Context
import com.aos.data.repository.remote.alarm.AlarmRemoteDataSourceImpl
import com.aos.data.repository.remote.alarm.AlarmRepositoryImpl
import com.aos.data.repository.remote.analyze.AnalyzeRemoteDataSourceImpl
import com.aos.data.repository.remote.analyze.AnalyzeRepositoryImpl
import com.aos.data.repository.remote.book.BookRemoteDataSourceImpl
import com.aos.data.repository.remote.book.BookRepositoryImpl
import com.aos.data.repository.remote.subscribe.SubscribeRemoteDataSourceImpl
import com.aos.data.repository.remote.subscribe.SubscribeRepositoryImpl
import com.aos.data.repository.remote.user.UserRemoteDataSourceImpl
import com.aos.data.repository.remote.user.UserRepositoryImpl
import com.aos.repository.AlarmRepository
import com.aos.repository.AnalyzeRepository
import com.aos.repository.BookRepository
import com.aos.repository.SubscribeRepository
import com.aos.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(
        userRemoteDataSource: UserRemoteDataSourceImpl
    ) : UserRepository {
        return UserRepositoryImpl(
            userRemoteDataSource
        )
    }

    @Singleton
    @Provides
    fun provideBookRepository(
        @ApplicationContext context: Context,
        bookDataSourceImpl: BookRemoteDataSourceImpl
    ) : BookRepository {
        return BookRepositoryImpl(
            context,
            bookDataSourceImpl
        )
    }

    @Singleton
    @Provides
    fun provideAnalyzeRepository(
        @ApplicationContext context: Context,
        analyzeRemoteDataSourceImpl: AnalyzeRemoteDataSourceImpl
    ) : AnalyzeRepository {
        return AnalyzeRepositoryImpl(
            context,
            analyzeRemoteDataSourceImpl
        )
    }

    @Singleton
    @Provides
    fun provideAlarmRepository(
        alarmRemoteDataSourceImpl: AlarmRemoteDataSourceImpl
    ) : AlarmRepository {
        return AlarmRepositoryImpl(
            alarmRemoteDataSourceImpl
        )
    }

    @Singleton
    @Provides
    fun provideSubscribeRepository(
        subscribeRemoteDataSourceImpl: SubscribeRemoteDataSourceImpl
    ) : SubscribeRepository {
        return SubscribeRepositoryImpl(
            subscribeRemoteDataSourceImpl
        )
    }
}