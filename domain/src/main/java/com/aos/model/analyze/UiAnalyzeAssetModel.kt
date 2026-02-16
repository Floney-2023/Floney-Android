package com.aos.model.analyze

import androidx.recyclerview.widget.DiffUtil

data class UiAnalyzeAssetModel(
    val totalDifference: String,
    val difference: String,
    val differenceValue: Double,
    val initAsset: String,
    val currentAsset: String,
    val analyzeResult: List<Asset>
) {    interface OnItemClickListener {
        fun onItemClick(item: Asset)
    }

    companion object : DiffUtil.ItemCallback<Asset>() {
        override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
            return oldItem == newItem
        }
    }
}

data class Asset(
    val month: Int,
    val value: Float
)
