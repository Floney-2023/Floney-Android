package com.aos.floney.view.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.aos.floney.databinding.DialogChoiceImageBinding
import com.aos.floney.databinding.DialogChoicePictureBinding
import com.aos.floney.databinding.DialogDeletePcitureBinding

class DeletePictureDialog(context: Context, private val onClickYes: () -> Unit): Dialog(context) {
    private lateinit var binding: DialogDeletePcitureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDeletePcitureBinding.inflate(layoutInflater)
        setUpUi()
        onClickBtnListener()

        setContentView(binding.root)
    }

    private fun setUpUi() {
        this.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    private fun onClickBtnListener() {
        binding.btnYes.setOnClickListener {
            onClickYes()
            dismiss()
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }
    }
}