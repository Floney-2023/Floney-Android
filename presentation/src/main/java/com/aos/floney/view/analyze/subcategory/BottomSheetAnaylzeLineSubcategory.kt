package com.aos.floney.view.analyze.subcategory

import android.os.Bundle
import android.view.View
import com.aos.floney.R
import com.aos.floney.base.BaseBottomSheetFragment
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryBinding
import dagger.hilt.android.AndroidEntryPoint


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

    }
}