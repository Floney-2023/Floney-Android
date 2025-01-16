package com.aos.floney.view.book.setting.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.FragmentBookSettingEditProfilechangeBinding
import com.aos.floney.ext.repeatOnStarted
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
        if (checkGalleryPermission()) {
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
        } else {
            viewModel.baseEvent(BaseViewModel.Event.ShowToast("이미지 접근 권한이 허용되지 않았습니다. "))
        }
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
        }
    }

    private fun checkGalleryPermission(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val imagePermission = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Timber.e("true")
            if (imagePermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
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
                    requireActivity(), arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
                )
                false
            } else {
                true
            }
        }
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