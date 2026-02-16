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
import androidx.core.graphics.drawable.toDrawable

@AndroidEntryPoint
class BaseAlertDialog(
    val title : String,
    val info : String,
    val check : Boolean,
    val buttonMode: ButtonMode = ButtonMode.DOUBLE,
    private val onSelect: (Boolean) -> Unit) :
    DialogFragment(){

    enum class ButtonMode {
        SINGLE,
        DOUBLE,
    }

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
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    private fun setUpUi() {
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            requestFeature(Window.FEATURE_NO_TITLE)
            attributes = attributes?.apply {
                dimAmount = 0.2f // 20% 딤 효과
            }
        }

        binding.apply {
            tvPopupTitle.text = title
            tvPopupInfo.text = info

            val isSingleButtonMode = buttonMode == ButtonMode.SINGLE

            if (isSingleButtonMode) {
                // 단일 버튼 모드: 왼쪽 버튼만 사용, 전체 너비
                btnLeft.text = getString(R.string.already_pick_button)
                btnRight.visibility = View.GONE
                middleView.visibility = View.GONE

                val params = btnLeft.layoutParams as LinearLayout.LayoutParams
                params.width = LinearLayout.LayoutParams.MATCH_PARENT
                btnLeft.layoutParams = params
            } else {
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
