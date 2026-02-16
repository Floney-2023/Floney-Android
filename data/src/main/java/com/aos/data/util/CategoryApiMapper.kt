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
    // INCOME
    "salary" to "Salary",
    "월급" to "Salary",
    "급여" to "Salary",
    "business" to "Business",
    "business income" to "Business",
    "부업" to "Business",
    "사업소득" to "Business",
    "allowance" to "Allowance",
    "용돈" to "Allowance",
    "bonus" to "Bonus",
    "상여금" to "Bonus",
    "extra income" to "Extra Income",
    "부수입" to "Extra Income",
    "financial income" to "Financial Income",
    "금융소득" to "Financial Income",
    "etc" to "Etc",
    "기타" to "Etc",
    // OUTCOME
    "food" to "Food",
    "식비" to "Food",
    "cafe/snacks" to "Cafe/Snacks",
    "카페/간식" to "Cafe/Snacks",
    "transport" to "Transport",
    "transportation" to "Transportation",
    "교통" to "Transportation",
    "shopping" to "Shopping",
    "쇼핑" to "Shopping",
    "health" to "Health",
    "medical" to "Medical",
    "의료/건강" to "Medical",
    "문화/여가" to "Culture",
    "문화" to "Culture",
    "beauty" to "Beauty",
    "style/beauty" to "Style/Beauty",
    "패션/미용" to "Beauty",
    "education" to "Education",
    "교육" to "Education",
    "housing/phone" to "Housing/Phone",
    "주거/통신" to "Housing/Phone",
    "travel/stay" to "Travel/Stay",
    "여행/숙박" to "Travel/Stay",
    "family" to "Family",
    "육아" to "Family",
    "events" to "Events",
    "경조사" to "Events",
    "living" to "Living",
    "생활" to "Living",
    "other" to "Other",
    "uncategorized" to "Uncategorized",
    "미분류" to "Uncategorized",
    // ASSET
    "cash" to "Cash",
    "현금" to "Cash",
    "bank" to "Bank",
    "은행" to "Bank",
    "card" to "Card",
    "카드" to "Card",
    "debit card" to "Debit Card",
    "debitcard" to "DebitCard",
    "체크카드" to "Debit Card",
    "credit card" to "Credit Card",
    "creditcard" to "CreditCard",
    "신용카드" to "Credit Card",
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
    "cardpayment" to "CardPayment",
    "카드대금" to "Card Payment",
    "loan" to "Loan",
    "대출" to "Loan",
)

fun String.toCategoryTypeCode(): String {
    val trimmed = trim()
    return CATEGORY_TYPE_ALIASES[trimmed] ?: CATEGORY_TYPE_ALIASES[trimmed.lowercase()] ?: trimmed
}

fun String.toCategoryRequestValue(): String {
    val trimmed = trim()
    return CATEGORY_KEY_ALIASES[trimmed] ?: CATEGORY_KEY_ALIASES[trimmed.lowercase()] ?: trimmed
}
