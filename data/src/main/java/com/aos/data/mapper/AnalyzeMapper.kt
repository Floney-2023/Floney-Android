package com.aos.data.mapper

import android.content.Context
import com.aos.data.R
import com.aos.data.entity.response.analyze.PostAnalyzeAssetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeBudgetEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryInComeEntity
import com.aos.data.entity.response.analyze.PostAnalyzeCategoryOutComeEntity
import com.aos.data.entity.response.analyze.PostAnalyzeLineSubCategoryEntity
import com.aos.data.util.CurrencyUtil
import com.aos.model.analyze.AnalyzeResult
import com.aos.model.analyze.Asset
import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.model.analyze.UiAnalyzeCategoryInComeModel
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import com.aos.model.analyze.UiAnalyzeLineSubCategoryModel
import com.aos.model.analyze.UiAnalyzePlanModel
import com.aos.model.analyze.BookSubData
import timber.log.Timber
import java.text.NumberFormat
import java.util.Calendar
import kotlin.math.pow
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Locale

private val colorUsedArr = arrayListOf<Int>()
private val colorArr = listOf<Int>(
    "#AD1F25".toColorInt(), // red1
    "#EFA9AB".toColorInt(), // red3
    "#FF5C00".toColorInt(), // orange2
    "#FFBE99".toColorInt(), // orange4
    "#E4BF00".toColorInt(), // yellow1
    "#FFEE97".toColorInt(), // yellow3
    "#0060E5".toColorInt(), // blue1
    "#99C4FF".toColorInt(), // blue4
    "#4A48B0".toColorInt(), // indigo2
    "#706EC4".toColorInt(), // indigo3
    "#654CFF".toColorInt(), // purple1
    "#D3CCFF".toColorInt() // purple3
)

private val randomColorArr = arrayListOf<Int>()
private var stepIdx = 0
private var colorIdx = 0

fun PostAnalyzeCategoryOutComeEntity.toUiAnalyzeModel(context: Context): UiAnalyzeCategoryOutComeModel {
    colorIdx = 0
    stepIdx = 0
    colorUsedArr.clear()
    randomColorArr.clear()
    randomColorArr.addAll(getRandomColor(this.analyzeResult.size))

    val formattedTotal = "${NumberFormat.getNumberInstance().format(this.total)}${CurrencyUtil.currency}"

    return UiAnalyzeCategoryOutComeModel(
        total = context.getString(R.string.analyze_outcome_total, formattedTotal),
        differance = if (differance < 0) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(this.differance * -1.0)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_outcome_less_than_last, formattedDiff)
        } else if (this.differance == this.total) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(differance)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_outcome_more_than_last, formattedDiff)
        } else if (this.differance > this.total) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(this.differance - total)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_outcome_more_than_last, formattedDiff)
        } else if (total > differance) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(differance)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_outcome_more_than_last, formattedDiff)
        } else {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(differance)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_outcome_less_than_last, formattedDiff)
        },
        size = this.analyzeResult.size,
        analyzeResult = this.analyzeResult.map {
            AnalyzeResult(
                category = it.category,
                money = it.money,
                uiMoney = "${
                    NumberFormat.getNumberInstance().format(it.money)
                }${CurrencyUtil.currency}",
                percent = (it.money / total) * 100.0,
                uiPercent = "${((it.money / total) * 100.0).roundToInt()}%",
                color = when (stepIdx) {
                    0 -> {
                        stepIdx++
                        "#FFDE31".toColorInt() // yellow#2
                    }

                    1 -> {
                        stepIdx++
                        "#FF965B".toColorInt() // orange#3
                    }

                    2 -> {
                        stepIdx++
                        "#E56E73".toColorInt() // red#2
                    }

                    else -> {
                        try {
                            randomColorArr[colorIdx++]
                        }catch (i: IndexOutOfBoundsException) {
                            i.printStackTrace()
                            randomColorArr[0]
                        }
                    }
                }
            )
        })
}

