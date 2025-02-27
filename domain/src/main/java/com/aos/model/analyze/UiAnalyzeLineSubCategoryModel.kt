package com.aos.model.analyze

import androidx.recyclerview.widget.DiffUtil

data class UiAnalyzeLineSubCategoryModel(
    val subcategoryName: String,
    val bookLines: List<bookLines>
) {
    interface OnItemClickListener {
        fun onItemClick(item: bookLines)
    }

    companion object : DiffUtil.ItemCallback<bookLines>() {
        override fun areItemsTheSame(oldItem: bookLines, newItem: bookLines): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: bookLines, newItem: bookLines): Boolean {
            return oldItem == newItem
        }
    }
}

data class bookLines(
    val money: String,
    val lineDate: String,
    val description: String,
    val userProfileImg: String? = ""
)