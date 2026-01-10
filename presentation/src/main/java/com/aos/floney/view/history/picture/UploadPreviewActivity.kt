package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityUploadPreviewBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.DeletePictureDialog
import com.aos.floney.view.common.EditNotSaveDialog
import com.aos.floney.view.onboard.OnBoardViewPaperAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class UploadPreviewActivity :
    BaseActivity<ActivityUploadPreviewBinding, UploadPreviewViewModel>(R.layout.activity_upload_preview) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setVariable(BR.vm, viewModel)

        setUpBackPressHandler()
        setImageAdapter()
        setupViewModelObserver()
    }

    private fun setUpBackPressHandler() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                EditNotSaveDialog(this@UploadPreviewActivity) {
                    finish()
                }.show()
            }
        })
    }

    private fun setImageAdapter() {
        val uris = intent.getParcelableArrayListExtra<Uri>("uploadedUris") ?: emptyList()
        viewModel.setImages(uris)

        val adapter = UploadImagePagerAdapter(this, uris)
        binding.viewPager.adapter = adapter

        when (uris.size) {
            1 -> {
                binding.dotIndicator.visibility = View.GONE
                binding.viewPager.adapter = UploadImagePagerAdapter(this, uris)
                binding.viewPager.isUserInputEnabled = false
            }
            else -> {
                binding.viewPager.adapter = UploadImagePagerAdapter(this, uris)
                binding.dotIndicator.attachTo(binding.viewPager)
                binding.viewPager.isUserInputEnabled = true
            }
        }
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            viewModel.onClickedBack.collect {
                EditNotSaveDialog(this@UploadPreviewActivity) {
                    finish()
                }.show()
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
