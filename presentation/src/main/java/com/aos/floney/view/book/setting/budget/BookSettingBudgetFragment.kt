package com.aos.floney.view.book.setting.budget

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingBudgetBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.analyze.AnalyzeViewModel
import com.aos.floney.view.analyze.ChoiceDatePickerBottomSheet
import com.aos.floney.view.common.SuccessToastDialog
import com.aos.model.book.BudgetItem
import com.aos.model.book.UiBookBudgetModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalStateException

@AndroidEntryPoint
class BookSettingBudgetFragment : BaseFragment<FragmentBookSettingBudgetBinding, BookSettingBudgetViewModel>(R.layout.fragment_book_setting_budget) , UiBookBudgetModel.OnItemClickListener {

    private var listener: OnFragmentInteractionListener? = null
    interface OnFragmentInteractionListener {
        fun onFragmentRemoved()
    }

    // 뒤로 가기 버튼을 처리하기 위한 콜백 생성
    val callback = object : OnBackPressedCallback(true) { // `true`는 콜백이 활성화되어 있다는 의미
        override fun handleOnBackPressed() {
            // 여기에 뒤로 가기 버튼이 눌렸을 때 실행할 코드를 작성합니다.
            Timber.e("뒤로 가기 버튼이 눌렸습니다")
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                listener?.onFragmentRemoved()
            }
        }
    }

    override fun onItemClick(item: BudgetItem) {
        viewModel.settingBudget(item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this@BookSettingBudgetFragment)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 이전 페이지 이동
            viewModel.back.collect {
                if(it) {
                    try {
                        findNavController().popBackStack()
                    } catch (e: Exception) {
                        listener?.onFragmentRemoved()
                    }
                }
            }
        }

        repeatOnStarted {
            // 예산 설정 bottomSheet
            viewModel.budgetSettingPage.collect {
                if(it.month!="") {
                    val bottomSheetFragment = BookSettingBudgetBottomSheetFragment(
                        it, viewModel.convertMonthStringToDateString(it.month)) { updateBudgetMoney ->
                        viewModel.updateBudget(it, updateBudgetMoney)
                    }
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
        repeatOnStarted {
            // 예산 설정 bottomSheet
            viewModel.yearSetting.collect {
                ChoiceYearPickerBottomSheet(requireContext(), viewModel.year.value!!) {

                    val successToast = SuccessToastDialog(requireContext(), getString(R.string.toast_change_successed))
                    successToast.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        successToast.dismiss()
                    }, 2000)

                    // 결과값
                    val item = it.split("-")
                    viewModel.getBudgetInform(item[0].toInt().toString(),it)
                }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        callback.remove()
        callback.isEnabled = false
        Timber.e("onDestroyView")
    }
}