package com.aos.floney.ext

import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.checkDecimalPoint
import com.aos.data.R
import com.aos.model.analyze.Asset
import com.aos.model.analyze.UiAnalyzePlanModel
import com.aos.model.settlement.SettlementMoneyState
import com.suke.widget.SwitchButton
import timber.log.Timber
import java.text.DecimalFormat
import kotlin.math.abs


fun String.formatNumber(): String {
     return if(this != "") {
         val text = this.replace(CurrencyUtil.currency, "")

         if (text.endsWith(".")){
             text
         }
         else if (checkDecimalPoint() && text.length>=15){
             "999,999,999.99"
         }
         else if (!checkDecimalPoint() && text.length>=15) {
             "99,999,999,999"
         }
         else if(text != "") {
             DecimalFormat("#,###.##").format(text.replace(",", "").toDouble())
         } else {
             ""
         }
    } else {
        ""
    }
}

@BindingAdapter("bind:setPlanText")
fun TextView.setPlanText(item: UiAnalyzePlanModel?) {
    this.text = item?.budgetStatusText ?: ""
}

@BindingAdapter("setDynamicMarginTop")
fun TextView.setDynamicMarginTop(isZeroWon: Boolean) {
    val marginInDp = if (isZeroWon) 26 else 10
    val density = this.context.resources.displayMetrics.density
    val marginInPx = (marginInDp * density).toInt()

    this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        topMargin = marginInPx
    }
}

@BindingAdapter("bind:setAssetMonthText")
fun TextView.setAssetMonthText(item: Asset?){
    if(item != null) {
        this.text = item.month.toString()
    } else {
        this.text = ""
    }
}
@BindingAdapter("bind:setTextViewWeight")
fun TextView.setLayoutWeight(weight: Float) {
    val layoutParams = this.layoutParams as LinearLayout.LayoutParams
    layoutParams.weight = weight
    this.layoutParams = layoutParams
}

@BindingAdapter("bind:setViewWeight")
fun View.setLayoutWeight(weight: Float) {
    val layoutParams = this.layoutParams as LinearLayout.LayoutParams
    layoutParams.weight = weight
    this.layoutParams = layoutParams
}


@BindingAdapter("bind:setTextWithEllipsis")
fun TextView.setTextWithEllipsis(text: String?) {
    text?.let {
        if (it.length > 10) {
            this.text = "${it.substring(0, 10)}..."
        } else {
            this.text = it
        }
    }
}

@BindingAdapter("bind:adjustTotalMoneyText")
fun TextView.adjustTotalMoneyText(amount: String?) {
    amount?.let {
        Timber.e("amount : ${amount}")

        // 금액에 따른 최대 글자 단위 조정
        val isNegative = if(amount.startsWith("-")) "-" else ""
        var filterAmount = amount.replace(CurrencyUtil.currency, "")

        val displayAmount = when {
            checkDecimalPoint() && filterAmount.length >= 15 -> {
                "${isNegative}999,999,999.99"
            }
            !checkDecimalPoint() && filterAmount.length >= 15 -> {
                "${isNegative}99,999,999,999"
            }
            else -> {
                filterAmount
            }
        }

        // 자릿수에 따른 글자 크기 조정
        val amountValue = filterAmount.replace(",", "").toLongOrNull()?.let { abs(it) } ?: return

        when {
            amountValue < 1_000_000_000 -> {
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            }
            amountValue in 1_000_000_000..99_999_999_999 -> {
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            }
            else -> {
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            }
        }

        this.text = "$displayAmount${CurrencyUtil.currency}"
    }
}

