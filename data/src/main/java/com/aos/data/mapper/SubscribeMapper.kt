package com.aos.data.mapper

import com.aos.data.entity.response.subscribe.GetPresignedUrlEntity
import com.aos.data.entity.response.subscribe.GetSubscribeAndroidEntity
import com.aos.model.subscribe.GetPresignedUrlModel
import com.aos.model.subscribe.GetSubscribeAndroidModel
fun GetSubscribeAndroidEntity.toGetSubscribeAndroidModel(): GetSubscribeAndroidModel {
    return GetSubscribeAndroidModel(this.isValid ?: false)
}

fun GetPresignedUrlEntity.toGetPresignedUrlModel(): GetPresignedUrlModel {
    return GetPresignedUrlModel(this.fileName, this.url, this.viewUrl)
}