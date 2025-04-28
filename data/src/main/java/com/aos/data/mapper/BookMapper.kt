package com.aos.data.mapper

import com.aos.data.entity.request.book.PostBooksLinesEntity
import com.aos.data.entity.response.book.GetBookCategoryEntity
import com.aos.data.entity.response.book.GetBookFavoriteEntity
import com.aos.data.entity.response.book.GetBookRepeatEntity
import com.aos.data.entity.response.book.PostBooksChangeEntity
import com.aos.data.entity.response.book.GetBooksBudgetEntity
import com.aos.data.entity.response.book.GetBooksCodeEntity
import com.aos.data.entity.response.book.GetBooksEntity
import com.aos.data.entity.response.book.GetBooksInfoCurrencyEntity
import com.aos.data.entity.response.book.GetBooksInfoEntity
import com.aos.data.entity.response.book.PostBookFavoriteEntity
import com.aos.data.entity.response.book.PostBooksCategoryAddEntity
import com.aos.data.entity.response.book.PostBooksCreateEntity
import com.aos.data.entity.response.book.PostBooksInfoCurrencyEntity
import com.aos.data.entity.response.book.PostBooksJoinEntity
import com.aos.data.entity.response.home.GetBookDaysEntity
import com.aos.data.entity.response.home.GetBookInfoEntity
import com.aos.data.entity.response.home.GetBookMonthEntity
import com.aos.data.entity.response.home.GetCheckUserBookEntity
import com.aos.model.book.PostBooksChangeModel
import com.aos.data.entity.response.settlement.GetBooksUsersEntity
import com.aos.data.entity.response.settlement.GetSettleUpLastEntity
import com.aos.data.entity.response.settlement.GetSettlementSeeEntity
import com.aos.data.entity.response.settlement.PostBooksOutcomesEntity
import com.aos.data.entity.response.settlement.PostNaverShortenUrlEntity
import com.aos.data.entity.response.settlement.PostSettlementAddEntity
import com.aos.data.util.CurrencyUtil
import com.aos.model.book.BudgetItem
import com.aos.model.book.GetBooksCodeModel
import com.aos.model.book.GetBooksInfoCurrencyModel
import com.aos.model.book.MyBookUsers
import com.aos.model.book.PostBookFavoriteModel
import com.aos.model.book.PostBooksCreateModel
import com.aos.model.book.PostBooksInfoCurrencyModel
import com.aos.model.book.PostBooksJoinModel
import com.aos.model.book.PostBooksLinesModel
import com.aos.model.book.UiBookBudgetModel
import com.aos.model.book.UiBookCategory
import com.aos.model.book.UiBookSettingModel
import com.aos.model.home.DayMoney
import com.aos.model.home.Expenses
import com.aos.model.home.ExtData
import com.aos.model.home.GetCheckUserBookModel
import com.aos.model.home.MonthMoney
import com.aos.model.home.OurBookUsers
import com.aos.model.home.UiBookDayModel
import com.aos.model.home.UiBookInfoModel
import com.aos.model.home.UiBookMonthModel
import timber.log.Timber
import com.aos.model.settlement.BookUsers
import com.aos.model.settlement.Details
import com.aos.model.settlement.GetSettlementLastModel
import com.aos.model.settlement.Outcomes
import com.aos.model.settlement.Settlement
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.model.settlement.UiOutcomesSelectModel
import com.aos.model.settlement.UiSettlementAddModel
import com.aos.model.settlement.UiSettlementSeeModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

import com.aos.model.book.PostBooksCategoryAddModel
import com.aos.model.book.UiBookEntranceModel
import com.aos.model.book.UiBookFavoriteModel
import com.aos.model.book.UiBookRepeatModel
import com.aos.model.home.CarryOverInfo
import com.aos.model.home.ImageUrls
import com.aos.model.settlement.NaverShortenUrlModel

// 유저 가계부 유효 확인
fun GetCheckUserBookEntity.toGetCheckUserBookModel(): GetCheckUserBookModel {
    return GetCheckUserBookModel(this.bookKey ?: "")
}

