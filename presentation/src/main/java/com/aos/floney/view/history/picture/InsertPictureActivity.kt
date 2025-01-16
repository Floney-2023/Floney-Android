package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.lifecycleScope
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityInsertMemoBinding
import com.aos.floney.databinding.ActivityInsertPictureBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.ChoicePictureDialog
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class InsertPictureActivity: BaseActivity<ActivityInsertPictureBinding, InsertPictureViewModel>(R.layout.activity_insert_picture) {

    // 사진 찍기 결과
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it) {
            viewModel.addPictureNum()
            viewModel.createBitmapFile(viewModel.getTakeCaptureUri())

            val pictureNum = viewModel.getPictureNum()
            val imageView = when(pictureNum) {
                    1 -> binding.ivPicture2
                    2 -> binding.ivPicture3
                    3 -> binding.ivPicture4
                    4 -> {
                        binding.ivAddPicture.isVisible = false
                        binding.ivPicture1
                    }
                    else -> binding.ivPicture1
                }

            Glide.with(this)
                .load(viewModel.getImageBitmap(pictureNum))
                .fitCenter()
                .centerCrop()
                .into(imageView)
        }
    }

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                viewModel.addPictureNum()
                viewModel.createBitmapFile(result.data?.data)

                val pictureNum = viewModel.getPictureNum()

                val imageView = when(pictureNum) {
                    1 -> binding.ivPicture2
                    2 -> binding.ivPicture3
                    3 -> binding.ivPicture4
                    4 -> {
                        binding.ivAddPicture.isVisible = false
                        binding.ivPicture1
                    }
                    else -> binding.ivPicture1
                }

                Glide.with(this@InsertPictureActivity)
                    .load(viewModel.getImageBitmap(pictureNum))
                    .fitCenter()
                    .centerCrop()
                    .into(imageView)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setupViewModelObserver()
    }

    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            // 사진 추가 버튼 클릭
            viewModel.onClickedAddPicture.collect {
                // 다이얼로그 표시
                ChoicePictureDialog(this@InsertPictureActivity, {
                    // 사진 촬영
                    viewModel.setTakeCaptureUri(viewModel.createTempImageFile())
                    takePhoto.launch(viewModel.getTakeCaptureUri())
                }, {
                    // 앨범에서 사진 선택
                    selectGallery()
                }).show()
            }
        }
    }

//    private fun setUpBackPressHandler() {
//        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (viewModel.getImageBitmap() != null) {
//                    BaseAlertDialog(title = "잠깐", info = "수정한 내용이 저장되지 않았습니다.\n그대로 나가시겠습니까?", false) {
//                        if(it) {
//                            finish()
//                        }
//                    }.show(this@InsertPictureActivity, "baseAlertDialog")
//                } else {
//                    findNavController().popBackStack()
//                }
//            }
//        })
//    }

    private fun selectGallery() {
        if (checkGalleryPermission()) {
            // 권한이 있는 경우 갤러리 실행
            val intent = Intent(Intent.ACTION_PICK)
            // intent와 data와 type을 동시에 설정하는 메서드
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
            )

            imageResult.launch(intent)
        } else {
            viewModel.baseEvent(BaseViewModel.Event.ShowToast("이미지 접근 권한이 허용되지 않았습니다."))
        }
    }

    private fun checkGalleryPermission(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val imagePermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_MEDIA_IMAGES
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Timber.e("true")
            if (imagePermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    ), 1
                )

                false
            } else {
                true
            }
        } else {
            Timber.e("else")
            if (readPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
                )
                false
            } else {
                true
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }

}