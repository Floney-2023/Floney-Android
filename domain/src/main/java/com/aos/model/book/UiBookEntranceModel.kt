package com.aos.model.book

import androidx.recyclerview.widget.DiffUtil
import timber.log.Timber


data class UiBookEntranceModel(
    val bookName: String,
    val bookImg: String,
    val bookInfo: String,
    val bookKey : String
){
    interface OnItemClickListener {
        fun onItemClick(item: UiBookEntranceModel)
    }

    companion object : DiffUtil.ItemCallback<UiBookEntranceModel>() {
        override fun areItemsTheSame(oldItem: UiBookEntranceModel, newItem: UiBookEntranceModel): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: UiBookEntranceModel, newItem: UiBookEntranceModel): Boolean {
            return oldItem == newItem
        }
    }
}