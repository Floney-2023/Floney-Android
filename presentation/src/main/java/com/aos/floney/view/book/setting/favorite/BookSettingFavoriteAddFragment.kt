package com.aos.floney.view.book.setting.favorite

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingFavoriteAddBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.history.CategoryBottomSheetDialog
import com.aos.floney.view.history.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookSettingFavoriteAddFragment :
    BaseFragment<FragmentBookSettingFavoriteAddBinding, HistoryViewModel>(R.layout.fragment_book_setting_favorite_add) {

    private lateinit var categoryBottomSheetDialog: CategoryBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setFavoriteMode()
        setUpViewModelObserver()
        setUpBackButton()
    }

    private fun setUpBackButton(){
        // 뒤로 가기 콜백 등록
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    override fun onBackPressed() {
        viewModel.onFavoriteAddClickCloseBtn()
    }



    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.onClickCategory.collect {
                categoryBottomSheetDialog = CategoryBottomSheetDialog(requireContext(), it, viewModel, this@BookSettingFavoriteAddFragment, {
                    // 완료 버튼 클릭
                    viewModel.onClickCategoryChoiceDate()
                }, {
                    // 편집 버튼 클릭 (카테고리 설정 화면)
                    val activity = requireActivity() as BookFavoriteActivity
                    activity.startBookSettingActivity()
                })
                categoryBottomSheetDialog.show()
            }
        }

        repeatOnStarted {
            viewModel.postBooksFavorites.collect {
                if(it) {
                    findNavController().popBackStack()
                }
            }
        }

        repeatOnStarted {
            viewModel.onClickCloseBtn.collect {
                if(it) {
                    // 수정 내역 있음
                    BaseAlertDialog(title = "잠깐", info = "수정한 내용이 저장되지 않았습니다.\n그대로 나가시겠습니까?", false) {
                        if(it) {
                            findNavController().popBackStack()
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                } else {
                    // 수정 내역 없음
                    findNavController().popBackStack()
                }
            }
        }
    }
}