fun PostAnalyzeCategoryInComeEntity.toUiAnalyzeModel(context: Context): UiAnalyzeCategoryInComeModel {
    colorIdx = 0
    stepIdx = 0
    colorUsedArr.clear()
    randomColorArr.clear()
    randomColorArr.addAll(getRandomColor(this.analyzeResult.size))

    Timber.e("different $differance")
    Timber.e("total $total")

    val formattedTotal = "${NumberFormat.getNumberInstance().format(this.total)}${CurrencyUtil.currency}"

    return UiAnalyzeCategoryInComeModel(
        total = context.getString(R.string.analyze_income_total, formattedTotal),
        differance = if (differance < 0 && total == 0.0) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(this.differance * -1.0)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_income_less_than_last, formattedDiff)
        } else if (this.differance == this.total) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(differance)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_income_more_than_last, formattedDiff)
        } else if (this.differance > this.total) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(this.differance - total)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_income_more_than_last, formattedDiff)
        } else if (differance < 0 && total > differance) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(differance * -1.0)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_income_more_than_last, formattedDiff)
        } else {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(differance)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_income_more_than_last, formattedDiff)
        },
        size = this.analyzeResult.size,
        analyzeResult = this.analyzeResult.map {
            AnalyzeResult(
                category = it.category,
                money = it.money,
                uiMoney = "${
                    NumberFormat.getNumberInstance().format(it.money)
                }${CurrencyUtil.currency}",
                percent = (it.money / total) * 100.0,
                uiPercent = "${((it.money / total) * 100.0).roundToInt()}%",
                color = when (stepIdx) {
                    0 -> {
                        stepIdx++
                        "#4C97FF".toColorInt() // blue#3
                    }

                    1 -> {
                        stepIdx++
                        "#35347F".toColorInt() // indigo#1
                    }

                    2 -> {
                        stepIdx++
                        "#9B8BFF".toColorInt() // purple#2
                    }

                    else -> {
                        try {
                            randomColorArr[colorIdx++]
                        }catch (i: IndexOutOfBoundsException) {
                            i.printStackTrace()
                            randomColorArr[0]
                        }
                    }
                }
            )
        })
}

fun PostAnalyzeBudgetEntity.toUiAnalyzePlanModel(context: Context): UiAnalyzePlanModel {
    val tempLeftMoney = "${
        NumberFormat.getNumberInstance().format(this.leftMoney)
    }${CurrencyUtil.currency}".substring(
        1,
        "${NumberFormat.getNumberInstance().format(this.leftMoney)}${CurrencyUtil.currency}".length
    )

    // 남은 일 수 계산
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val remainingDays = lastDayOfMonth - today + 1

    val percentValue = if (this.leftMoney < 0) {
        "100"
    } else {
        (((this.initBudget - this.leftMoney) / this.initBudget) * 100).toInt().toString()
    }

    val divMoneyValue = "${
        if (leftMoney > 0) {
            NumberFormat.getNumberInstance().format((this.leftMoney / remainingDays).roundToInt())
        } else {
            0
        }
    }${CurrencyUtil.currency}".replace("-", "")

    // Budget status text 결정
    val budgetStatusText = if (this.initBudget == 0.0) {
        context.getString(R.string.analyze_budget_no_budget)
    } else {
        when (percentValue.toIntOrNull() ?: 0) {
            in 0..30 -> context.getString(R.string.analyze_budget_plenty)
            in 31..60 -> context.getString(R.string.analyze_budget_reduce)
            in 61..100 -> context.getString(R.string.analyze_budget_careful)
            else -> context.getString(R.string.analyze_budget_careful)
        }
    }

    return UiAnalyzePlanModel(
        initBudget = "${
            NumberFormat.getNumberInstance().format(this.initBudget)
        }${CurrencyUtil.currency}",
        leftMoney = "${
            NumberFormat.getNumberInstance().format(this.leftMoney)
        }${CurrencyUtil.currency}",
        percent = percentValue,
        divMoney = divMoneyValue,
        budgetUsedText = context.getString(R.string.analyze_budget_used, percentValue),
        budgetPerDayText = context.getString(R.string.analyze_budget_per_day, divMoneyValue),
        budgetStatusText = budgetStatusText
    )
}

