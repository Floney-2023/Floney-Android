package com.aos.data.entity.response.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetBookDaysEntity(
    val dayLinesResponse: List<DayLiensResponse> = listOf(),
    val totalExpense: List<TotalExpense>,
    val seeProfileImg: Boolean,
    val carryOverInfo: CarryOverInfoDays,
)

@Serializable
data class DayLiensResponse(
    val id: Int,
    val money: Double,
    val description: String,
    val lineCategory: String,
    val lineSubCategory: String,
    val assetSubCategory: String,
    val exceptStatus: Boolean,
    val writerEmail: String,
    val writerNickname: String,
    val writerProfileImg: String,
    val repeatDuration: String,
    val memo: String,
    val imageUrls: List<ImageUrls>,
)

@Serializable
data class ImageUrls(
    val id: Int,
    val url: String,
)

@Serializable
data class TotalExpense(
    val money: Double,
    val categoryType: String
)


@Serializable
data class CarryOverInfoDays(
    val carryOverStatus: Boolean,
    val carryOverMoney: Double
)
