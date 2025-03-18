package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityInsertPictureDetailBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.DeletePictureDialog
import com.aos.model.home.ImageUrls
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.Serializable

@AndroidEntryPoint
class InsertPictureDetailActivity :
    BaseActivity<ActivityInsertPictureDetailBinding, InsertPictureDetailViewModel>(
        R.layout.activity_insert_picture_detail
    ) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setupViewModelObserver()
        setImageInform()
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
                    // cloud 이미지일 경우, 이미지 삭제 api 호출
                    if (viewModel.getImageUrl().id != -1)
                    {
                        viewModel.imgDelete()
                    }else {
                        deleteComplete()
                    }

                }.show()
            }
        }
        repeatOnStarted {
            viewModel.onDeleteComplete.collect {
                if(it){
                    deleteComplete()
                }
            }
        }
    }

    private fun deleteComplete(){
        val intent = Intent()
        intent.putExtra("deleteFilePath", viewModel.getImageUrl())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun showImage(imageUrls: ImageUrls) {
        Timber.i("imageUrls ${imageUrls}")
        if (imageUrls.id != -1){
            setImageFromPresignedUrl(binding.ivDetail, imageUrls.url)
        }
        else if (imageUrls.url.isNotBlank()) {
            val file = File(imageUrls.url)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.ivDetail.setImageBitmap(bitmap) // ImageView에 Bitmap 설정
            } else {
                Timber.e("File does not exist: $imageUrls.url")
            }
        } else {
            Timber.e("Image path is null or empty")
        }
    }

    fun setImageFromPresignedUrl(imageView: ImageView, presignedUrl: String) {

        Glide.with(this)
            .load(presignedUrl)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView)
    }

    // 이미지 정보 가져오기
    private fun setImageInform() {
        val imageUrls : ImageUrls? = intent.serializable<ImageUrls>("url")
        imageUrls?.let{
            viewModel.setImageUrl(imageUrls)
            showImage(imageUrls)
        }
    }
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getSerializableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(key) as? T
    }
}
