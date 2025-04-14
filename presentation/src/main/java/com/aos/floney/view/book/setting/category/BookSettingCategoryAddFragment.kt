package com.aos.floney.view.book.setting.category

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingCategoryAddBinding
import com.aos.floney.databinding.FragmentBookSettingCategoryBinding
import com.aos.floney.databinding.FragmentBookSettingCurrencyBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.book.setting.BookSettingActivity
import com.aos.floney.view.book.setting.BookSettingMainFragmentDirections
import com.aos.floney.view.book.setting.currency.BookSettingCurrencyViewModel
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.model.book.Currency
import com.aos.model.book.UiBookCategory
import com.aos.model.book.UiBookCurrencyModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSettingCategoryAddFragment() : BaseFragment<FragmentBookSettingCategoryAddBinding, BookSettingCategoryAddViewModel>(R.layout.fragment_book_setting_category_add) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }
    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this@BookSettingCategoryAddFragment)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 이전 페이지로 (저장 X)
            viewModel.back.collect {
                if(it) {
                    findNavController().popBackStack()
                }
            }
        }
        repeatOnStarted {
            // 이전 페이지로 (저장 O)
            viewModel.completePage.collect {
                if(it.isNotEmpty()) {
                    setFragmentResult("key", bundleOf("flow" to viewModel.flow.value))
                    findNavController().popBackStack()
                }
            }
        }
    }
}