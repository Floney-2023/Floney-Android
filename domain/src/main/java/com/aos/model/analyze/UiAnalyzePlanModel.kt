package com.aos.model.analyze

import androidx.recyclerview.widget.DiffUtil

data class UiAnalyzePlanModel(
    val leftMoney: String,
    val initBudget: String,
    val percent: String,
    val divMoney: String,
    val budgetUsedText: String,
    val budgetPerDayText: String,
    val budgetStatusText: String
) {
    interface OnItemClickListener {
        fun onItemClick(item: AnalyzeResult)
    }

    companion object : DiffUtil.ItemCallback<AnalyzeResult>() {
        override fun areItemsTheSame(oldItem: AnalyzeResult, newItem: AnalyzeResult): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: AnalyzeResult, newItem: AnalyzeResult): Boolean {
            return oldItem == newItem
        }
    }
}
