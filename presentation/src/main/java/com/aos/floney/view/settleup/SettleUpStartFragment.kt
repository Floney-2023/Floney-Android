package com.aos.floney.view.settleup

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentSettleUpStartBinding
import com.aos.floney.ext.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettleUpStartFragment :
    BaseFragment<FragmentSettleUpStartBinding, SettleUpStartViewModel>(R.layout.fragment_settle_up_start) {
    private val activityViewModel: SettleUpViewModel by activityViewModels()

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }

    private fun setUpUi() {
        activityViewModel.bottomSee(true)
    }

    private fun setUpViewModelObserver() {

        repeatOnStarted {
            viewModel.settleUpStartPage.collect {
                // 구독 만료 시, 적용 팝업 표시
                if (activityViewModel.subscribeExpired.value!!) {
                    activityViewModel.showSubscribePopupIfNeeded()
                } else { // 정산 시작하기 이동
                    val action =
                        SettleUpStartFragmentDirections.actionSettleUpStartFragmentToSettleUpMemberSelectFragment()
                    findNavController().navigate(action)
                }
            }
        }

        repeatOnStarted {
            // 정산 내역 보기 이동
            viewModel.settleUpSeePage.collect {
                if (it) {
                    val action =
                        SettleUpStartFragmentDirections.actionSettleUpStartFragmentToSettleUpSeeFragment(
                            -1,
                            ""
                        )
                    findNavController().navigate(action)
                }
            }
        }

        repeatOnStarted {
            activityViewModel.sharePage.collect {
                if (it) {
                    val action =
                        SettleUpStartFragmentDirections.actionSettleUpStartFragmentToSettleUpSeeFragment(
                            activityViewModel.id.value!!,
                            activityViewModel.bookKey.value!!
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

}