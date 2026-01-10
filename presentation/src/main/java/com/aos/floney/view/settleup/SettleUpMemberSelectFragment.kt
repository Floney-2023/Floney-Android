package com.aos.floney.view.settleup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentSettleUpMemberSelectBinding
import com.aos.floney.databinding.FragmentSettleUpStartBinding
import com.aos.floney.databinding.FragmentSignUpAgreeBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.signup.SignUpActivity
import com.aos.model.home.MonthMoney
import com.aos.model.settlement.BookUsers
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.model.user.MyBooks
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SettleUpMemberSelectFragment : BaseFragment<FragmentSettleUpMemberSelectBinding, SettleUpMemberSelectViewModel>(R.layout.fragment_settle_up_member_select) , UiMemberSelectModel.OnItemClickListener {
    private val activityViewModel: SettleUpViewModel by activityViewModels()
    override fun onItemClick(item: BookUsers) {
        viewModel.settingSettlementMember(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }

    private fun setUpUi() {
        activityViewModel.bottomSee(false)
        binding.setVariable(BR.eventHolder, this@SettleUpMemberSelectFragment)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 다음 페이지 이동
            viewModel.nextPage.collect {
                if(it.isNotEmpty()) {
                    val action =
                        SettleUpMemberSelectFragmentDirections.actionSettleUpMemberSelectFragmentToSettleUpPeriodSelectFragment(it)
                    findNavController().navigate(action)
                }
            }
        }

        repeatOnStarted {
            // 처음 정산 페이지로
            viewModel.settlementPage.collect {
                if(it) {
                    val activity = requireActivity() as SettleUpActivity
                    activity.startSettleUpActivity()
                }
            }
        }

    }

}