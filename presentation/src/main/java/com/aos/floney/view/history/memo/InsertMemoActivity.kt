package com.aos.floney.view.history.memo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.library.baseAdapters.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityInsertMemoBinding
import com.aos.floney.ext.repeatOnStarted

class InsertMemoActivity : BaseActivity<ActivityInsertMemoBinding, InsertMemoViewModel>(R.layout.activity_insert_memo) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setupViewModelObserver()
    }

    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            viewModel.onClickedSaveWriting.collect {
                val intent = Intent()
                intent.putExtra("memo", binding.etInsertMemo.text.toString())
                setResult(Activity.RESULT_OK, intent) // 결과와 데이터를 설정
                finish() // 액티비티 종료
            }
        }
        repeatOnStarted {
            viewModel.onClickedBack.collect {
                val intent = Intent()
                intent.putExtra("memo", "")
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
