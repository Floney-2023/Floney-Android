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
import com.aos.floney.databinding.WarningPopupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WarningPopupDialog(
    val title : String,
    val info : String,
    val leftButton : String,
    val rightButton : String,
    val check : Boolean,
    private val onSelect: (Boolean) -> Unit) :
    DialogFragment(){

    private var _binding: WarningPopupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = WarningPopupBinding.inflate(inflater, container, false)
        val view = binding.root

        setUpUi()
        setUpListener()
        return view
    }
    private fun setUpUi()
    {
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding.apply {
            tvPopupTitle.text = title
            tvPopupInfo.text = info
            btnLeft.text = leftButton
            btnRight.text = rightButton

            // check true면 black Popup, false면 red Popup
            if (check) {
                ivPopupImage.setImageResource(R.drawable.item_black_exit_popup)
            }

            if (leftButton.isBlank()){
                btnLeft.visibility = View.GONE
                viewPadding.visibility = View.GONE
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