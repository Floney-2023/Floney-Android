package com.aos.floney.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.aos.floney.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchMaintenanceTimes(): Pair<LocalDateTime, LocalDateTime> {

        // remoteConfig 활성화 되었는 지 확인
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("Remote config fetch failed")
            }
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        // Remote Config에서 점검 시작 및 종료 시간 값 가져오기
        val maintenanceStartTime = remoteConfig.getString("aos_maintenance_start_time")
        val maintenanceEndTime = remoteConfig.getString("aos_maintenance_end_time")
        Timber.i("dateTime : ${maintenanceEndTime} ${maintenanceStartTime}")

        // 값이 비어있거나 유효하지 않은 경우 기본값 반환
        if (maintenanceStartTime.isBlank() || maintenanceEndTime.isBlank()) {
            // 예를 들어 현재 시간보다 과거의 시간을 반환하여 점검 시간이 아니도록 함
            return Pair(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
            )
        }

        return try {
            // 가져온 값들을 LocalDateTime으로 파싱
            val maintenanceStart = LocalDateTime.parse(maintenanceStartTime, formatter)
            val maintenanceEnd = LocalDateTime.parse(maintenanceEndTime, formatter)
            Pair(maintenanceStart, maintenanceEnd)
        } catch (e: Exception) {
            // 파싱 중 오류가 발생하면 기본값 반환
            Timber.e(e, "Failed to parse maintenance times")
            Pair(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
            )
        }
    }
}