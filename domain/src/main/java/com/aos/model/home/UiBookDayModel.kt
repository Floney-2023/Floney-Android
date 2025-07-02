package com.aos.model.home

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import java.io.Serial
import java.io.Serializable


data class UiBookDayModel(
    val data: List<DayMoney>,
    val extData: ExtData,
    val carryOverData : CarryOverInfo
) {
    interface OnItemClickListener {
        fun onItemClick(item: DayMoney)
    }

    companion object : DiffUtil.ItemCallback<DayMoney>() {
        override fun areItemsTheSame(oldItem: DayMoney, newItem: DayMoney): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: DayMoney, newItem: DayMoney): Boolean {
            return oldItem == newItem
        }
    }
}

data class DayMoney(
    val id: Int,
    val money: String,
    val description: String,
    val lineCategory: String,
    val lineSubCategory: String,
    val assetSubCategory: String,
    val exceptStatus: Boolean,
    val writerEmail: String,
    val writerNickName: String,
    val writerProfileImg: String,
    val repeatDuration: String,
    val memo: String,
    val imageUrls: List<ImageUrls>,
    val seeProfileStatus: Boolean
)


data class ImageUrls(
    val id: Int,
    val url: String,
) : Serializable

// 내역 수정 전달 아이템
data class DayMoneyModifyItem(
    val id: Int,
    var money: String,
    val description: String,
    val lineDate: String,
    var lineCategory: String,
    val lineSubCategory: String,
    val assetSubCategory: String,
    val exceptStatus: Boolean,
    val writerNickName: String,
    val repeatDuration: String,
    val memo: String,
    val imageUrls: List<ImageUrls>
): Serializable

// 즐겨찾기 전달 아이템
data class DayMoneyFavoriteItem(
    val money: String,
    val description: String,
    val lineCategoryName: String,
    val lineSubcategoryName: String,
    val assetSubcategoryName: String,
    val exceptStatus : Boolean
): Serializable