@BindingAdapter("bind:adjustOnlyMoneyText")
fun TextView.adjustOnlyMoneyText(amount: String?) {
    amount?.let {
        Timber.e("amount : ${amount}")
        var amount = amount.replace(CurrencyUtil.currency, "")
        val amountValue = amount.replace(",", "").toLongOrNull() ?: return

        val displayAmount = when {
            checkDecimalPoint() && amount.length >= 15 -> {
                "999,999,999.99"
            }
            !checkDecimalPoint() && amount.length >= 15 -> {
                "99,999,999,999"
            }
            else -> {
                amount
            }
        }

        this.text = "$displayAmount${CurrencyUtil.currency}"
    }
}
@BindingAdapter("bind:adjustDayMoneyText")
fun TextView.adjustDayMoneyText(amount: String?) {
    amount?.let{
        val amountValue = kotlin.math.abs(amount.replace(",", "").toDoubleOrNull() ?: return)
        when {
            amountValue < 1_000_000_000 -> {
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9f)
                this.text = amount
            }
            amountValue >= 1_000_000_000f -> {
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8f)
                val adjustAmount = when {
                    checkDecimalPoint() -> {
                        "999,999,999.99"
                    }

                    !checkDecimalPoint() -> {
                        "99,999,999,999"
                    }
                    else -> {
                        amount
                    }
                }
                this.text = "$adjustAmount.."
            }
        }
    }
}

@BindingAdapter("bind:setLayoutMargin")
fun TextView.setLayoutMargin(margin: Float) {
    val layoutParams = this.layoutParams as MarginLayoutParams
    val marginPx = (margin * this.context.resources.displayMetrics.density).toInt()
    layoutParams.setMargins(marginPx, marginPx, marginPx, marginPx)
    this.layoutParams = layoutParams
}

@BindingAdapter("bind:adjustDesign")
fun SwitchButton.adjustDesign(isMarketingTerms: Boolean) {
    
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("bind:adjustCategoryFont")
fun TextView.adjustCategoryFont(isBold: Boolean) {
    if (isBold) {
        this.setTypeface(resources.getFont(com.aos.floney.R.font.pretendard_semibold), Typeface.NORMAL)
    } else {
        this.setTypeface(resources.getFont(com.aos.floney.R.font.pretendard_medium), Typeface.NORMAL)
    }
}

@BindingAdapter("bind:setProfileMargin")
fun TextView.setProfileMargin(status: Boolean) {

    val marginStartValue = if (status) {
        16 // seeProfileStatus가 true인 경우 marginStart는 16
    } else {
        20 // seeProfileStatus가 false인 경우 marginStart는 20
    }

    val layoutParams = this.layoutParams as MarginLayoutParams
    layoutParams.marginStart = (marginStartValue * context.resources.displayMetrics.density).toInt()
    this.layoutParams = layoutParams
}

@BindingAdapter("bind:setSubscribeMarginTop")
fun TextView.setSubscribeMarginTop(status: Boolean) {

    val marginTopValue = if (status) {
        16
    } else {
        10
    }

    val layoutParams = this.layoutParams as MarginLayoutParams
    layoutParams.topMargin = (marginTopValue * context.resources.displayMetrics.density).toInt()
    this.layoutParams = layoutParams
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("settlementMoneyText", "settlementState")
fun TextView.setSettlementMoneyText(money: String?, state: SettlementMoneyState?) {
    if (state == null) return

    if (state == SettlementMoneyState.NONE) {
        text = context.getString(R.string.settlement_no_money)
        return
    }

    val resId = when (state) {
        SettlementMoneyState.SEND -> R.string.settlement_send_money
        SettlementMoneyState.RECEIVE -> R.string.settlement_receive_money
        else -> return
    }

    val fullText = context.getString(resId, money)
    val spannable = SpannableString(fullText)

    if (!money.isNullOrEmpty()) {
        val start = fullText.indexOf(money)
        if (start >= 0) {
            val customTypeface = resources.getFont(com.aos.floney.R.font.pretendard_bold)

            spannable.setSpan(
                CustomTypefaceSpan(customTypeface),
                start,
                start + money.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    text = spannable
}

class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(tp: TextPaint) {
        apply(tp)
    }

    override fun updateMeasureState(tp: TextPaint) {
        apply(tp)
    }

    private fun apply(paint: TextPaint) {
        val oldTypeface = paint.typeface
        val oldStyle = oldTypeface?.style ?: 0

        val fakeStyle = oldStyle and typeface.style.inv()

        if (fakeStyle and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fakeStyle and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = typeface
    }
}

