package com.aos.floney.view.book.setting.favorite

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingFavoriteAddBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog
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
                    setFragmentResult("key", bundleOf("flow" to viewModel.lineType.value))
                    findNavController().popBackStack()
                }
            }
        }

        repeatOnStarted {
            viewModel.onClickCloseBtn.collect {
                if(it) {
                    // 수정 내역 있음
                    BaseAlertDialog(title = getString(R.string.dialog_wait), info = getString(R.string.dialog_not_saved), false) {
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
        repeatOnStarted {
            // 구독 유도 팝업
            viewModel.subscribePrompt.collect {
                if(it) {
                    val exitDialogFragment = WarningPopupDialog(
                        getString(R.string.subscribe_prompt_title),
                        getString(R.string.subscribe_prompt_inform),
                        getString(R.string.already_pick_button),
                        getString(R.string.subscribe_plan_btn),
                        true
                    ) {  checked ->
                        if (!checked) // 구독 플랜 보기로 이동
                        {
                            val activity = requireActivity() as BookFavoriteActivity
                            activity.goToSubscribePlanActivity()
                        }
                    }
                    exitDialogFragment.show(parentFragmentManager, "exitDialog")
                }
            }
        }
    }
}