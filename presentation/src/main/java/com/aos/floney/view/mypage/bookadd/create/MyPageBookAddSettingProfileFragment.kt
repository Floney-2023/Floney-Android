package com.aos.floney.view.mypage.bookadd.create

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityBookAddBinding
import com.aos.floney.databinding.FragmentBookAddSettingProfileBinding
import com.aos.floney.databinding.FragmentMyPageBookAddSettingProfileBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.util.PermissionUtil
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ChoiceImageDialog
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class MyPageBookAddSettingProfileFragment : BaseFragment<FragmentMyPageBookAddSettingProfileBinding, MyPageBookAddSettingProfileViewModel>(R.layout.fragment_my_page_book_add_setting_profile) {

    // 사진 찍기 결과
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            viewModel.createBitmapFile(viewModel.getTakeCaptureUri())

            Glide.with(requireContext())
                .load(viewModel.getImageBitmap())
                .fitCenter()
                .centerCrop()
                .into(binding.ivProfileCardView)
        }
    }

    // 갤러리 사진 불러오기
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            lifecycleScope.launch {
                viewModel.createBitmapFile(uri)

                Glide.with(requireContext())
                    .load(viewModel.getImageBitmap())
                    .fitCenter()
                    .centerCrop()
                    .into(binding.ivProfileCardView)
            }
        }
    }

    // Android 11 이하용 갤러리 선택
    private val legacyImageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                lifecycleScope.launch {
                    viewModel.createBitmapFile(uri)

                    Glide.with(requireContext())
                        .load(viewModel.getImageBitmap())
                        .fitCenter()
                        .centerCrop()
                        .into(binding.ivProfileCardView)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelObserver()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 다음 페이지 이동
            viewModel.nextPage.collect {
                Timber.e("nextPage $it")
                if(it) {
                    val code = viewModel.inviteCode.value ?: ""
                    val action =
                        MyPageBookAddSettingProfileFragmentDirections.actionMyPageBookAddSettingProfileFragmentToMyPageBookAddCreateSuccessFragment(code)
                    findNavController().navigate(action)
                }
            }
        }
        repeatOnStarted {
            // 이전 페이지 이동
            viewModel.back.collect {
                if(it) {
                    findNavController().popBackStack()
                }
            }
        }
        repeatOnStarted {
            viewModel.onClickChoiceImage.collect {
                if(it) {
                    onClickChoiceImage()
                }
            }
        }
        repeatOnStarted {
            viewModel.onClickChange.collect {
                if (it) {
                    if (viewModel.getImageBitmap() != null) {
                        viewModel.onClickNextPage()
                    } else {
                        viewModel.baseEvent(BaseViewModel.Event.ShowToast("변경할 이미지가 선택되지 않았습니다."))
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.onClickDefaultProfile.collect {
                if (it) {
                    BaseAlertDialog(getString(R.string.dialog_change_profile_title), getString(R.string.dialog_switch_default_profile), true) {
                        if(it) {
                            // 랜덤 이미지
                            val bitmap = BitmapFactory.decodeResource(
                                requireContext().resources,
                                R.drawable.icon_default_profile
                            )

                            Glide.with(requireContext())
                                .load(bitmap)
                                .fitCenter()
                                .centerCrop()
                                .into(binding.ivProfileCardView)

                            viewModel.onClickNextPage()
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                }
            }
        }
    }

    private fun onClickChoiceImage() {
        ChoiceImageDialog(requireContext(), {
            // 사진 촬영하기
            viewModel.setTakeCaptureUri(viewModel.createTempImageFile())
            takePhoto.launch(viewModel.getTakeCaptureUri())
        }, {
            // 앨범에서 사진 선택
            launchPhotoPicker()
        }, {
            // 랜덤 이미지
            val bitmap = BitmapFactory.decodeResource(
                requireContext().resources,
                viewModel.getRandomProfileDrawable()
            )

            viewModel.setImageBitmap(bitmap)

            Glide.with(requireContext())
                .load(bitmap)
                .fitCenter()
                .centerCrop()
                .into(binding.ivProfileCardView)
        }).show()
    }

    private fun launchPhotoPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상: PhotoPicker 사용
            imageResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            // Android 12 이하: 기존 Intent 방식 사용
            launchLegacyImagePicker()
        }
    }

    private fun launchLegacyImagePicker() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        legacyImageResult.launch(intent)
    }

    private fun selectGallery() {
        launchPhotoPicker()
    }

    // No longer needed as PhotoPicker doesn't require permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
