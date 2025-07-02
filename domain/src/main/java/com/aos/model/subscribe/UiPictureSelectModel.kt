package com.aos.model.subscribe

import com.aos.model.home.ImageUrls
import androidx.recyclerview.widget.DiffUtil
import java.io.File

data class UiPictureSelectModel(
    val selectablePictures: List<SelectablePicture>
) {
    interface OnItemClickListener {
        fun onItemClick(item: SelectablePicture)
    }

    companion object : DiffUtil.ItemCallback<SelectablePicture>() {
        override fun areItemsTheSame(oldItem: SelectablePicture, newItem: SelectablePicture): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: SelectablePicture, newItem: SelectablePicture): Boolean {
            return oldItem == newItem
        }
    }
}

data class SelectablePicture(
    val picture: PictureItem,
    val isSelected: Boolean = false,
    val isDeleteMode: Boolean = false
)

sealed class PictureItem {
    data class CloudImage(val imageUrls: ImageUrls) : PictureItem() // S3 이미지 URL
    data class LocalImage(val file: File) : PictureItem()  // 로컬 이미지 파일
}
