package com.aos.data.util

private val CATEGORY_TYPE_ALIASES = mapOf(
    "INCOME" to "INCOME",
    "수입" to "INCOME",
    "income" to "INCOME",
    "OUTCOME" to "OUTCOME",
    "지출" to "OUTCOME",
    "expense" to "OUTCOME",
    "outcome" to "OUTCOME",
    "TRANSFER" to "TRANSFER",
    "이체" to "TRANSFER",
    "transfer" to "TRANSFER",
    "ASSET" to "ASSET",
    "자산" to "ASSET",
    "asset" to "ASSET",
)

private val CATEGORY_KEY_ALIASES = mapOf(
    // ASSET
    "cash" to "Cash",
    "현금" to "Cash",
    "bank" to "Bank",
    "은행" to "Bank",
    "debit card" to "Debit Card",
    "debitcard" to "Debit Card",
    "체크카드" to "Debit Card",
    "card" to "Debit Card",
    "카드" to "Debit Card",
    "credit card" to "Credit Card",
    "creditcard" to "Credit Card",
    "신용카드" to "Credit Card",
    // OUTCOME
    "food" to "Food",
    "식비" to "Food",
    "cafe/snacks" to "Cafe/Snacks",
    "카페/간식" to "Cafe/Snacks",
    "transport" to "Transport",
    "transportation" to "Transport",
    "교통" to "Transport",
    "housing/phone" to "Housing/Phone",
    "주거/통신" to "Housing/Phone",
    "health" to "Health",
    "medical" to "Health",
    "의료/건강" to "Health",
    "건강" to "Health",
    "culture" to "Culture",
    "문화" to "Culture",
    "travel/stay" to "Travel/Stay",
    "여행/숙박" to "Travel/Stay",
    "living" to "Living",
    "생활" to "Living",
    "style/beauty" to "Style/Beauty",
    "beauty" to "Style/Beauty",
    "패션/미용" to "Style/Beauty",
    "스타일/뷰티" to "Style/Beauty",
    "family" to "Family",
    "육아" to "Family",
    "가족" to "Family",
    "education" to "Education",
    "교육" to "Education",
    "events" to "Events",
    "경조사" to "Events",
    "경조사/회비" to "Events",
    "other" to "Other",
    "기타" to "Other",
    "uncategorized" to "Uncategorized",
    "미분류" to "Uncategorized",
    // INCOME
    "salary" to "Salary",
    "월급" to "Salary",
    "급여" to "Salary",
    "extra income" to "Extra Income",
    "extraincome" to "Extra Income",
    "부수입" to "Extra Income",
    "allowance" to "Allowance",
    "용돈" to "Allowance",
    "financial income" to "Financial Income",
    "financialincome" to "Financial Income",
    "금융소득" to "Financial Income",
    "business income" to "Business Income",
    "businessincome" to "Business Income",
    "business" to "Business Income",
    "사업소득" to "Business Income",
    "부업" to "Business Income",
    "bonus" to "Bonus",
    "상여금" to "Bonus",
    // TRANSFER
    "transfer" to "Transfer",
    "이체" to "Transfer",
    "savings" to "Savings",
    "저축" to "Savings",
    "investment" to "Investment",
    "투자" to "Investment",
    "insurance" to "Insurance",
    "보험" to "Insurance",
    "card payment" to "Card Payment",
    "cardpayment" to "Card Payment",
    "카드대금" to "Card Payment",
    "loan" to "Loan",
    "대출" to "Loan",
)

private fun normalizeCategoryToken(value: String): String {
    return value.lowercase().replace(Regex("[\\s/_-]"), "")
}

fun String.toCategoryTypeCode(): String {
    val trimmed = trim()
    return CATEGORY_TYPE_ALIASES[trimmed] ?: CATEGORY_TYPE_ALIASES[trimmed.lowercase()] ?: trimmed
}

fun String.toCategoryRequestValue(): String {
    val trimmed = trim()
    return CATEGORY_KEY_ALIASES[trimmed]
        ?: CATEGORY_KEY_ALIASES[trimmed.lowercase()]
        ?: CATEGORY_KEY_ALIASES[normalizeCategoryToken(trimmed)]
        ?: trimmed
}
