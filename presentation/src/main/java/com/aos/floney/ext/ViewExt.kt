package com.aos.floney.ext

import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.checkDecimalPoint
import com.aos.floney.R
import com.aos.model.analyze.Asset
import com.aos.model.analyze.UiAnalyzePlanModel
import com.suke.widget.SwitchButton
import timber.log.Timber
import java.text.DecimalFormat

// 값에 따라 topMargin 값 바꾸기
@BindingAdapter("bind:setViewMarginTop")
fun View.setViewMarginTop(value: Int) {

    val marginTopValue = value

    val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.topMargin = (marginTopValue * context.resources.displayMetrics.density).toInt()
    this.layoutParams = layoutParams
}