package com.aos.floney.view.analyze

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentAnalyzeIncomeBinding
import com.aos.floney.view.analyze.subcategory.BottomSheetAnaylzeLineSubcategory
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.model.analyze.AnalyzeResult
import com.aos.model.analyze.UiAnalyzeCategoryInComeModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyzeIncomeFragment :
    BaseFragment<FragmentAnalyzeIncomeBinding, AnalyzeIncomeViewModel>(R.layout.fragment_analyze_income),
    UiAnalyzeCategoryInComeModel.OnItemClickListener {

    private val activityViewModel: AnalyzeViewModel by activityViewModels()
    override val applyTransition: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
        viewModel.postAnalyzeCategory(activityViewModel.getFormatDate())
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
    }

    private fun setUpViewModelObserver() {
        viewModel.postAnalyzeInComeCategoryResult.observe(viewLifecycleOwner) {
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
        // 구독 중일 때 분석 상세 bottomSheet 표시
        if(activityViewModel.subscribeBookActive){
            // 상세 지출 bottomSheet로 이동 (선택된 달이 있는 경우만)
            viewModel.selectMonth.value?.let {
                val bottomSheetFragment = BottomSheetAnaylzeLineSubcategory(
                    category = "수입",
                    subcategory = item.category,
                    date = it
                )
                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            }
        } else // 구독 중일 아닐 경우
        {
            // 혜택 적용 중이라면, 구독 팝업
            if(activityViewModel.subscribeExpired)
                activityViewModel.showSubscribePopupIfNeeded()
            // 혜택 적용 중이 아니라면, 클릭 시 구독 유도 팝업이 표시되도록 한다.
            else {
                val exitDialogFragment = WarningPopupDialog(
                    getString(R.string.subscribe_prompt_title),
                    getString(R.string.subscribe_prompt_inform),
                    getString(R.string.already_pick_button),
                    getString(R.string.subscribe_plan_btn),
                    true
                ) {  checked ->
                    if (!checked) // 구독 플랜 보기로 이동
                    {
                        val activity = requireActivity() as AnalyzeActivity
                        activity.goToSubscribePlanActivity()
                    }
                }
                exitDialogFragment.show(parentFragmentManager, "exitDialog")
            }
        }
    }
}