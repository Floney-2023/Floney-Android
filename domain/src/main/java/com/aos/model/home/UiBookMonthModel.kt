package com.aos.model.home

import androidx.recyclerview.widget.DiffUtil


data class UiBookMonthModel(
    val data: List<MonthMoney>,
    val extData: ExtData,
    val carryOverData : CarryOverInfo
) {
    interface OnItemClickListener {
        fun onItemClick(item: MonthMoney)
    }

    companion object : DiffUtil.ItemCallback<MonthMoney>() {
        override fun areItemsTheSame(oldItem: MonthMoney, newItem: MonthMoney): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: MonthMoney, newItem: MonthMoney): Boolean {
            return oldItem == newItem
        }
    }
}

data class MonthMoney(
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val expenses: Expenses = Expenses(),
    val isToday: Boolean = false
)

data class ExtData(
    val totalIncome: String,
    val totalOutcome: String,
    val totalBalance : String = "0"
)

data class Expenses(
    val date: String = "",
    var outcome: String = "",
    val income: String = "",
)

data class CarryOverInfo(
    val carryOverStatus: Boolean,
    val carryOverMoney: String,
)
