package com.aos.floney.util

import android.content.Context
import com.aos.floney.R
import com.aos.model.book.UiBookCategory

object CategoryLocalizationMapper {

    /**
     * Maps a UiBookCategory to its localized display name.
     * If the category is a default category (default=true) and has a categoryKey,
     * it will use the localized string resource.
     * Otherwise, it will use the original name from the API.
     */
    fun getLocalizedCategoryName(context: Context, category: UiBookCategory): String {
        // If it's not a default category or doesn't have a categoryKey, return the original name
        if (!category.default || category.categoryKey == null) {
            return category.name
        }

        // Map the categoryKey to the appropriate string resource based on actual server response
        val stringResId = when (category.categoryKey) {
            // Asset categories (자산)
            "Cash" -> R.string.category_cash
            "Debit Card" -> R.string.category_debit_card
            "Credit Card" -> R.string.category_credit_card
            "Bank" -> R.string.category_bank

            // Expense categories (지출)
            "Food" -> R.string.category_food
            "Cafe/Snacks" -> R.string.category_cafe_snacks
            "Transport" -> R.string.category_transport
            "Housing/Phone" -> R.string.category_housing_phone
            "Health" -> R.string.category_health
            "Culture" -> R.string.category_culture
            "Travel/Stay" -> R.string.category_travel_stay
            "Living" -> R.string.category_living
            "Style/Beauty" -> R.string.category_style_beauty
            "Family" -> R.string.category_family
            "Education" -> R.string.category_education
            "Events" -> R.string.category_events
            "Other" -> R.string.category_other
            "Uncategorized" -> R.string.category_uncategorized

            // Income categories (수입)
            "Salary" -> R.string.category_salary
            "Extra Income" -> R.string.category_extra_income
            "Allowance" -> R.string.category_allowance
            "Financial Income" -> R.string.category_financial_income
            "Business Income" -> R.string.category_business_income
            "Bonus" -> R.string.category_bonus

            // Transfer categories (이체)
            "Transfer" -> R.string.category_transfer
            "Savings" -> R.string.category_savings
            "Investment" -> R.string.category_investment
            "Insurance" -> R.string.category_insurance
            "Card Payment" -> R.string.category_card_payment
            "Loan" -> R.string.category_loan

            // If categoryKey doesn't match any known key, return the original name
            else -> return category.name
        }

        return context.getString(stringResId)
    }

    /**
     * Maps a list of UiBookCategory items to their localized display names.
     * Creates new UiBookCategory instances with localized names.
     */
    fun localizeCategories(context: Context, categories: List<UiBookCategory>): List<UiBookCategory> {
        return categories.map { category ->
            category.copy(
                name = getLocalizedCategoryName(context, category)
            )
        }
    }
}