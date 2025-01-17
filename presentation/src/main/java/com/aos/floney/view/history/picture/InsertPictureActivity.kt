package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.lifecycleScope
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityInsertPictureBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.ChoicePictureDialog
import com.aos.floney.view.common.EditNotSaveDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class InsertPictureActivity :
    BaseActivity<ActivityInsertPictureBinding, InsertPictureViewModel>(R.layout.activity_insert_picture) {

    // 사진 찍기 결과
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            viewModel.addPictureNum()
            viewModel.createBitmapFile(viewModel.getTakeCaptureUri())

            val pictureNum = viewModel.getPictureNum()
            val imageView = when (pictureNum) {
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
                .load(viewModel.getImageFile(pictureNum))
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

                val imageView = when (pictureNum) {
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
                    .load(viewModel.getImageFile(pictureNum))
                    .fitCenter()
                    .centerCrop()
                    .into(imageView)
            }
        }
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val url = result.data?.getStringExtra("deleteFilePath") ?: ""
                Timber.e("url $url")

                // 삭제 요청 받음
                viewModel.deletePictureFile(url)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setupViewModelObserver()
        setUpBackPressHandler()
    }

    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            // 사진 추가 버튼 클릭
            viewModel.onClickedBack.collect {
                // 다이얼로그 표시
                EditNotSaveDialog(this@InsertPictureActivity) {
                    finish()
                }.show()
            }
        }
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
        repeatOnStarted {
            // 이미지 업로드 성공
            viewModel.onSuccessImageUpload.collect { url ->
                val intent = Intent()
                intent.putStringArrayListExtra("url", ArrayList(viewModel.getPictureList()))
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        repeatOnStarted {
            // 사진 상세 버튼 클릭
            viewModel.onClickPictureDetail.collect { url ->
                val intent =
                    Intent(this@InsertPictureActivity, InsertPictureDetailActivity::class.java)
                intent.putExtra("url", url)
                getResult.launch(intent)
            }
        }
        repeatOnStarted {
            // 사진 삭제 후 정렬
            viewModel.sortPictures.collect { files ->
                viewModel.setPictureNum(files.size)
                when (files.size) {
                    0 -> {
                        binding.ivAddPicture.isVisible = true
                        resetPictureImage(binding.ivPicture1)
                        resetPictureImage(binding.ivPicture2)
                        resetPictureImage(binding.ivPicture3)
                        resetPictureImage(binding.ivPicture4)
                    }

                    1 -> {
                        setPictureImage(binding.ivPicture2, files[0])
                        resetPictureImage(binding.ivPicture3)
                        resetPictureImage(binding.ivPicture4)
                    }

                    2 -> {
                        setPictureImage(binding.ivPicture2, files[0])
                        setPictureImage(binding.ivPicture3, files[1])
                        resetPictureImage(binding.ivPicture4)
                    }

                    3 -> {
                        binding.ivAddPicture.isVisible = true
                        resetPictureImage(binding.ivPicture1)
                        setPictureImage(binding.ivPicture2, files[0])
                        setPictureImage(binding.ivPicture3, files[1])
                        setPictureImage(binding.ivPicture4, files[2])
                    }
                }
            }
        }
    }

    private fun resetPictureImage(imageView: ImageView) {
        Glide.with(imageView.context).clear(imageView)
        imageView.setBackgroundResource(R.drawable.input_picture_blank)
    }

    private fun setPictureImage(imageView: ImageView, file: File) {
        Glide.with(this)
            .load(file)
            .fitCenter()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView)
    }

    private fun setUpBackPressHandler() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.getImageFileList().isNotEmpty()) {
                    // 다이얼로그 표시
                    EditNotSaveDialog(this@InsertPictureActivity) {
                        finish()
                    }.show()
                } else {
                    finish()
                }
            }
        })
    }

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