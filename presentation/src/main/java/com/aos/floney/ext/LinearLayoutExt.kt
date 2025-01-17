package com.aos.floney.ext

import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.aos.floney.R
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat

@BindingAdapter("setDynamicPaddingBottom")
fun LinearLayout.setDynamicPaddingBottom(condition: Boolean) {
    val paddingBottom = if (condition) 16 else 70
    val paddingBottomPx = dpToPx(context, paddingBottom)
    this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, paddingBottomPx)
}

fun dpToPx(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
}


@BindingAdapter("bind:setWalletMargin")
fun LinearLayout.setWalletMargin(status: Boolean) {
    val marginTopValue = if (status) 16 else 0

    val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.topMargin = (marginTopValue * context.resources.displayMetrics.density).toInt()
    this.layoutParams = layoutParams
}