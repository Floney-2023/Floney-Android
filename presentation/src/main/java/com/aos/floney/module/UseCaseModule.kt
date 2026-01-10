package com.aos.floney.module

import com.aos.repository.AnalyzeRepository
import com.aos.repository.BookRepository
import com.aos.repository.SubscribeRepository
import com.aos.repository.UserRepository
import com.aos.usecase.analyze.PostAnalyzeAssetUseCase
import com.aos.usecase.analyze.PostAnalyzeIPlanUseCase
import com.aos.usecase.analyze.PostAnalyzeInComeCategoryUseCase
import com.aos.usecase.analyze.PostAnalyzeOutComeCategoryUseCase
import com.aos.usecase.bookadd.ChangeBookImgUseCase
import com.aos.usecase.history.DeleteBookLineUseCase
import com.aos.usecase.history.DeleteBooksLinesAllUseCase
import com.aos.usecase.history.GetBookCategoryUseCase
import com.aos.usecase.history.PostBooksLinesChangeUseCase
import com.aos.usecase.history.PostBooksLinesUseCase
import com.aos.usecase.home.CheckUserBookUseCase
import com.aos.usecase.home.GetBookInfoUseCase
import com.aos.usecase.home.GetMoneyHistoryDaysUseCase
import com.aos.usecase.home.GetMoneyHistoryMonthUseCase
import com.aos.usecase.login.AuthTokenCheckUseCase
import com.aos.usecase.login.SocialLoginUseCase
import com.aos.usecase.mypage.ChangeProfileUseCase
import com.aos.usecase.signup.CheckEmailCodeUseCase
import com.aos.usecase.signup.SendEmailUseCase
import com.aos.usecase.password.SendTempPasswordUseCase
import com.aos.usecase.signup.SignUpSocialUseCase
import com.aos.usecase.signup.SignUpUseCase
import com.aos.usecase.subscribe.SubscribeAndroidUseCase
import com.aos.usecase.subscribe.SubscribeBookUseCase
import com.aos.usecase.subscribe.SubscribePresignedUrlUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideSendEmailUseCase(userRepository: UserRepository) = SendEmailUseCase(userRepository)
    @Provides
    @Singleton
    fun provideCheckEmailCodeUseCase(userRepository: UserRepository) = CheckEmailCodeUseCase(userRepository)
    @Provides
    @Singleton
    fun provideSignUpUseCase(userRepository: UserRepository) = SignUpUseCase(userRepository)
    @Provides
    @Singleton
    fun provideSendTempPasswordUseCase(userRepository: UserRepository) = SendTempPasswordUseCase(userRepository)
    @Provides
    @Singleton
    fun provideCheckUserBookUseCase(bookRepository: BookRepository) = CheckUserBookUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideGetMoneyHistoryMonthUseCase(bookRepository: BookRepository) = GetMoneyHistoryMonthUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideGetMoneyHistoryDaysUseCase(bookRepository: BookRepository) = GetMoneyHistoryDaysUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideGetBookInfoUseCase(bookRepository: BookRepository) = GetBookInfoUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideGetBookCategoryUseCase(bookRepository: BookRepository) = GetBookCategoryUseCase(bookRepository)
    @Provides
    @Singleton
    fun providePostBooksLinesUseCase(bookRepository: BookRepository) = PostBooksLinesUseCase(bookRepository)
    @Provides
    @Singleton
    fun providePostBooksLinesChangeUseCase(bookRepository: BookRepository) = PostBooksLinesChangeUseCase(bookRepository)
    @Provides
    @Singleton
    fun providePostAnalyzeCategoryUseCase(analyzeRepository: AnalyzeRepository) = PostAnalyzeOutComeCategoryUseCase(analyzeRepository)
    @Provides
    @Singleton
    fun providePostAnalyzeInComeCategoryUseCase(analyzeRepository: AnalyzeRepository) = PostAnalyzeInComeCategoryUseCase(analyzeRepository)
    @Provides
    @Singleton
    fun providePostAnalyzeIPlanUseCase(analyzeRepository: AnalyzeRepository) = PostAnalyzeIPlanUseCase(analyzeRepository)
    @Provides
    @Singleton
    fun providePostAnalyzeAssetUseCase(analyzeRepository: AnalyzeRepository) = PostAnalyzeAssetUseCase(analyzeRepository)
    @Provides
    @Singleton
    fun provideDeleteBookLineUseCase(bookRepository: BookRepository) = DeleteBookLineUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideDeleteBooksLinesAllUseCase(bookRepository: BookRepository) = DeleteBooksLinesAllUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideAuthTokenCheckUseCase(userRepository: UserRepository) = AuthTokenCheckUseCase(userRepository)
    @Provides
    @Singleton
    fun provideSignUpSocialUseCase(userRepository: UserRepository) = SignUpSocialUseCase(userRepository)
    @Provides
    @Singleton
    fun provideSocialLoginUseCase(userRepository: UserRepository) = SocialLoginUseCase(userRepository)
    @Provides
    @Singleton
    fun provideGetChangeProfileUseCase(userRepository: UserRepository) = ChangeProfileUseCase(userRepository)
    @Provides
    @Singleton
    fun provideChangeBookImgUseCase(bookRepository: BookRepository) = ChangeBookImgUseCase(bookRepository)
    @Provides
    @Singleton
    fun provideSubscribeAndroidUseCase(subscribeRepository: SubscribeRepository) = SubscribeAndroidUseCase(subscribeRepository)
    @Provides
    @Singleton
    fun provideSubscribePresignedUseCase(subscribeRepository: SubscribeRepository) = SubscribePresignedUrlUseCase(subscribeRepository)
    @Provides
    @Singleton
    fun provideSubscribeBookUseCase(subscribeRepository: SubscribeRepository) = SubscribeBookUseCase(subscribeRepository)

}