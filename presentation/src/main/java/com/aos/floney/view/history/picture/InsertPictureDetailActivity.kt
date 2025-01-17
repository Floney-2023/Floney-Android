package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityInsertPictureDetailBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.DeletePictureDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import timber.log.Timber
import java.io.File
import java.util.ArrayList

class InsertPictureDetailActivity :
    BaseActivity<ActivityInsertPictureDetailBinding, InsertPictureDetailViewModel>(
        R.layout.activity_insert_picture_detail
    ) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setupViewModelObserver()
        showImage(getImageUrl())
    }

    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            viewModel.onClickedBack.collect {
                finish()
            }
        }
        repeatOnStarted {
            viewModel.onClickedDelete.collect {
                DeletePictureDialog(this@InsertPictureDetailActivity) {
                    // 삭제 선택
                    val intent = Intent()
                    intent.putExtra("deleteFilePath", viewModel.getImageUrl())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }.show()
            }
        }
    }

    private fun showImage(url: String) {
        if (!url.isNullOrEmpty()) {
            val file = File(url)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.ivDetail.setImageBitmap(bitmap) // ImageView에 Bitmap 설정
            } else {
                Timber.e("File does not exist: $url")
            }
        } else {
            Timber.e("Image path is null or empty")
        }
    }

    // 이미지 가져오기
    private fun getImageUrl(): String {
        val url = intent.getStringExtra("url") ?: ""
        viewModel.setImageUrl(url)
        return url
    }
}