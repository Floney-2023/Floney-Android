package com.aos.floney.view.analyze

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryBinding
import com.aos.floney.databinding.FragmentAnalyzeOutComeBinding
import com.aos.floney.view.analyze.subcategory.BottomSheetAnaylzeLineSubcategory
import com.aos.floney.view.book.setting.carryinfo.BookSettingCarryInfoSheetFragment
import com.aos.model.analyze.AnalyzeResult
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AnalyzeOutComeFragment :
    BaseFragment<FragmentAnalyzeOutComeBinding, AnalyzeOutComeViewModel>(R.layout.fragment_analyze_out_come),
    UiAnalyzeCategoryOutComeModel.OnItemClickListener {
    private val activityViewModel: AnalyzeViewModel by activityViewModels()
    override val applyTransition: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelObserver()
        setUpUi()
        viewModel.postAnalyzeCategory(activityViewModel.getFormatDate())
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
    }

    private fun setUpViewModelObserver() {
        viewModel.postAnalyzeOutComeCategoryResult.observe(viewLifecycleOwner) {
            binding.chartStackHorizontal.isVisible = false
            val colorArr = arrayListOf<Int>()
            it.analyzeResult.forEachIndexed { index, analyzeResult ->
                if (index > 2) {
                    colorArr.add(analyzeResult.color)
                }
            }

            binding.chartStackHorizontal.setData(
                it.analyzeResult.map { it.percent.toFloat() }, colorArr
            )
            binding.chartStackHorizontal.isVisible = true
            binding.chartStackHorizontal.clearColorIdx()
        }

        lifecycleScope.launch {
            activityViewModel.onChangedDate.collect {
                viewModel.postAnalyzeCategory(it)
            }
        }
    }

    override fun onItemClick(item: AnalyzeResult) {
        Timber.i("click Outcome")
        // 상세 지출 bottomSheet로 이동
        val bottomSheetFragment = BottomSheetAnaylzeLineSubcategory(
            "지출",
            item.category)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

}