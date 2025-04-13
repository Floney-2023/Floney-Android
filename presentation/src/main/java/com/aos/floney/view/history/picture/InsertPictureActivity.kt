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
import com.aos.model.home.ImageUrls
import com.aos.model.home.PictureItem
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
                Timber.i("pictureNum ${pictureNum}")
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

                val imageUrls : ImageUrls? = result.data?.getSerializableExtra("deleteFilePath") as? ImageUrls
                Timber.e("url $imageUrls")

                // 삭제 요청 받음
                if (imageUrls != null) {
                    viewModel.deletePictureFile(imageUrls)
                }
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

        // 초기 url 이미지 세팅 (이미 저장했던 이미지가 있는 경우)
        val cloudUrls : ArrayList<ImageUrls>? = intent.getSerializableExtra("cloudPhotoUrl") as? ArrayList<ImageUrls>
        val localUrls : ArrayList<File>? = intent.getSerializableExtra("localPhotoUrl") as? ArrayList<File>

        Timber.i("cloudUrls : $cloudUrls")
        Timber.i("localUrls : $localUrls")

        if (!localUrls.isNullOrEmpty() || !cloudUrls.isNullOrEmpty()){
            val totalCount = (localUrls?.size ?: 0) + (cloudUrls?.size ?: 0)
            viewModel.setPictureNum(totalCount) // 개수 카운트
            viewModel.initPhotoList(cloudUrls, localUrls) // 데이터 세팅
        }
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
                intent.putExtra("updateCloudPhotoUrl", ArrayList(viewModel.getCloudPictureList()))
                intent.putExtra("updateLocalPhotoUrl", ArrayList(viewModel.getLocalPictureList()))

                setResult(Activity.RESULT_OK, intent)
                Timber.i("getCloudPictureList ${ ArrayList(viewModel.getCloudPictureList())}")
                Timber.i("getLocalPictureList ${ ArrayList(viewModel.getLocalPictureList())}")

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
            // 사진 상세 버튼 클릭 (인덱스 값 감지)
            viewModel.onClickPictureDetailNum.collect { index ->
                // 현재 이미지 개수
                val cloudImageCount = viewModel.getCloudFileList().size
                val localImageCount = viewModel.getImageFileList().size
                val totalImageCount = cloudImageCount + localImageCount

                if (totalImageCount < index) // 이미지가 없는 뷰 클릭 시 return
                    return@collect

                // 상세 이미지 Urls
                val detailUrls: ImageUrls? = if (index <= cloudImageCount) {
                    // 클라우드 이미지인 경우, 그냥 index로 가져옴
                    viewModel.getCloudFileList()[index - 1]
                } else {
                    // 로컬 이미지인 경우, 클라우드 개수만큼 빼줘야 올바른 index가 됨
                    val localIndex = index - cloudImageCount - 1 // ★ 여기서 인덱스 보정
                    viewModel.getImageFileList().getOrNull(localIndex)?.let {
                        ImageUrls(-1, it.absolutePath)
                    }
                }

                Timber.i("detailUrls ${detailUrls}")
                detailUrls?.let {
                    val intent =
                        Intent(this@InsertPictureActivity, InsertPictureDetailActivity::class.java)
                    intent.putExtra("url", detailUrls)
                    getResult.launch(intent)
                }
            }
        }
        repeatOnStarted {
            // 사진 소팅 후 정렬
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

                    4 -> {
                        binding.ivAddPicture.isVisible = false
                        setPictureImage(binding.ivPicture1, files[3])
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

    private fun setPictureImageFromUrl(imageView: ImageView, url: String) {
        Glide.with(this)
            .load(url)
            .fitCenter()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    private fun setPictureImage(imageView: ImageView, file: File) {
        Glide.with(this)
            .load(file)
            .fitCenter()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    fun setPictureImage(imageView: ImageView, picture: PictureItem) {
        when (picture) {
            is PictureItem.CloudImage -> {
                setPictureImageFromUrl(imageView, picture.imageUrls.url)
            }
            is PictureItem.LocalImage -> {
                setPictureImage(imageView, picture.file)
            }
        }
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