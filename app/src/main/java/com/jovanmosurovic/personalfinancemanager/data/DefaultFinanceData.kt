package com.jovanmosurovic.personalfinancemanager.data

import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType

object DefaultFinanceData {
    const val FOOD = 100L
    const val GROCERIES = 101L
    const val DELIVERY = 102L

    const val BILLS = 200L
    const val PHONE_INTERNET = 201L
    const val ELECTRICITY = 202L

    const val TRANSPORT = 300L
    const val FUEL = 301L

    const val SHOPPING = 400L
    const val ONLINE = 401L

    const val INCOME = 500L
    const val SALARY = 501L

    val categories = listOf(
        category(FOOD, "category_food"),
        category(GROCERIES, "category_groceries", FOOD),
        category(DELIVERY, "category_delivery", FOOD),
        category(BILLS, "category_utilities"),
        category(PHONE_INTERNET, "category_phone_internet", BILLS),
        category(ELECTRICITY, "category_electricity", BILLS),
        category(TRANSPORT, "category_car"),
        category(FUEL, "category_fuel", TRANSPORT),
        category(SHOPPING, "category_shopping"),
        category(ONLINE, "category_online", SHOPPING),
        category(INCOME, "category_income"),
        category(SALARY, "category_salary", INCOME)
    )

    val keywordRules = listOf(
        rules(
            name = "Food / Groceries",
            categoryId = GROCERIES,
            type = TransactionType.EXPENSE,
            "MAXI", "LIDL", "IDEA", "AMAN"
        ),
        rules(
            name = "Food / Delivery",
            categoryId = DELIVERY,
            type = TransactionType.EXPENSE,
            "GLOVO"
        ),
        rules(
            name = "Bills / Phone and internet",
            categoryId = PHONE_INTERNET,
            type = TransactionType.EXPENSE,
            "YETTEL ERACUN", "YETTEL"
        ),
        rules(
            name = "Bills / Electricity",
            categoryId = ELECTRICITY,
            type = TransactionType.EXPENSE,
            "EPS"
        ),
        rules(
            name = "Transport / Fuel",
            categoryId = FUEL,
            type = TransactionType.EXPENSE,
            "NIS", "MOL"
        ),
        rules(
            name = "Shopping / Online",
            categoryId = ONLINE,
            type = TransactionType.EXPENSE,
            "TEMU"
        ),
        rules(
            name = "Income / Salary",
            categoryId = SALARY,
            type = TransactionType.INCOME,
            "JETBRAINS"
        )
    ).flatten()

    private fun category(id: Long, nameKey: String, parentId: Long? = null) =
        CategoryEntity(id = id, nameKey = nameKey, parentId = parentId)

    private fun rules(
        name: String,
        categoryId: Long,
        type: TransactionType,
        vararg keywords: String
    ): List<KeywordRuleEntity> = keywords.map { keyword ->
        KeywordRuleEntity(
            name = name,
            keyword = keyword,
            categoryId = categoryId,
            transactionType = type.name,
            priority = if (keyword.length <= 3) 10 else 0,
            matchMode = if (keyword.length <= 3) "WHOLE_WORD" else "CONTAINS"
        )
    }
}
