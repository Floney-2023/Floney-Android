package com.aos.floney.view.book.setting.category

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingCategoryBinding
import com.aos.floney.databinding.FragmentBookSettingCurrencyBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.book.setting.BookSettingActivity
import com.aos.floney.view.book.setting.BookSettingMainFragmentDirections
import com.aos.floney.view.book.setting.currency.BookSettingCurrencyViewModel
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.model.book.Currency
import com.aos.model.book.UiBookCategory
import com.aos.model.book.UiBookCurrencyModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSettingCategoryFragment : BaseFragment<FragmentBookSettingCategoryBinding, BookSettingCategoryViewModel>(R.layout.fragment_book_setting_category) , UiBookCategory.OnItemClickListener {
    override fun onResume() {
        super.onResume()
    }
    override fun onItemClick(item: UiBookCategory) {

        if (viewModel.edit.value!!){

            val dialogInfo = getString(R.string.book_setting_category_dialog_info, item.name)

            val exitDialogFragment = WarningPopupDialog(
                getString(R.string.book_setting_category_dialog_title),
                dialogInfo,
                getString(R.string.book_setting_category_dialog_left_button),
                getString(R.string.book_setting_category_dialog_right_button),
                false
            ) { checked ->
                if (checked)
                    viewModel.deleteCategory(item)
            }
            exitDialogFragment.show(parentFragmentManager, "initDialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
        setUpResultListener()
        setUpBackButton()
    }

    private fun setUpResultListener(){
        // 저장 콜백 등록
        setFragmentResultListener("key") { key, bundle ->
            val result = bundle.getString("flow")
            viewModel.flow.value = result
            viewModel.getBookCategory()
        }
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
        viewModel.onClickPreviousPage()
    }
    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this@BookSettingCategoryFragment)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 이전 페이지로
            viewModel.back.collect {
                if(it) {
                    val activity = requireActivity() as BookCategoryActivity
                    activity.startBookSettingActivity()
                }
                else{ // 편집 모드일 경우
                    BaseAlertDialog(title = "잠깐", info = "수정한 내용이 저장되지 않았습니다.\n그대로 나가시겠습니까?", false) {
                        if(it) {
                            val activity = requireActivity() as BookCategoryActivity
                            activity.startBookSettingActivity()
                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                }
            }
        }
        repeatOnStarted {
            // 분류항목 추가 페이지로
            viewModel.addPage.collect {
                if(it) {
                    val addAction = BookSettingCategoryFragmentDirections.actionBookSettingCategoryFragmentToBookSettingCategoryAddFragment(viewModel.flow.value!!)
                    findNavController().navigate(addAction)
                }
            }
        }
    }

}