package com.aos.floney.view.analyze.subcategory

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.aos.floney.R
import com.aos.floney.base.BaseBottomSheetFragment
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.SuccessToastDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber


@AndroidEntryPoint
class BottomSheetAnaylzeLineSubcategory(
    private val category: String,
    private val subcategory: String
) :
    BaseBottomSheetFragment<BottomSheetAnalyzeSubcategoryBinding, AnalyzeLineSubcategoryViewModel>
        (R.layout.bottom_sheet_analyze_subcategory) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setCategory(category,subcategory)
        setUpViewModelObserver()
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.userSelectBottomSheet.collect {
                if (it) {
                    val bottomSheetFragment = BottomSheetAnaylzeUserSelectSubcategory()
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }

    }
}