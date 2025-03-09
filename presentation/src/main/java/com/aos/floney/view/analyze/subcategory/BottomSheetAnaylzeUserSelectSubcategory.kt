package com.aos.floney.view.analyze.subcategory

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
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
) :
    BaseBottomSheetFragment<BottomSheetAnalyzeSubcategoryUserBinding, AnalyzeLineSubcategoryViewModel>
        (R.layout.bottom_sheet_analyze_subcategory_user), UiMemberSelectModel.OnItemClickListener {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUserList()
        binding.setVariable(BR.eventHolder, this@BottomSheetAnaylzeUserSelectSubcategory)

        setUpViewModelObserver()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.closeSheet.collect {
                if(it) {
                    dismiss()
                }
            }
        }
    }

    override fun onItemClick(item: BookUsers) {
        //사용자 클릭 시 업데이트
        viewModel.settingSettlementMember(item)
    }
}