// 캘린더 조회
fun GetBookMonthEntity.toUiBookMonthModel(): UiBookMonthModel {
    val listData = arrayListOf<MonthMoney>()
    var tempDay = ""
    var tempExpenses: Expenses? = null
    var tempDate = ""
    var tempOutcome = ""

    val calendar = Calendar.getInstance()
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

    this.expenses.forEach {

        if (it.date == tempDate) {
            tempExpenses?.outcome = tempOutcome
            listData.add(
                MonthMoney(
                    "${it.date.split("-")[0].toInt()}",
                    "${it.date.split("-")[1].toInt()}",
                    "${it.date.split("-")[2].toInt()}",
                    Expenses(
                        date = it.date,
                        outcome = when {
                            // 첫 번째 조건: money가 0.0이고 날짜가 1일이 아닌 경우
                            it.money.toString() == "0.0" && it.date.split("-")[2].toInt() != 1 -> ""

                            // 두 번째 조건: 날짜가 1일인 경우
                            it.date.split("-")[2].toInt() == 1 -> {
                                when {
                                    // 이월 상태가 없는 경우
                                    !carryOverInfo.carryOverStatus -> {
                                        if (it.money.toString() == "0.0") ""
                                        else "-${NumberFormat.getNumberInstance().format(it.money)}"
                                    }
                                    // 이월 상태가 있는 경우
                                    else -> {
                                        val totalMoney =
                                            it.money + kotlin.math.abs(carryOverInfo.carryOverMoney)
                                        if (kotlin.math.abs(totalMoney) >= 1e-9 && carryOverInfo.carryOverMoney < 0) {
                                            "-${
                                                NumberFormat.getNumberInstance().format(totalMoney)
                                            }"
                                        } else {
                                            ""
                                        }
                                    }
                                }
                            }
                            // 기본 조건: 위 조건에 해당하지 않는 경우
                            else -> "-${NumberFormat.getNumberInstance().format(it.money)}"
                        },
                        income = tempExpenses!!.income
                    ),
                    todayDate == it.date
                )
            )
        } else {
            tempDate = it.date
            tempOutcome = "-${NumberFormat.getNumberInstance().format(it.money)}"
            tempDay = it.date.split("-")[2].toInt().toString()
            tempExpenses = Expenses(
                date = it.date,
                outcome = tempOutcome,
                income = when {
                    // 첫 번째 조건: money가 0.0이고 날짜가 1일이 아닌 경우
                    it.money.toString() == "0.0" && it.date.split("-")[2].toInt() != 1 -> ""

                    // 두 번째 조건: 날짜가 1일인 경우
                    it.date.split("-")[2].toInt() == 1 -> {
                        when {
                            // 이월 상태가 없는 경우
                            !carryOverInfo.carryOverStatus -> {
                                if (it.money.toString() == "0.0") ""
                                else "+${NumberFormat.getNumberInstance().format(it.money)}"
                            }
                            // 이월 상태가 있는 경우
                            else -> {
                                val totalMoney =
                                    it.money + kotlin.math.abs(carryOverInfo.carryOverMoney)
                                if (kotlin.math.abs(totalMoney) >= 1e-9 && carryOverInfo.carryOverMoney > 0) {
                                    "+${NumberFormat.getNumberInstance().format(totalMoney)}"
                                } else {
                                    ""
                                }
                            }
                        }
                    }
                    // 기본 조건: 위 조건에 해당하지 않는 경우
                    else -> "+${NumberFormat.getNumberInstance().format(it.money)}"
                }
            )
        }
    }

    // 날짜 앞 빈공백 추가
    val year = tempDate.split("-")[0].toInt()
    val month = (tempDate.split("-")[1].toInt() - 1)

    calendar.set(year, month, 1)

    // 달의 첫번째 요일 저장
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val list = arrayListOf<MonthMoney>()

    for (i in 2..firstDayOfWeek) {
        list.add(MonthMoney())
    }
    list.addAll(listData)

    return if (carryOverInfo.carryOverStatus) {
        if (carryOverInfo.carryOverMoney >= 0) {
            // 총 수입에 포함
            UiBookMonthModel(
                list, ExtData(
                    "${
                        NumberFormat.getNumberInstance()
                            .format(totalIncome + carryOverInfo.carryOverMoney)
                    }${CurrencyUtil.currency}",
                    "${
                        NumberFormat.getNumberInstance().format(totalOutcome)
                    }${CurrencyUtil.currency}",
                    totalBalance = "${
                        NumberFormat.getNumberInstance().format(totalIncome - totalOutcome)
                    }${CurrencyUtil.currency}"
                ), CarryOverInfo(
                    carryOverInfo.carryOverStatus, NumberFormat.getNumberInstance()
                        .format(carryOverInfo.carryOverMoney)
                )
            )
        } else {
            // 총 지출에 포함
            UiBookMonthModel(
                list, ExtData(
                    totalIncome = "${
                        NumberFormat.getNumberInstance().format(totalIncome)
                    }${CurrencyUtil.currency}",
                    totalOutcome = "${
                        NumberFormat.getNumberInstance()
                            .format(totalOutcome + kotlin.math.abs(carryOverInfo.carryOverMoney))
                    }${CurrencyUtil.currency}",
                    totalBalance = "${
                        NumberFormat.getNumberInstance().format(totalIncome - totalOutcome)
                    }${CurrencyUtil.currency}"
                ), CarryOverInfo(
                    carryOverInfo.carryOverStatus, NumberFormat.getNumberInstance()
                        .format(carryOverInfo.carryOverMoney)
                )
            )
        }
    } else {
        // 이월 내역 없을 경우
        UiBookMonthModel(
            list, ExtData(
                totalIncome = "${
                    NumberFormat.getNumberInstance().format(totalIncome)
                }${CurrencyUtil.currency}",
                totalOutcome = "${
                    NumberFormat.getNumberInstance().format(totalOutcome)
                }${CurrencyUtil.currency}",
                totalBalance = "${
                    NumberFormat.getNumberInstance().format(totalIncome - totalOutcome)
                }${CurrencyUtil.currency}"
            ), CarryOverInfo(
                carryOverInfo.carryOverStatus, NumberFormat.getNumberInstance()
                    .format(carryOverInfo.carryOverMoney)
            )
        )
    }
}

