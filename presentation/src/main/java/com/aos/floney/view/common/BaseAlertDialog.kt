package com.aos.floney.view.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.aos.floney.R
import com.aos.floney.databinding.BaseAlertDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaseAlertDialog(
    val title : String,
    val info : String,
    val check : Boolean,
    private val onSelect: (Boolean) -> Unit) :
    DialogFragment(){

    private var _binding: BaseAlertDialogBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다이얼로그가 취소되지 않도록 설정
        isCancelable = false
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BaseAlertDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        setUpUi()
        setUpListener()
        return view
    }
    private fun setUpUi() {
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
            attributes = attributes?.apply {
                dimAmount = 0.2f // 20% 딤 효과
            }
        }

        binding.apply {
            tvPopupTitle.text = title
            tvPopupInfo.text = info

            // 단일 버튼 모드 체크 (초대 코드 복사 또는 알림)
            val isSingleButtonMode = title == "초대 코드 복사" || title.contains("알림")

            if (isSingleButtonMode) {
                // 단일 버튼 모드: 왼쪽 버튼만 사용, 전체 너비
                btnLeft.text = "OK"
                btnRight.visibility = View.GONE
                middleView.visibility = View.GONE

                // ConstraintLayout으로 왼쪽 버튼을 전체 너비로 설정
                val params =
                    btnLeft.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.endToEnd =
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                btnLeft.layoutParams = params
            } else {
                // 두 버튼 모드: 기본 설정 유지
                btnRight.visibility = View.VISIBLE
                middleView.visibility = View.VISIBLE
            }

            // check 값에 따라 왼쪽 버튼의 글씨색 변경
            btnLeft.setTextColor(
                if (check) Color.RED else ContextCompat.getColor(
                    requireContext(),
                    R.color.grayscale2
                )
            )
        }
    }

    private fun setUpListener()
    {
        binding.btnLeft.setOnClickListener {
            onSelect(true)
            dismiss()
        }

        binding.btnRight.setOnClickListener {
            onSelect(false)
            dismiss()
        }
    }
}
