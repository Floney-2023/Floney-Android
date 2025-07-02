package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.FragmentManager
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityInsertPictureBinding
import com.aos.floney.ext.intentSerializable
import com.aos.floney.ext.intentSerializableList
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ChoicePictureDialog
import com.aos.floney.view.common.EditNotSaveDialog
import com.aos.model.home.ImageUrls
import com.aos.model.subscribe.PictureItem
import com.aos.model.subscribe.SelectablePicture
import com.aos.model.subscribe.UiPictureSelectModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class InsertPictureActivity :
    BaseActivity<ActivityInsertPictureBinding, InsertPictureViewModel>(R.layout.activity_insert_picture), UiPictureSelectModel.OnItemClickListener {

    // 사진 찍기 결과
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            handleImageResult(viewModel.getTakeCaptureUri())
        }
    }

    // 갤러리 사진 불러오기
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(4)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val currentImageCount = viewModel.getCloudPictureList().size + viewModel.getLocalPictureList().size
            val remainingCount = 4 - currentImageCount

            if (uris.size > remainingCount) {
                viewModel.baseEvent(BaseViewModel.Event.ShowToast("사진은 최대 4장까지 추가할 수 있어요."))
                return@registerForActivityResult
            }

            uris.forEach { uri ->
                handleImageResult(uri)
            }
        }
    }

    // Android 12 이하용 갤러리 선택 (단일 이미지)
    private val legacyImageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val currentImageCount =
                    viewModel.getCloudPictureList().size + viewModel.getLocalPictureList().size

                // 다중 선택인 경우
                val clipData = data.clipData
                if (clipData != null) {
                    val remainingCount = 4 - currentImageCount
                    val itemCount = clipData.itemCount

                    if (itemCount > remainingCount) {
                        viewModel.baseEvent(BaseViewModel.Event.ShowToast("사진은 최대 4장까지 추가할 수 있어요."))
                        return@registerForActivityResult
                    }

                    for (i in 0 until itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        handleImageResult(uri)
                    }
                } else {
                    // 단일 선택인 경우
                    data.data?.let { uri ->
                        if (currentImageCount >= 4) {
                            viewModel.baseEvent(BaseViewModel.Event.ShowToast("사진은 최대 4장까지 추가할 수 있어요."))
                            return@registerForActivityResult
                        }

                        handleImageResult(uri)
                    }
                }
            }
        }
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.intentSerializable("deleteFilePath", ImageUrls::class.java)
                    ?.let {
                        Timber.e("url $it")
                        viewModel.setIsModify(true)
                        viewModel.deletePictureFile(it)
                    }

                viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast("사진 삭제가 완료되었습니다."))
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
        binding.setVariable(BR.eventHolder, this@InsertPictureActivity)

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.image_item_spacing) // 예: 12dp
        val itemDecoration = GridSpacingItemDecoration(spanCount = 2, spacing = spacingInPixels, includeEdge = true)
        binding.rvImageList.addItemDecoration(itemDecoration)
        binding.rvImageList.itemAnimator = null

        // 초기 url 이미지 세팅 (이미 저장했던 이미지가 있는 경우)
        val cloudUrls = intent.intentSerializableList<ImageUrls>("cloudPhotoUrl")
        val localUrls = intent.intentSerializableList<File>("localPhotoUrl")

        if (cloudUrls.isNotEmpty() || localUrls.isNotEmpty()) {
            Timber.i("cloudUrls : $cloudUrls")
            Timber.i("localUrls : $localUrls")
            viewModel.initPhotoList(cloudUrls, localUrls)
        }
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            // 뒤로 가기 버튼 클릭
            viewModel.onClickedBack.collect {

                val intent = Intent()
                intent.putExtra("updateCloudPhotoUrl", ArrayList(viewModel.getCloudPictureList()))
                intent.putExtra("updateLocalPhotoUrl", ArrayList(viewModel.getLocalPictureList()))

                setResult(Activity.RESULT_OK, intent)
                Timber.i("getCloudPictureList ${ ArrayList(viewModel.getCloudPictureList())}")
                Timber.i("getLocalPictureList ${ ArrayList(viewModel.getLocalPictureList())}")

                finish()
                /*
                if (viewModel.getIsModify()) {
                    // 다이얼로그 표시
                    EditNotSaveDialog(this@InsertPictureActivity) {
                        finish()
                    }.show()
                } else {
                    finish()
                }*/
            }
        }
        repeatOnStarted {
            // 사진 추가 버튼 클릭
            viewModel.onClickedAddPicture.collect {
                // 다이얼로그 표시
                ChoicePictureDialog(this@InsertPictureActivity, {
                    // 사진 촬영
                    viewModel.setTakeCaptureUri(viewModel.createTempImageFile())
                    viewModel.getTakeCaptureUri()?.let { uri ->
                        takePhoto.launch(uri)
                    } ?: run {
                        viewModel.baseEvent(BaseViewModel.Event.ShowToast("이미지 파일을 만들 수 없습니다."))
                    }
                }, {
                    // 앨범에서 사진 선택 (PhotoPicker 사용)
                    launchPhotoPicker()
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
    }

    private fun handleImageResult(uri: Uri?) {
        // 이미지 파일 생성
        viewModel.createBitmapFile(uri)
        // 수정 내역 여부 처리
        viewModel.setIsModify(true)
    }


    private fun setUpBackPressHandler() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent()
                intent.putExtra("updateCloudPhotoUrl", ArrayList(viewModel.getCloudPictureList()))
                intent.putExtra("updateLocalPhotoUrl", ArrayList(viewModel.getLocalPictureList()))

                setResult(Activity.RESULT_OK, intent)
                Timber.i("getCloudPictureList ${ ArrayList(viewModel.getCloudPictureList())}")
                Timber.i("getLocalPictureList ${ ArrayList(viewModel.getLocalPictureList())}")

                finish()
            }
        })
    }

    private fun launchPhotoPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상: PhotoPicker 사용 (다중 선택 가능)
            imageResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            // Android 12 이하: 기존 Intent 방식 사용 (단일 선택)
            launchLegacyImagePicker()
        }
    }

    private fun launchLegacyImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 다중 선택 시도
        }
        legacyImageResult.launch(intent)
    }

    override fun onItemClick(item: SelectablePicture) {
        // 보기 모드면 이미지 자세히 보기
        // 삭제 모드면 이미지 삭제 토글 ON/OFF
        if (viewModel.isDeleteMode.value!!)
            viewModel.settingSelectPicture(item)
        else {
            // PictureItem에 따라 처리
            val detailUrl: ImageUrls? = when (val picture = item.picture) {
                is PictureItem.CloudImage -> picture.imageUrls
                is PictureItem.LocalImage -> ImageUrls(-1, picture.file.absolutePath)
            }

            detailUrl?.let {
                val intent = Intent(this, InsertPictureDetailActivity::class.java)
                intent.putExtra("url", it)
                getResult.launch(intent)
            }
        }
    }
}
