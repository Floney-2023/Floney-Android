package com.aos.floney.view.history.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentInsertMemoBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.history.HistoryViewModel

class InsertMemoFragment : BaseFragment<FragmentInsertMemoBinding, InsertMemoViewModel>(R.layout.fragment_insert_memo) {

    private val historyViewModel: HistoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setupViewModelObserver()
    }

    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            viewModel.onClickedSaveWriting.collect {
                historyViewModel.setMemoValue(it)
            }
        }
        repeatOnStarted {
            viewModel.onClickedBack.collect {
                findNavController().popBackStack()
            }
        }
    }
}