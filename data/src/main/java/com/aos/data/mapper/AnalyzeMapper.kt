package com.aos.data.mapper

import android.graphics.Color
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
import kotlin.random.Random

private val colorUsedArr = arrayListOf<Int>()
private val colorArr = listOf<Int>(
    Color.parseColor("#AD1F25"),
    Color.parseColor("#EFA9AB"),
    Color.parseColor("#FF5C00"),
    Color.parseColor("#FFBE99"),
    Color.parseColor("#E4BF00"),
    Color.parseColor("#FFEE97"),
    Color.parseColor("#0060E5"),
    Color.parseColor("#4C97FF"),
    Color.parseColor("#99C4FF"),
    Color.parseColor("#35347F"),
    Color.parseColor("#4A48B0"),
    Color.parseColor("#706EC4"),
    Color.parseColor("#654CFF"),
    Color.parseColor("#9B8BFF"),
    Color.parseColor("#D3CCFF")
)

private val randomColorArr = arrayListOf<Int>()
private var stepIdx = 0
private var colorIdx = 0

fun PostAnalyzeCategoryOutComeEntity.toUiAnalyzeModel(): UiAnalyzeCategoryOutComeModel {
    colorIdx = 0
    stepIdx = 0
    colorUsedArr.clear()
    randomColorArr.clear()
    getRandomColor(this.analyzeResult.size)

    return UiAnalyzeCategoryOutComeModel(total = "총 ${
        NumberFormat.getNumberInstance().format(this.total)
    }${CurrencyUtil.currency}을\n소비했어요",
        differance = "${
            if (differance < 0 && total == 0.0) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(this.differance * -1.0)
                }${CurrencyUtil.currency}을\n덜 사용했어요"
            } else if (this.differance == this.total) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(differance)
                }${CurrencyUtil.currency}을\n더 사용했어요"
            } else if (this.differance > this.total) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(this.differance - total)
                }${CurrencyUtil.currency}을\n더 사용했어요"
            } else if (total > differance) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(differance)
                }${CurrencyUtil.currency}을\n더 사용했어요"
            } else {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(differance)
                }${CurrencyUtil.currency}을\n덜 사용했어요"
            }
        }",
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
                        Color.parseColor("#FFDE31")
                    }

                    1 -> {
                        stepIdx++
                        Color.parseColor("#FF965B")
                    }

                    2 -> {
                        stepIdx++
                        Color.parseColor("#E56E73")
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

fun PostAnalyzeCategoryInComeEntity.toUiAnalyzeModel(): UiAnalyzeCategoryInComeModel {
    colorIdx = 0
    stepIdx = 0
    colorUsedArr.clear()
    randomColorArr.clear()
    getRandomColor(this.analyzeResult.size)

    Timber.e("different $differance")
    Timber.e("total $total")

    return UiAnalyzeCategoryInComeModel(total = "총 ${
        NumberFormat.getNumberInstance().format(this.total)
    }${CurrencyUtil.currency}을\n벌었어요",
        differance = "${
            if (differance < 0 && total == 0.0) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(this.differance * -1.0)
                }${CurrencyUtil.currency}을\n덜 벌었어요"
            } else if (this.differance == this.total) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(differance)
                }${CurrencyUtil.currency}을\n더 벌었어요"
            } else if (this.differance > this.total) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(this.differance - total)
                }${CurrencyUtil.currency}을\n더 벌었어요"
            } else if (differance < 0 && total > differance) {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(differance * -1.0)
                }${CurrencyUtil.currency}을\n더 벌었어요"
            } else {
                "저번달 대비 ${
                    NumberFormat.getNumberInstance().format(differance)
                }${CurrencyUtil.currency}을\n더 벌었어요"
            }
        }",
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
                        Color.parseColor("#4C97FF")
                    }

                    1 -> {
                        stepIdx++
                        Color.parseColor("#35347F")
                    }

                    2 -> {
                        stepIdx++
                        Color.parseColor("#9B8BFF")
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

fun PostAnalyzeBudgetEntity.toUiAnalyzePlanModel(): UiAnalyzePlanModel {
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
    val enemyDay = lastDayOfMonth - today + 1

    return UiAnalyzePlanModel(
        initBudget = "${
            NumberFormat.getNumberInstance().format(this.initBudget)
        }${CurrencyUtil.currency}",
        leftMoney = "${
            NumberFormat.getNumberInstance().format(this.leftMoney)
        }${CurrencyUtil.currency}",
        percent = if (this.leftMoney < 0) {
            "100"
        } else {
            (((this.initBudget - this.leftMoney) / this.initBudget) * 100).toInt().toString()
        },
        divMoney = "${
            if (leftMoney > 0) {
                NumberFormat.getNumberInstance().format((this.leftMoney / enemyDay).roundToInt())
            } else {
                0
            }
        }${CurrencyUtil.currency}".replace("-", "")
    )
}

fun PostAnalyzeAssetEntity.toUiAnalyzeAssetModel(): UiAnalyzeAssetModel {
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
            "나의 자산이\n지난달보다 증가했어요"
        } else if (this.difference.toInt() == 0) {
            "나의 자산이\n지난달과 같아요"
        } else {
            "나의 자산이\n지난달보다 감소했어요"
        },
        difference = if (this.difference >= 0) {
            "지난달 대비 ${
                NumberFormat.getNumberInstance().format(this.difference)
            }${CurrencyUtil.currency}이\n증가했어요"
        } else {
            "지난달 대비 ${
                NumberFormat.getNumberInstance().format(this.difference)
            }${CurrencyUtil.currency}이\n감소했어요".replace("-", "")
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

fun PostAnalyzeLineSubCategoryEntity.toUiLineSubCategoryModel(): UiAnalyzeLineSubCategoryModel {
    return UiAnalyzeLineSubCategoryModel(
        subcategoryName = this.subcategoryName,
        bookLines = this.bookLines.map {
            BookSubData(
                money = "${NumberFormat.getNumberInstance().format(it.money)}",
                descriptionDetail = "${it.asset} ‧ ${it.lineDate.replace("-",".")}", // yyyy-mm-dd -> yyyy.mm.dd 형식으로 파싱
                description = "${it.description}(${it.asset}}",
                userProfileImg = it.userProfileImg ?: ""
            )
        }
    )
}


// 랜덤 색상 가져오기 그래프는 3개까지는 색상 고정 그 이후는 랜덤 색상임
fun getRandomColor(repeat: Int): List<Int> {
    if (repeat > 0) {
        var random = 0
        for (i in 0..repeat) {
            random = Random.nextInt(0, 14)
            if (!colorUsedArr.contains(random)) {
                colorUsedArr.add(random)
                randomColorArr.add(colorArr[random])
            }
        }
    }
    return randomColorArr
}

fun Double.round(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).roundToInt() / factor
}

private fun getConvertReceiveRepeatValue(value: String): String {
    Timber.e("value $value")
    return when(value) {
        "LATEST" -> "최신순"
        "OLDEST" -> "오래된 순"
        "USER_NICKNAME" -> "사용자 닉네임 가나다 순"
        "LINE_SUBCATEGORY_NAME" -> "분류 가나다 순"
        else -> ""
    }
}