package com.aos.floney.view.history.memo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.activity.OnBackPressedCallback
import androidx.databinding.library.baseAdapters.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityInsertMemoBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.EditNotSaveDialog

class InsertMemoActivity : BaseActivity<ActivityInsertMemoBinding, InsertMemoViewModel>(R.layout.activity_insert_memo) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setupViewModelObserver()
        setUpBackPressHandler()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)

        binding.etInsertMemo.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true) // NestedScrollView가 터치 뺏지 못하게
            false // EditText가 원래대로 터치 이벤트 받게
        }

        val receivedData = intent.getStringExtra("memo")
        receivedData?.let { viewModel.initMemo(receivedData) }
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
                goToHistoryActivity()
            }
        }
    }

    private fun goToHistoryActivity() {
        if (!viewModel.originalMemoValue.equals(viewModel.insertMemoValue.value)) {
            // 다이얼로그 표시
            EditNotSaveDialog(this@InsertMemoActivity) {
                finish()
            }.show()
        } else {
            finish()
        }
    }
    private fun setUpBackPressHandler() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToHistoryActivity()
            }
        })
    }
}
