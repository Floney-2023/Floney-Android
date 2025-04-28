package com.aos.floney.view.book.add

import android.app.Activity
import android.app.Activity.RESULT_OK
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
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.util.PermissionUtil
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ChoiceImageDialog
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class BookAddSettingProfileFragment : BaseFragment<FragmentBookAddSettingProfileBinding, BookAddSettingProfileViewModel>(R.layout.fragment_book_add_setting_profile) {

    // 사진 찍기 결과
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it) {
            viewModel.createBitmapFile(viewModel.getTakeCaptureUri())

            Glide.with(requireContext())
                .load(viewModel.getImageBitmap())
                .fitCenter()
                .centerCrop()
                .into(binding.ivProfileCardView)
        }
    }

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                viewModel.createBitmapFile(result.data?.data)

                Glide.with(requireContext())
                    .load(viewModel.getImageBitmap())
                    .fitCenter()
                    .centerCrop()
                    .into(binding.ivProfileCardView)
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
                if(it) {
                    val code = viewModel.inviteCode.value ?: ""
                    val nextAction =
                        BookAddSettingProfileFragmentDirections.actionBookAddSettingProfileFragmentToBookAddCreateSuccessFragment(code)
                    findNavController().navigate(nextAction)
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
                    BaseAlertDialog("프로필 변경", "기본 프로필로 변경하시겠습니까?", true) {
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
        PermissionUtil.checkAndRequestGalleryPermission(
            activity = requireActivity(),
            fragmentManager = parentFragmentManager,
            onGranted = {
                ChoiceImageDialog(requireContext(), {
                    // 사진 촬영하기
                    viewModel.setTakeCaptureUri(viewModel.createTempImageFile())
                    takePhoto.launch(viewModel.getTakeCaptureUri())
                }, {
                    // 앨범에서 사진 선택
                    selectGallery()
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
        )
    }

    private fun selectGallery() {
        // 권한이 있는 경우 갤러리 실행
        val intent = Intent(Intent.ACTION_PICK)
        // intent와 data와 type을 동시에 설정하는 메서드
        intent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
        )

        imageResult.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtil.REQUEST_GALLERY_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClickChoiceImage()
            } else {
                viewModel.baseEvent(BaseViewModel.Event.ShowToast("이미지 접근 권한이 허용되지 않았습니다."))
            }
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
    }
}