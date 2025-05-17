package com.aos.floney.view.signup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentSignUpInputInfoBinding
import com.aos.floney.ext.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpInputInfoFragment : BaseFragment<FragmentSignUpInputInfoBinding, SignUpInputInfoViewModel>(R.layout.fragment_sign_up_input_info) {
    private val activityViewModel: SignUpViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.setSocialInfo(activityViewModel.getSocialUserModel()?.nickname ?: "", activityViewModel.getSocialUserModel()?.token ?: "", activityViewModel.getSocialUserModel()?.provider ?: "", activityViewModel.getSocialUserModel()?.email ?: "")
        }, 100)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelObserver()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.nextPage.collect() {
                if (it) {
                    val activity = requireActivity() as SignUpActivity
                    activity.startBookAddActivity()
                }
            }
        }

        repeatOnStarted {
            // 이전 페이지 이동
            viewModel.back.collect {
                if(it) {
                    if(activityViewModel.getSocialUserModel()?.email != "") {
                        activityViewModel.onClickedBack()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        }

        repeatOnStarted {
            // 이전 이메일 값 셋팅
            viewModel.socialEmail.collect {
                if(it.isNotEmpty()) {
                    binding.etEmail.text = it
                }
            }
        }
    }
}