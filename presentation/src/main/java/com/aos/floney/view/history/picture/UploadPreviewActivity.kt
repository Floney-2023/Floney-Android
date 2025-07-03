package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityUploadPreviewBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.DeletePictureDialog
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadPreviewActivity :
    BaseActivity<ActivityUploadPreviewBinding, UploadPreviewViewModel>(R.layout.activity_upload_preview) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.setVariable(BR.vm, viewModel)

        val uris = intent.getParcelableArrayListExtra<Uri>("uploadedUris") ?: emptyList()
        viewModel.setImages(uris)

        val adapter = UploadImagePagerAdapter(this, uris)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.dotIndicator, binding.viewPager) { _, _ -> }.attach()

        setupViewModelObserver()
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            viewModel.onClickedBack.collect {
                finish()
            }
        }
        repeatOnStarted {
            viewModel.onClickedUpload.collect {
                uploadComplete()
            }
        }
    }

    private fun uploadComplete(){
        val intent = Intent().apply {
            putParcelableArrayListExtra("uploadFilePath", ArrayList(viewModel.images.value ?: emptyList()))
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