fun PostAnalyzeAssetEntity.toUiAnalyzeAssetModel(context: Context): UiAnalyzeAssetModel {
    val listAsset = arrayListOf<Asset>()
    var maxAsset = 0.0

    for ((key, value) in this.assetInfo) {
        // 최대 자산 저장
        if (maxAsset < value) {
            maxAsset = value
        }
    }

    for ((key, value) in this.assetInfo) {
        // 최대 자산 저장
        if (value <= 0.0) {
            listAsset.add(
                Asset(
                    key.substring(key.length - 2, key.length).toInt(),
                    1f
                )
            )
        } else {
            listAsset.add(
                Asset(
                    key.substring(key.length - 2, key.length).toInt(),
                    (value / maxAsset).toFloat() * 100
                )
            )
        }
    }

    return UiAnalyzeAssetModel(
        totalDifference = if (this.difference > 0) {
            context.getString(R.string.analyze_asset_increased)
        } else if (this.difference.toInt() == 0) {
            context.getString(R.string.analyze_asset_same)
        } else {
            context.getString(R.string.analyze_asset_decreased)
        },
        difference = if (this.difference >= 0) {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(this.difference)}${CurrencyUtil.currency}"
            context.getString(R.string.analyze_asset_up, formattedDiff)
        } else {
            val formattedDiff = "${NumberFormat.getNumberInstance().format(this.difference)}${CurrencyUtil.currency}".replace("-", "")
            context.getString(R.string.analyze_asset_down, formattedDiff)
        },
        initAsset = "${
            NumberFormat.getNumberInstance().format(this.initAsset)
        }${CurrencyUtil.currency}",
        currentAsset = "${
            NumberFormat.getNumberInstance().format(this.currentAsset)
        }${CurrencyUtil.currency}",
        analyzeResult = listAsset
    )
}

// 상세 분석-지출/수입

fun PostAnalyzeLineSubCategoryEntity.toUiLineSubCategoryModel(
    context: Context,
    category: String
): UiAnalyzeLineSubCategoryModel {

    return UiAnalyzeLineSubCategoryModel(
        subcategoryName = this.subcategoryName,
        bookLines = this.bookLines.map {

            val formattedDate = formatLocalizedDate(context, it.lineDate)

            BookSubData(
                money = "${if (category == "수입") "+" else "-"}${
                    NumberFormat.getNumberInstance().format(it.money)
                }",

                descriptionDetail = context.getString(
                    R.string.line_detail_format,
                    it.asset,
                    formattedDate
                ),

                description = it.description,
                userProfileImg = it.userProfileImg ?: ""
            )
        }
    )
}


fun formatLocalizedDate(context: Context, dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        val pattern = context.getString(R.string.book_date_pattern)
        val outputFormat = SimpleDateFormat(pattern, Locale.getDefault())

        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}


// 랜덤 색상 가져오기 그래프는 3개까지는 색상 고정 그 이후는 랜덤 색상임
fun getRandomColor(repeat: Int): List<Int> {
    val colorCount = colorArr.size
    val indices = (0 until colorCount).shuffled().take(repeat)
    return indices.map { colorArr[it] }
}

fun Double.round(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).roundToInt() / factor
}

private fun getConvertReceiveRepeatValue(context: Context, value: String): String {
    Timber.e("value $value")
    return when(value) {
        "LATEST" -> context.getString(R.string.sort_latest)
        "OLDEST" -> context.getString(R.string.sort_oldest)
        "USER_NICKNAME" -> context.getString(R.string.sort_user_nickname)
        "LINE_SUBCATEGORY_NAME" -> context.getString(R.string.sort_subcategory)
        else -> ""
    }
}