// 일별 조회
fun GetBookDaysEntity.toUiBookMonthModel(): UiBookDayModel {
    val dayMoneyList: List<DayMoney> = this.dayLinesResponse.map {
        DayMoney(
            id = it.id,
            money = when (it.lineCategory) {
                "INCOME" -> "+ ${NumberFormat.getNumberInstance().format(it.money)}"
                "OUTCOME" -> "- ${NumberFormat.getNumberInstance().format(it.money)}"
                "TRANSFER" -> NumberFormat.getNumberInstance().format(it.money)
                else -> "0"
            },
            description = it.description,
            lineCategory = it.lineCategory,
            lineSubCategory = it.lineSubCategory,
            assetSubCategory = it.assetSubCategory,
            exceptStatus = it.exceptStatus,
            writerEmail = it.writerEmail,
            writerNickName = it.writerNickname,
            writerProfileImg = it.writerProfileImg,
            repeatDuration = getConvertReceiveRepeatValue(it.repeatDuration),
            memo = it.memo,
            imageUrls = it.imageUrls.map { image ->
                ImageUrls(
                    image.id,
                    image.url
                )
            },
            seeProfileStatus = false
        )
    }

    Timber.e("this.dayLiensResponse ${this.dayLinesResponse}")
    Timber.e("dayMoneyList $dayMoneyList")

    var totalIncome = 0.0
    var totalOutcome = 0.0

    this.totalExpense.forEach {
        if (it.categoryType == "INCOME") {
            totalIncome = it.money
        }
        if (it.categoryType == "OUTCOME") {
            totalOutcome = it.money
        }
    }
    return UiBookDayModel(
        dayMoneyList, ExtData(
            "${NumberFormat.getNumberInstance().format(totalIncome)}${CurrencyUtil.currency}",
            "${NumberFormat.getNumberInstance().format(totalOutcome)}${CurrencyUtil.currency}"
        ), CarryOverInfo(
            carryOverInfo.carryOverStatus, NumberFormat.getNumberInstance()
                .format(carryOverInfo.carryOverMoney)
        )
    )
}

fun GetBookInfoEntity.toUiBookInfoModel(): UiBookInfoModel {
    val ourBookUsers = this.ourBookUsers.map {
        OurBookUsers(
            name = it.name, profileImg = it.profileImg ?: "user_default", role = it.role, me = it.me
        )
    }
    return UiBookInfoModel(
        bookName = this.bookName,
        bookImg = this.bookImg ?: "book_default",
        startDay = this.startDay,
        seeProfileStatus = this.seeProfileStatus,
        carryOver = this.carryOver,
        ourBookUsers = ourBookUsers
    )
}


