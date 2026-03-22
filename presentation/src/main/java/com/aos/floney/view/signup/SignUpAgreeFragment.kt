package com.aos.floney.view.signup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentSignUpAgreeBinding
import com.aos.floney.ext.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignUpAgreeFragment : BaseFragment<FragmentSignUpAgreeBinding, SignUpAgreeViewModel>(R.layout.fragment_sign_up_agree) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelObserver()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 다음 페이지 이동
            viewModel.nextPage.collect {
                if(it) {
                    val action =
                        SignUpAgreeFragmentDirections.actionSignUpAgreeFragmentToSignUpInputEmailFragment(viewModel.marketingTerms.value ?: false)
                    findNavController().navigate(action)
                }
            }
        }

        repeatOnStarted {
            // 이전 페이지 이동
            viewModel.back.collect {
                if(it) {
                    val activity = requireActivity() as SignUpActivity
                    activity.startLoginActivity()
                }
            }
        }

        // 약관 페이지 이동
        viewModel.clickedTerms.observe(viewLifecycleOwner) {
            if(it) {
                val activity = requireActivity() as SignUpActivity
                activity.startWebViewActivity(viewModel.link.value!!, viewModel.subtitle.value!!)
            }
        }
    }
}