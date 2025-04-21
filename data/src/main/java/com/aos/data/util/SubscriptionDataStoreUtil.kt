package com.aos.data.util

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "subscription_prefs")

@Singleton
class SubscriptionDataStoreUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val KEY_BOOK_SUBSCRIBE = booleanPreferencesKey("book_subscribe")
    private val KEY_USER_SUBSCRIBE = booleanPreferencesKey("user_subscribe")
    private val KEY_SUBSCRIBE_BENEFIT = booleanPreferencesKey("subscribe_benefit")

    // 저장
    suspend fun setBookSubscribe(value: Boolean) {
        context.dataStore.edit { it[KEY_BOOK_SUBSCRIBE] = value }
    }

    suspend fun setUserSubscribe(value: Boolean) {
        context.dataStore.edit { it[KEY_USER_SUBSCRIBE] = value }
    }

    suspend fun setSubscribeBenefit(value: Boolean) {
        context.dataStore.edit { it[KEY_SUBSCRIBE_BENEFIT] = value }
    }

    // 조회
    fun getBookSubscribe(): Flow<Boolean> {
        return context.dataStore.data.map { it[KEY_BOOK_SUBSCRIBE] ?: false }
    }

    fun getUserSubscribe(): Flow<Boolean> {
        return context.dataStore.data.map { it[KEY_USER_SUBSCRIBE] ?: false }
    }

    fun getSubscribeBenefit(): Flow<Boolean> {
        return context.dataStore.data.map { it[KEY_SUBSCRIBE_BENEFIT] ?: false }
    }
}
