package com.aos.data.util

import java.util.Locale

private val koDisplayByCanonicalKey = mapOf(
    "Cash" to "현금",
    "Debit Card" to "체크카드",
    "Credit Card" to "신용카드",
    "Bank" to "은행",
    "Transfer" to "이체",
    "Savings" to "저축",
    "Investment" to "투자",
    "Insurance" to "보험",
    "Card Payment" to "카드대금",
    "Loan" to "대출",
    "Food" to "식비",
    "Cafe/Snacks" to "카페/간식",
    "Transport" to "교통",
    "Housing/Phone" to "주거/통신",
    "Health" to "의료/건강",
    "Culture" to "문화",
    "Travel/Stay" to "여행/숙박",
    "Living" to "생활",
    "Style/Beauty" to "패션/미용",
    "Family" to "육아",
    "Education" to "교육",
    "Events" to "경조사",
    "Other" to "기타",
    "Uncategorized" to "미분류",
    "Salary" to "급여",
    "Extra Income" to "부수입",
    "Allowance" to "용돈",
    "Financial Income" to "금융소득",
    "Business Income" to "사업소득",
    "Bonus" to "상여금"
)

private val aliasesToCanonical = mapOf(
    // ASSET
    "Cash" to "Cash",
    "현금" to "Cash",
    "Debit Card" to "Debit Card",
    "체크카드" to "Debit Card",
    "Credit Card" to "Credit Card",
    "신용카드" to "Credit Card",
    "Bank" to "Bank",
    "은행" to "Bank",
    // TRANSFER
    "Transfer" to "Transfer",
    "이체" to "Transfer",
    "Savings" to "Savings",
    "저축" to "Savings",
    "Investment" to "Investment",
    "투자" to "Investment",
    "Insurance" to "Insurance",
    "보험" to "Insurance",
    "Card Payment" to "Card Payment",
    "카드대금" to "Card Payment",
    "Loan" to "Loan",
    "대출" to "Loan",
    // OUTCOME
    "Food" to "Food",
    "식비" to "Food",
    "Cafe/Snacks" to "Cafe/Snacks",
    "카페/간식" to "Cafe/Snacks",
    "Transport" to "Transport",
    "교통" to "Transport",
    "Housing/Phone" to "Housing/Phone",
    "주거/통신" to "Housing/Phone",
    "Health" to "Health",
    "건강" to "Health",
    "의료/건강" to "Health",
    "Culture" to "Culture",
    "문화" to "Culture",
    "Travel/Stay" to "Travel/Stay",
    "여행/숙박" to "Travel/Stay",
    "Living" to "Living",
    "생활" to "Living",
    "Style/Beauty" to "Style/Beauty",
    "패션/미용" to "Style/Beauty",
    "스타일/뷰티" to "Style/Beauty",
    "Family" to "Family",
    "육아" to "Family",
    "가족" to "Family",
    "Education" to "Education",
    "교육" to "Education",
    "Events" to "Events",
    "경조사" to "Events",
    "경조사/회비" to "Events",
    "Other" to "Other",
    "기타" to "Other",
    "Uncategorized" to "Uncategorized",
    "미분류" to "Uncategorized",
    // INCOME
    "Salary" to "Salary",
    "급여" to "Salary",
    "월급" to "Salary",
    "Business Income" to "Business Income",
    "사업소득" to "Business Income",
    "부업" to "Business Income",
    "Extra Income" to "Extra Income",
    "부수입" to "Extra Income",
    "Allowance" to "Allowance",
    "용돈" to "Allowance",
    "Financial Income" to "Financial Income",
    "금융소득" to "Financial Income",
    "Bonus" to "Bonus",
    "상여금" to "Bonus"
)

private fun normalizeCategoryToken(value: String): String {
    return value.lowercase().replace(Regex("[\\s/_-]"), "")
}

private val canonicalByNormalizedToken: Map<String, String> = buildMap {
    aliasesToCanonical.forEach { (alias, canonical) ->
        put(alias, canonical)
        put(normalizeCategoryToken(alias), canonical)
    }
    koDisplayByCanonicalKey.forEach { (canonical, koDisplay) ->
        put(koDisplay, canonical)
        put(normalizeCategoryToken(koDisplay), canonical)
    }
}

fun toCanonicalCategoryKey(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed
    return canonicalByNormalizedToken[trimmed]
        ?: canonicalByNormalizedToken[normalizeCategoryToken(trimmed)]
        ?: trimmed
}

fun localizeCategoryKeyDisplayName(raw: String, locale: Locale = Locale.getDefault()): String {
    val canonical = toCanonicalCategoryKey(raw)
    return if (locale.language == "ko") {
        koDisplayByCanonicalKey[canonical] ?: raw
    } else {
        raw
    }
}
