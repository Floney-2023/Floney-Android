package com.aos.floney.view.mypage.inform.withdraw

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.aos.data.util.CommonUtil
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentMyPageWithdrawReasonCheckBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.WarningPopupDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageInformWithdrawReasonCheckFragment :
    BaseFragment<FragmentMyPageWithdrawReasonCheckBinding, MyPageInformWithdrawReasonCheckViewModel>(R.layout.fragment_my_page_withdraw_reason_check) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelObserver()
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.back.collect() {
                if(it) {
                    findNavController().popBackStack()
                }
            }
        }
        repeatOnStarted {
            viewModel.nextPage.collect() {
                if(it.isNotEmpty()) {
                    val reasonType = it
                    val reason = viewModel.directInputText.value ?: ""

                    if (viewModel.subscribeCheck.value!!) // 구독 결제가 유효한 경우 (구독 해지 안한 경우)
                    {
                        val subScribeDialogFragment = WarningPopupDialog(
                            getString(R.string.unsubscribe_exit_popup_title),
                            getString(R.string.unsubscribe_exit_popup_info),
                            "",
                            getString(R.string.already_pick_button),
                            false
                        ) {}
                        subScribeDialogFragment.show(parentFragmentManager, "WithdrawDialog")
                    }
                    else {
                        if (CommonUtil.provider == "EMAIL"){ // 이메일 유저 탈퇴 시, 비밀번호 입력
                            val withdrawPopupAction =
                                MyPageInformWithdrawReasonCheckFragmentDirections.actionMyPageInformWithdrawReasonCheckFragmentToMyPageInformWithdrawInputPasswordFragment(
                                    reasonType,
                                    reason
                                )
                            findNavController().navigate(withdrawPopupAction)
                        } else { // 간편 로그인 유저일 경우, 비밀번호 입력 없이 바로 탈퇴 팝업
                            val exitDialogFragment = WarningPopupDialog(
                                getString(R.string.mypage_main_inform_exit_popup_title),
                                getString(R.string.mypage_main_inform_exit_popup_info),
                                getString(R.string.mypage_main_inform_exit_popup_btn_exit),
                                getString(R.string.mypage_main_inform_exit_popup_btn_cancel),
                                false
                            ) { checked ->
                                if (checked) {
                                    viewModel.requestWithdraw(reasonType, reason)
                                }
                            }
                            exitDialogFragment.show(parentFragmentManager, "WithdrawDialog")
                        }
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.withdrawPage.collect() {// 탈퇴 완료
                if(it) {
                    val action =
                        MyPageInformWithdrawReasonCheckFragmentDirections.actionMyPageInformWithdrawReasonCheckFragmentToMyPageInformWithdrawCompleteFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }
}