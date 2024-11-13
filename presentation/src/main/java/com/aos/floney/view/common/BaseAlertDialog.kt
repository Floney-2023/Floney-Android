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
            btnLeft.apply {
                // '초대 코드 복사'가 맞으면 weight를 2로 설정, OK로 변경
                if (title == "초대 코드 복사" || title.contains("알림")) {
                    btnLeft.text = "OK"
                    middleView.visibility = View.GONE
                    val params = layoutParams as LinearLayout.LayoutParams
                    params.weight = 2f
                    layoutParams = params
                }
                // 'check' 값에 따라 버튼의 글씨색 변경
                setTextColor(
                    if (check) Color.RED else ContextCompat.getColor(requireContext(), R.color.grayscale2)
                )
            }
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