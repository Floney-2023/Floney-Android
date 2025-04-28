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
import androidx.core.graphics.toColorInt

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

fun PostAnalyzeCategoryOutComeEntity.toUiAnalyzeModel(): UiAnalyzeCategoryOutComeModel {
    colorIdx = 0
    stepIdx = 0
    colorUsedArr.clear()
    randomColorArr.clear()
    randomColorArr.addAll(getRandomColor(this.analyzeResult.size))

    return UiAnalyzeCategoryOutComeModel(total = "총 ${
        NumberFormat.getNumberInstance().format(this.total)
    }${CurrencyUtil.currency}을\n소비했어요",
        differance = if (differance < 0) {
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

fun PostAnalyzeCategoryInComeEntity.toUiAnalyzeModel(): UiAnalyzeCategoryInComeModel {
    colorIdx = 0
    stepIdx = 0
    colorUsedArr.clear()
    randomColorArr.clear()
    randomColorArr.addAll(getRandomColor(this.analyzeResult.size))

    Timber.e("different $differance")
    Timber.e("total $total")

    return UiAnalyzeCategoryInComeModel(total = "총 ${
        NumberFormat.getNumberInstance().format(this.total)
    }${CurrencyUtil.currency}을\n벌었어요",
        differance = if (differance < 0 && total == 0.0) {
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
                description = it.description,
                userProfileImg = it.userProfileImg ?: ""
            )
        }
    )
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