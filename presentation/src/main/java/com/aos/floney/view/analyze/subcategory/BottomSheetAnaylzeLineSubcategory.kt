package com.aos.floney.view.analyze.subcategory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseBottomSheetFragment
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryBinding
import com.aos.floney.ext.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetAnaylzeLineSubcategory(
    private val category: String,
    private val subcategory: String,
    private val date: String
) :
    BaseBottomSheetFragment<BottomSheetAnalyzeSubcategoryBinding, AnalyzeLineSubcategoryViewModel>
        (R.layout.bottom_sheet_analyze_subcategory) {
    val activityViewModel: AnalyzeLineSubcategoryViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setVariable(BR.vm, activityViewModel)
        activityViewModel.setCategory(category, subcategory, date)
        activityViewModel.getUserList()
        setUpViewModelObserver()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            activityViewModel.userSelectBottomSheet.collect {
                if (it) {
                    val bottomSheetFragment = BottomSheetAnaylzeUserSelectSubcategory { emailList ->
                        activityViewModel.settingLineSubcategory()
                    }
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }

        repeatOnStarted {
            activityViewModel.sortBottomSheet.collect {
                if (it) {
                    val bottomSheetFragment = BottomSheetAnaylzeSortSelectSubcategory { sortType ->
                        activityViewModel.settingLineSubcategory()
                    }
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }

    }
}