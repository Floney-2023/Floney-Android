package com.aos.floney.view.book.setting.favorite

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingFavoriteBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.analyze.AnalyzeActivity
import com.aos.floney.view.book.setting.category.BookCategoryActivity
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.home.HomeViewModel
import com.aos.model.book.UiBookFavoriteModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BookSettingFavoriteFragment : BaseFragment<FragmentBookSettingFavoriteBinding, BookSettingFavoriteViewModel>(R.layout.fragment_book_setting_favorite) , UiBookFavoriteModel.OnItemClickListener {
    private val activityViewModel: BookFavoriteViewModel by activityViewModels()

    override fun onItemClick(item: UiBookFavoriteModel) {
        if (viewModel.edit.value!!)
        {

            val exitDialogFragment = BaseAlertDialog(
                "삭제하기",
                "삭제하시겠습니까?",
                false
            ) { checked ->
                if (checked)
                    viewModel.deleteFavorite(item)
            }
            exitDialogFragment.show(parentFragmentManager, "favoriteLoadDialog")
        }
        else if (activityViewModel.entryCheck){
            val exitDialogFragment = BaseAlertDialog(
                "즐겨찾기",
                "해당 내역을 불러오겠습니까?",
                false
            ) { checked ->
                if (checked){
                    val activity = requireActivity() as BookFavoriteActivity
                    activity.startHistoryAddActivity(item)
                }
            }
            exitDialogFragment.show(parentFragmentManager, "favoriteLoadDialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
        setUpBackButton()
        setUpResultListener()
    }

    private fun setUpResultListener(){
        // 저장 콜백 등록
        setFragmentResultListener("key") { key, bundle ->
            val result = bundle.getString("flow")
            viewModel.flow.value = result
            viewModel.getBookCategory()
        }
    }

    fun setUpBackButton(){
        // 뒤로 가기 콜백 등록
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    override fun onBackPressed() {
        viewModel.onClickPreviousPage()
    }
    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this@BookSettingFavoriteFragment)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 이전 페이지로 (가계부 설정)
            viewModel.back.collect {
                if(it) {
                    val activity = requireActivity() as BookFavoriteActivity
                    activity.startBookSettingActivity()
                }
                else{ // 편집 모드일 경우
                    BaseAlertDialog(title = "잠깐", info = "수정한 내용이 저장되지 않았습니다.\n그대로 나가시겠습니까?", false) {
                        if(it) {
                            val activity = requireActivity() as BookFavoriteActivity
                            activity.startBookSettingActivity()
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                }
            }
        }
        repeatOnStarted {
            // 즐겨찾기 추가 페이지로 (초과 아닌 경우에만)

            viewModel.addPage.collect {
                if(it) {
                    val addAction = BookSettingFavoriteFragmentDirections.actionBookSettingFavoriteFragmentToBookSettingFavoriteAddFragment(viewModel.flow.value!!)
                    findNavController().navigate(addAction)
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