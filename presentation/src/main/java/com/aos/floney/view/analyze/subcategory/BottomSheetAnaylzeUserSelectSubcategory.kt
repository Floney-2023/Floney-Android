package com.aos.floney.view.analyze.subcategory

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import com.aos.floney.R
import com.aos.floney.base.BaseBottomSheetFragment
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryBinding
import com.aos.floney.databinding.BottomSheetAnalyzeSubcategoryUserBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.SuccessToastDialog
import com.aos.model.settlement.BookUsers
import com.aos.model.settlement.UiMemberSelectModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber


@AndroidEntryPoint
class BottomSheetAnaylzeUserSelectSubcategory(
    private val clickedChoiceBtn: (List<String>) -> Unit
) :
    BaseBottomSheetFragment<BottomSheetAnalyzeSubcategoryUserBinding, AnalyzeLineSubcategoryViewModel>
        (R.layout.bottom_sheet_analyze_subcategory_user), UiMemberSelectModel.OnItemClickListener {
    val activityViewModel: AnalyzeLineSubcategoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setVariable(BR.vm, activityViewModel)
        binding.setVariable(BR.eventHolder, this@BottomSheetAnaylzeUserSelectSubcategory)

        setUpViewModelObserver()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            activityViewModel.closeSheet.collect {
                if(it) {
                    clickedChoiceBtn(viewModel.email.value ?: emptyList())
                    this@BottomSheetAnaylzeUserSelectSubcategory.dismiss()
                }
            }
        }
    }

    override fun onItemClick(item: BookUsers) {
        //사용자 클릭 시 업데이트
        activityViewModel.settingSettlementMember(item)
    }
}