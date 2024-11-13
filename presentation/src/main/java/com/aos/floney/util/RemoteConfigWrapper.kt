package com.aos.floney.util

import com.aos.floney.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigWrapper @Inject constructor() {
    private val remoteConfig: FirebaseRemoteConfig by lazy{
        FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings
                .Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()

            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(mapOf(BuildConfig.MINIMUM_VERSION_KEY to "1.1.10"))
        }
    }

    fun fetchAndActivateConfig(): String {
        remoteConfig.fetchAndActivate().addOnCompleteListener{ task ->
            val updated = task.result
            if (task.isSuccessful){
                val updated = task.result
                Timber.d("Config params updated success: $updated")
            } else {
                Timber.d("Config params updated failed: $updated")
            }
        }
        return remoteConfig.getString(BuildConfig.MINIMUM_VERSION_KEY)
    }
}