fun PostBooksJoinEntity.toPostBooksJoinModel(): PostBooksJoinModel {
    return PostBooksJoinModel(this.bookKey ?: "", this.code ?: "")
}

fun PostBooksCreateEntity.toPostBooksCreateModel(): PostBooksCreateModel {
    return PostBooksCreateModel(this.bookKey ?: "", this.code ?: "")
}

fun PostBooksLinesEntity.toPostBooksLinesModel(): PostBooksLinesModel {
    return PostBooksLinesModel(
        money = this.money.toInt(),
        flow = this.flow,
        asset = this.asset,
        line = this.line,
        lineDate = this.lineDate,
        description = this.description,
        except = this.except,
        nickname = this.nickname,
        repeatDuration = this.repeatDuration
    )
}

fun PostBooksChangeEntity.toPostBooksLinesChangeModel(): PostBooksChangeModel {
    return PostBooksChangeModel(
        money = this.money,
        flow = this.flow,
        asset = this.asset,
        line = this.line,
        lineDate = this.lineDate,
        description = this.description,
        except = this.except,
        nickname = this.nickname
    )
}

// 정렬 기준 정의
val assetOrder = listOf("현금", "체크카드", "신용카드", "은행")
val expenseOrder = listOf(
    "식비",
    "카페/간식",
    "교통",
    "주거/통신",
    "의료/건강",
    "문화",
    "여행/숙박",
    "생활",
    "패션/미용",
    "육아",
    "교육",
    "경조사",
    "기타",
    "미분류"
)
val incomeOrder = listOf("급여", "부수입", "용돈", "금융소득", "사업소득", "상여금", "기타", "미분류")
val transferOrder = listOf("이체", "저축", "현금", "투자", "보험", "카드대금", "대출", "기타", "미분류")

fun List<GetBookCategoryEntity>.toUiBookCategory(): List<UiBookCategory> {
    val allOrders = assetOrder + expenseOrder + incomeOrder + transferOrder
    return this.sortedWith(compareBy({
        allOrders.indexOf(it.name).takeIf { it >= 0 } ?: Int.MAX_VALUE
    }, { it.name }))
        .mapIndexed { idx, it ->
            UiBookCategory(
                idx, false, it.name, it.default
            )
        }
}

fun GetSettleUpLastEntity.toGetsettleUpLastModel(): GetSettlementLastModel {
    return GetSettlementLastModel(passedDays = this.passedDays)
}

fun List<GetBooksUsersEntity>.toUiMemberSelectModel(): UiMemberSelectModel {
    val myBookUsers = this.map {
        BookUsers(
            email = it.email, nickname = it.nickname, profileImg = it.profileImg ?: "user_default", isCheck = false
        )
    }
    return UiMemberSelectModel(
        booksUsers = myBookUsers
    )
}

fun List<PostBooksOutcomesEntity>.toUiOutcomesSelectModel(): UiOutcomesSelectModel {
    val myBookOutcomes = this.mapIndexed { index, item ->
        Outcomes(
            id = index, // 인덱스를 id로 사용
            money = item.money.roundToLong(),
            moneyFormat = "${
                NumberFormat.getNumberInstance().format(formatAmount(item.money))
            }${CurrencyUtil.currency}",
            category = "${item.category[0]} ‧ ${item.category[1]}",
            assetType = item.assetType,
            content = item.content,
            img = item.img,
            userEmail = item.userEmail,
            isClick = false
        )
    }
    return UiOutcomesSelectModel(
        outcomes = myBookOutcomes
    )
}

