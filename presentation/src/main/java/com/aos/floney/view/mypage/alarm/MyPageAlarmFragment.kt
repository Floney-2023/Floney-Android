package com.aos.floney.view.mypage.alarm

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentMyPageAlarmBinding
import com.aos.model.alarm.UiAlarmGetModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyPageAlarmFragment : BaseFragment<FragmentMyPageAlarmBinding, MyPageAlarmViewModel>(R.layout.fragment_my_page_alarm), UiAlarmGetModel.OnItemClickListener {

    companion object {
        private const val ARG_BOOK_KEY = "book_key"

        fun newInstance(bookKey: String): MyPageAlarmFragment {
            val fragment = MyPageAlarmFragment()
            val args = Bundle()
            args.putString(ARG_BOOK_KEY, bookKey)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }

    override fun onResume() {
        super.onResume()
        setUpAlarmData()
    }
    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
        binding.setVariable(BR.eventHolder, this@MyPageAlarmFragment)
    }
    private fun setUpAlarmData(){
        val bookKey = arguments?.getString(ARG_BOOK_KEY)
        viewModel.getAlarmInform(bookKey!!) // 가계부 정보 읽어오기
    }
    override fun onItemClick(item: UiAlarmGetModel) {
    }
}