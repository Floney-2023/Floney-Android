package com.aos.floney.view.analyze.subcategory

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseBottomSheetFragment
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryBinding
import com.aos.floney.ext.DpToPx
import com.aos.floney.ext.repeatOnStarted
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tbuonomo.viewpagerdotsindicator.pxToDp
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

    override fun onStart() {
        super.onStart()

        val screenHeight = resources.displayMetrics.heightPixels
        val collapsedHeight = (screenHeight * 0.6).toInt()
        val expandedHeight = (screenHeight * 0.8).toInt()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)

            sheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            behavior.peekHeight = collapsedHeight
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED

            // 이 방식은 깜빡임 방지용
            val rootContainer = sheet.findViewById<View>(R.id.cl_bottom_sheet_analyze_view)
            rootContainer?.layoutParams?.height = expandedHeight
            rootContainer?.requestLayout()
        }
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