package com.aos.floney.view.mypage.inform.profile.change

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aos.data.util.CommonUtil
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.FragmentMyPageInformProfilechangeBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.util.PermissionUtil
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ChoiceImageDialog
import com.aos.floney.view.mypage.inform.MyPageInformActivity
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MyPageInformProfileChangeFragment :
    BaseFragment<FragmentMyPageInformProfilechangeBinding, MyPageInformProfileChangeViewModel>(R.layout.fragment_my_page_inform_profilechange) {

    private lateinit var activity: MyPageInformActivity

    // 사진 찍기 결과
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it) {
            viewModel.createBitmapFile(viewModel.getTakeCaptureUri())

            Glide.with(requireContext())
                .load(viewModel.getImageBitmap())
                .fitCenter()
                .centerCrop()
                .into(binding.profileImg)
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
                    .into(binding.profileImg)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as MyPageInformActivity

        setUpViewModelObserver()
        setupUi()
        setUpBackPressHandler()
    }
    private fun setUpBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.getImageBitmap() != null || (!viewModel.getUserProfile().equals("user_default") && viewModel.isDefaultProfile)) {
                     BaseAlertDialog(title = "잠깐", info = "수정한 내용이 저장되지 않았습니다.\n그대로 나가시겠습니까?", false) {
                        if(it) {
                            findNavController().popBackStack()
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                } else {
                    findNavController().popBackStack()
                }
            }
        })
    }
    private fun setupUi() {
        if(viewModel.getUserProfile().equals("user_default")) {
            Glide.with(requireContext())
                .load(R.drawable.btn_profile)
                .fitCenter()
                .centerCrop()
                .into(binding.profileImg)
        } else {
            Glide.with(requireContext())
                .load(viewModel.getUserProfile())
                .fitCenter()
                .centerCrop()
                .into(binding.profileImg)
        }
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.back.collect() {
                if(it){
                    if (viewModel.getImageBitmap() != null || (!viewModel.getUserProfile().equals("user_default") && viewModel.isDefaultProfile)) {
                        BaseAlertDialog(title = "잠깐", info = "수정한 내용이 저장되지 않았습니다.\n그대로 나가시겠습니까?", false) {
                            if(it) {
                                findNavController().popBackStack()
                            }
                        }.show(parentFragmentManager, "baseAlertDialog")
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.onClickChoiceImage.collect {
                if (it) {
                    onClickChoiceImage()
                }
            }
        }
        repeatOnStarted {
            viewModel.onClickChange.collect {
                if (it) {
                    if (viewModel.getImageBitmap() != null || viewModel.isDefaultProfile) {
                        viewModel.uploadImageFile(viewModel.getImageBitmap())
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
                            // 기본 이미지 설정
                            Glide.with(requireContext())
                                .load(R.drawable.btn_profile)
                                .fitCenter()
                                .centerCrop()
                                .into(binding.profileImg)

                            viewModel.setImageBitmap(null)
                            viewModel.isDefaultProfile = true
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                }
            }
        }
        repeatOnStarted {
            viewModel.successProfileChange.collect {
                findNavController().popBackStack()
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
                        .into(binding.profileImg)
                }).show()
            }
        )
    }

    private fun selectGallery() {
        val intent = Intent(Intent.ACTION_PICK)
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
}