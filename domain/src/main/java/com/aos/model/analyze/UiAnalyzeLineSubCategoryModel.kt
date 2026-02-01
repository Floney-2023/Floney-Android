package com.aos.model.analyze

import androidx.recyclerview.widget.DiffUtil

data class UiAnalyzeLineSubCategoryModel(
    val subcategoryName: String,
    val bookLines: List<BookSubData>
) {
    interface OnItemClickListener {
        fun onItemClick(item: BookSubData)
    }

    companion object : DiffUtil.ItemCallback<BookSubData>() {
        override fun areItemsTheSame(oldItem: BookSubData, newItem: BookSubData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: BookSubData, newItem: BookSubData): Boolean {
            return oldItem == newItem
        }
    }
}

data class BookSubData (
    val money: String,
    val descriptionDetail: String,
    val description: String,
    val userProfileImg: String? = ""
)