fun PostSettlementAddEntity.toPostSettlementAddModel(): UiSettlementAddModel {

    val expenses = this.details.map {
        com.aos.model.settlement.Expenses(
            money = if (it.money.toInt() == 0) "${
                NumberFormat.getNumberInstance().format(this.totalOutcome.roundToLong())
            }${CurrencyUtil.currency}" else "${
                NumberFormat.getNumberInstance()
                    .format(it.money.roundToLong() + this.outcome.roundToLong())
            }${CurrencyUtil.currency}",
            userNickname = it.userNickname
        )
    }

    val details = this.details.map {
        Details(
            money = if (it.money.toInt() == 0) "${
                NumberFormat.getNumberInstance().format(this.totalOutcome.roundToLong())
            }${CurrencyUtil.currency}" else "${
                NumberFormat.getNumberInstance().format(it.money.roundToLong().absoluteValue)
            }${CurrencyUtil.currency}",
            userNickname = it.userNickname,
            useruserProfileImg = it.userProfileImg ?: "user_default",
            moneyInfo = if (it.money < 0) "을 보내야해요." else if (it.money > 0) "을 받아야해요." else "정산할 금액이 없어요."
        )
    }
    return UiSettlementAddModel(
        id = this.id,
        startDate = this.startDate,
        endDate = this.endDate,
        dateString = "${this.startDate.replace('-', '.')} - ${this.endDate.replace('-', '.')}",
        userCount = this.userCount,
        totalOutcome = "${
            NumberFormat.getNumberInstance().format(this.totalOutcome.roundToLong())
        }${CurrencyUtil.currency}",
        outcome = "${
            NumberFormat.getNumberInstance().format(this.outcome.roundToLong())
        }${CurrencyUtil.currency}",
        details = details,
        expenses = expenses
    )
}

fun List<GetSettlementSeeEntity>.toUiSettlementSeeModel(): UiSettlementSeeModel {
    val myBookSettlements = this.map {
        Settlement(
            id = it.id,
            dateString = "${it.startDate.replace('-', '.')} - ${it.endDate.replace('-', '.')}",
            userCount = "${it.userCount}명",
            totalOutcome = "${
                NumberFormat.getNumberInstance().format(it.totalOutcome)
            }${CurrencyUtil.currency}",
            outcome = "${
                NumberFormat.getNumberInstance().format(it.outcome)
            }${CurrencyUtil.currency}"
        )
    }
    return UiSettlementSeeModel(
        settlementList = myBookSettlements
    )
}

fun GetBooksInfoEntity.toUiBookSettingModel(): UiBookSettingModel {
    val myBookUsers = this.ourBookUsers.map {
        val roleString = if (it.me) "${it.role}·나" else it.role
        MyBookUsers(
            name = it.name,
            profileImg = it.profileImg ?: "user_default",
            email = it.email,
            role = roleString,
            me = it.me
        )
    }
    return UiBookSettingModel(
        bookName = this.bookName,
        bookImg = this.bookImg ?: "",
        startDay = "${this.startDay.replace('-', '.')} 개설",
        seeProfileStatus = this.seeProfileStatus,
        carryOver = this.carryOver,
        ourBookUsers = myBookUsers
    )
}

fun PostBooksInfoCurrencyEntity.toPostBooksInfoCurrencyModel(): PostBooksInfoCurrencyModel {
    return PostBooksInfoCurrencyModel(myBookCurrency = this.myBookCurrency ?: "")
}

fun GetBooksInfoCurrencyEntity.toGetBooksInfoCurrencyModel(): GetBooksInfoCurrencyModel {
    return GetBooksInfoCurrencyModel(myBookCurrency = this.myBookCurrency ?: "")
}

fun GetBooksBudgetEntity.toUiBookBudgetModel(): UiBookBudgetModel {
    val budgetList = listOf(
        BudgetItem(
            "1",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.january))
            }${CurrencyUtil.currency}",
            this.january > 0.0
        ),
        BudgetItem(
            "2",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.february))
            }${CurrencyUtil.currency}",
            this.february > 0.0
        ),
        BudgetItem(
            "3",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.march))
            }${CurrencyUtil.currency}",
            this.march > 0.0
        ),
        BudgetItem(
            "4",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.april))
            }${CurrencyUtil.currency}",
            this.april > 0.0
        ),
        BudgetItem(
            "5",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.may))
            }${CurrencyUtil.currency}",
            this.may > 0.0
        ),
        BudgetItem(
            "6",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.june))
            }${CurrencyUtil.currency}",
            this.june > 0.0
        ),
        BudgetItem(
            "7",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.july))
            }${CurrencyUtil.currency}",
            this.july > 0.0
        ),
        BudgetItem(
            "8",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.august))
            }${CurrencyUtil.currency}",
            this.august > 0.0
        ),
        BudgetItem(
            "9",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.september))
            }${CurrencyUtil.currency}",
            this.september > 0.0
        ),
        BudgetItem(
            "10",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.october))
            }${CurrencyUtil.currency}",
            this.october > 0.0
        ),
        BudgetItem(
            "11",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.november))
            }${CurrencyUtil.currency}",
            this.november > 0.0
        ),
        BudgetItem(
            "12",
            "${
                NumberFormat.getNumberInstance().format(formatAmount(this.december))
            }${CurrencyUtil.currency}",
            this.december > 0.0
        )
    )
    return UiBookBudgetModel(
        budgetList = budgetList
    )
}

