package com.aos.model.book

import androidx.recyclerview.widget.DiffUtil
import timber.log.Timber


data class UiBookSettingModel(
    val bookImg: String,
    val bookName: String,
    val startDay: String,
    val seeProfileStatus: Boolean,
    val carryOver: Boolean,
    val ourBookUsers: List<MyBookUsers>
){
    interface OnItemClickListener {
        fun onItemClick(item: MyBookUsers)
    }

    companion object : DiffUtil.ItemCallback<MyBookUsers>() {
        override fun areItemsTheSame(oldItem: MyBookUsers, newItem: MyBookUsers): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: MyBookUsers, newItem: MyBookUsers): Boolean {
            return oldItem == newItem
        }
    }
}
data class MyBookUsers(
    val name: String,
    val profileImg: String,
    val email: String,
    val me: Boolean,
    val role: String,
    val roleOrigin: String
)