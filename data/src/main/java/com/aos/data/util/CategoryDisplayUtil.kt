package com.aos.data.util

import java.util.Locale

private val koDisplayByCanonicalKey = mapOf(
    "Cash" to "현금",
    "DebitCard" to "체크카드",
    "CreditCard" to "신용카드",
    "Bank" to "은행",
    "Transfer" to "이체",
    "Savings" to "저축",
    "Investment" to "투자",
    "Insurance" to "보험",
    "CardPayment" to "카드대금",
    "Loan" to "대출",
    "Food" to "식비",
    "CafeSnack" to "카페/간식",
    "Transportation" to "교통",
    "HousingPhone" to "주거/통신",
    "Medical" to "건강",
    "Culture" to "문화",
    "Travel/Stay" to "여행/숙박",
    "Living" to "생활",
    "Beauty" to "패션/미용",
    "Family" to "가족",
    "Education" to "교육",
    "Events" to "경조사/회비",
    "Other" to "기타",
    "Uncategorized" to "미분류",
    "Salary" to "월급",
    "ExtraIncome" to "부수입",
    "Allowance" to "용돈",
    "FinancialIncome" to "금융소득",
    "BusinessIncome" to "사업소득",
    "Bonus" to "상여금"
)

private val enDisplayByCanonicalKey = mapOf(
    "Cash" to "Cash",
    "DebitCard" to "Debit Card",
    "CreditCard" to "Credit Card",
    "Bank" to "Bank",
    "Transfer" to "Transfer",
    "Savings" to "Savings",
    "Investment" to "Investment",
    "Insurance" to "Insurance",
    "CardPayment" to "Card Payment",
    "Loan" to "Loan",
    "Food" to "Food",
    "CafeSnack" to "Cafe/Snacks",
    "Transportation" to "Transport",
    "HousingPhone" to "Housing/Phone",
    "Medical" to "Health",
    "Culture" to "Culture",
    "Travel" to "Travel/Stay",
    "Living" to "Living",
    "Beauty" to "Style/Beauty",
    "Family" to "Family",
    "Education" to "Education",
    "Events" to "Events",
    "Other" to "Other",
    "Uncategorized" to "Uncategorized",
    "Salary" to "Salary",
    "ExtraIncome" to "Extra Income",
    "Allowance" to "Allowance",
    "FinancialIncome" to "Financial Income",
    "BusinessIncome" to "Business Income",
    "Bonus" to "Bonus"
)

private val aliasesToCanonical = mapOf(
    // ASSET
    "Cash" to "Cash",
    "현금" to "Cash",
    "Card" to "DebitCard",
    "카드" to "DebitCard",
    "DebitCard" to "DebitCard",
    "Debit Card" to "DebitCard",
    "체크카드" to "DebitCard",
    "CreditCard" to "CreditCard",
    "Credit Card" to "CreditCard",
    "신용카드" to "CreditCard",
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
    "CardPayment" to "CardPayment",
    "Card Payment" to "CardPayment",
    "카드대금" to "CardPayment",
    "Loan" to "Loan",
    "대출" to "Loan",
    // OUTCOME
    "Food" to "Food",
    "식비" to "Food",
    "CafeSnack" to "CafeSnack",
    "Cafe/Snacks" to "CafeSnack",
    "카페/간식" to "CafeSnack",
    "Transportation" to "Transportation",
    "Transport" to "Transportation",
    "교통" to "Transportation",
    "Shopping" to "Shopping",
    "쇼핑" to "Shopping",
    "HousingPhone" to "HousingPhone",
    "Housing/Phone" to "HousingPhone",
    "주거/통신" to "HousingPhone",
    "Medical" to "Medical",
    "Health" to "Medical",
    "건강" to "Medical",
    "의료/건강" to "Medical",
    "Culture" to "Culture",
    "문화" to "Culture",
    "문화/여가" to "Culture",
    "Travel" to "Travel",
    "Travel/Stay" to "Travel",
    "여행/숙박" to "Travel",
    "Living" to "Living",
    "생활" to "Living",
    "Beauty" to "Beauty",
    "Style/Beauty" to "Style/Beauty",
    "패션/미용" to "Style/Beauty",
    "Family" to "Family",
    "가족" to "Family",
    "육아" to "Family",
    "Education" to "Education",
    "교육" to "Education",
    "FamilyEvent" to "Events",
    "Events" to "Events",
    "경조사" to "Events",
    "경조사/회비" to "Events",
    "Etc" to "Other",
    "Other" to "Other",
    "기타" to "Other",
    "Uncategorized" to "Uncategorized",
    "미분류" to "Uncategorized",
    // INCOME
    "Salary" to "Salary",
    "월급" to "Salary",
    "급여" to "Salary",
    "Business" to "BusinessIncome",
    "BusinessIncome" to "BusinessIncome",
    "Business Income" to "BusinessIncome",
    "사업소득" to "BusinessIncome",
    "부업" to "BusinessIncome",
    "ExtraIncome" to "ExtraIncome",
    "Extra Income" to "ExtraIncome",
    "부수입" to "ExtraIncome",
    "Allowance" to "Allowance",
    "용돈" to "Allowance",
    "FinancialIncome" to "FinancialIncome",
    "Financial Income" to "FinancialIncome",
    "금융소득" to "FinancialIncome",
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
    enDisplayByCanonicalKey.forEach { (canonical, enDisplay) ->
        put(enDisplay, canonical)
        put(normalizeCategoryToken(enDisplay), canonical)
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
        enDisplayByCanonicalKey[canonical] ?: raw
    }
}
