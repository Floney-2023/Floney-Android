package com.aos.floney.view.book.setting.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.FragmentBookSettingEditProfilechangeBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.util.PermissionUtil
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ChoiceImageDialog
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class BookSettingProfileChangeFragment :
    BaseFragment<FragmentBookSettingEditProfilechangeBinding, BookSettingProfileChangeViewModel>(R.layout.fragment_book_setting_edit_profilechange) {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelObserver()
        setUpBackPressHandler()
    }

    private fun setUpBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.getImageBitmap() != null || (!viewModel.getBookProfile().equals("") && viewModel.isDefaultProfile)) {
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

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.back.collect() {
                if(it){
                    if (viewModel.getImageBitmap() != null || (!viewModel.getBookProfile().equals("") && viewModel.isDefaultProfile)) {
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
                        viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast("변경이 완료되었습니다."))
                        findNavController().popBackStack()
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
                                .load(R.drawable.btn_book_profile)
                                .fitCenter()
                                .centerCrop()
                                .into(binding.ivProfileCardView)

                            viewModel.setImageBitmap(null)
                            viewModel.isDefaultProfile = true
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                }
            }
        }
        repeatOnStarted {
            viewModel.onChange.collect() {
                if(it) {
                    findNavController().popBackStack()
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
        imageResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
