
package com.aos.floney.view.history.picture

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aos.floney.databinding.ItemUploadImageBinding
import com.bumptech.glide.Glide

class UploadImagePagerAdapter(
    private val context: Context,
    private val uris: List<Uri>
) : RecyclerView.Adapter<UploadImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemUploadImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemUploadImageBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = uris.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(context).load(uris[position]).into(holder.binding.ivUploaded)
    }
}