fun formatAmount(amount: Double): Double {
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toDouble() // 정수로 변환한 후 다시 Double로 변환
    } else {
        amount // 소수점 이하 값 포함
    }
}

fun PostBooksCategoryAddEntity.toPostBooksCategoryAddModel(): PostBooksCategoryAddModel {
    return PostBooksCategoryAddModel(name = this.name ?: "")
}

fun GetBooksCodeEntity.toGetBooksCodeModel(): GetBooksCodeModel {
    return GetBooksCodeModel(this.code ?: "")
}

fun List<GetBookRepeatEntity>.toUiBookRepeatModel(): List<UiBookRepeatModel> {
    return this.map {
        val repeatDurationInKorean = when (it.repeatDuration) {
            "NONE" -> ""
            "EVERYDAY" -> "매일"
            "WEEK" -> "매주"
            "MONTH" -> "매달"
            "WEEKDAY" -> "주중"
            "WEEKEND" -> "주말"
            else -> it.repeatDuration // 만약 매칭되는 값이 없을 경우 기본값 사용
        }
        UiBookRepeatModel(
            it.id,
            it.description,
            repeatDurationInKorean,
            it.lineSubCategory,
            it.assetSubCategory,
            "${
                NumberFormat.getNumberInstance().format(formatAmount(it.money))
            }${CurrencyUtil.currency}",
            false
        )
    }
}

fun GetBooksEntity.toUiBookEntranceModel(): UiBookEntranceModel {
    return UiBookEntranceModel(
        bookName = this.bookName,
        bookImg = this.bookImg,
        bookInfo = formatBookInfo(this.startDay, this.memberCount),
        bookKey = this.bookKey
    )
}

fun PostNaverShortenUrlEntity.toNaverShortenUrlModel(): NaverShortenUrlModel {
    return NaverShortenUrlModel(
        result = this.result.url
    )
}

fun formatBookInfo(startDay: String, memberCount: Int): String {
    // 입력된 날짜 형식
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
    // 출력할 날짜 형식
    val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    // startDay를 Date 객체로 파싱
    val date = inputFormat.parse(startDay)
    // 파싱된 Date 객체를 새로운 형식으로 포매팅
    val formattedDate = date?.let { outputFormat.format(it) } ?: ""

    return "$formattedDate 개설 ꞏ ${memberCount}명"
}

fun List<GetBookFavoriteEntity>.toUiBookFavorite(): List<UiBookFavoriteModel> {
    return this.map {
        UiBookFavoriteModel(
            idx = it.id,
            checked = false,
            description = it.description,
            lineCategoryName = it.lineCategoryName,
            lineSubcategoryName = it.lineSubcategoryName,
            assetSubcategoryName = it.assetSubcategoryName,
            money = "${
                NumberFormat.getNumberInstance().format(formatAmount(it.money))
            }${CurrencyUtil.currency}",
            exceptStatus = it.exceptStatus
        )
    }
}

fun PostBookFavoriteEntity.toPostBookFavoriteModel(): PostBookFavoriteModel {
    return PostBookFavoriteModel(
        id = this.id,
        money = this.money,
        description = this.description,
        lineCategoryName = this.lineCategoryName,
        lineSubcategoryName = this.lineSubcategoryName,
        assetSubcategoryName = this.assetSubcategoryName,
        exceptStatus = this.exceptStatus
    )

}

private fun getConvertReceiveRepeatValue(value: String): String {
    Timber.e("value $value")
    return when (value) {
        "NONE" -> "없음"
        "EVERYDAY" -> "매일"
        "WEEK" -> "매주"
        "MONTH" -> "매달"
        "WEEKDAY" -> "주중"
        "WEEKEND" -> "주말"
        else -> ""
    }
}