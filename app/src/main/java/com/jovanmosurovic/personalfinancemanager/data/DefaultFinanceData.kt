package com.jovanmosurovic.personalfinancemanager.data

import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.KnownOtpAccounts
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType

object DefaultFinanceData {
    const val FOOD = 100L
    const val GROCERIES = 101L
    const val FAST_FOOD = 102L
    const val DELIVERY = 103L
    const val KIOSK = 104L

    const val UTILITIES = 200L
    const val PHONE_INTERNET = 201L
    const val INFOSTAN = 202L
    const val ELECTRICITY = 203L

    const val CAR = 300L
    const val FUEL = 301L
    const val PARKING = 302L
    const val REGISTRATION = 303L

    const val INCOME = 400L
    const val SALARY = 401L

    const val PERSONAL = 500L
    const val DRUGSTORE = 501L
    const val PHARMACY = 502L

    const val SHOPPING = 600L
    const val CLOTHING = 601L
    const val ONLINE = 602L
    const val ELECTRONICS = 603L
    const val HOME = 604L

    const val LEISURE = 700L
    const val ENTERTAINMENT = 701L
    const val SUBSCRIPTIONS = 702L

    const val ONLINE_PAYMENTS = 800L
    const val VIRTUAL_CARD = 801L
    const val TRANSFERS = 900L

    val categories = listOf(
        category(FOOD, "category_food"),
        category(GROCERIES, "category_groceries", FOOD),
        category(FAST_FOOD, "category_fast_food", FOOD),
        category(DELIVERY, "category_delivery", FOOD),
        category(KIOSK, "category_kiosk", FOOD),
        category(UTILITIES, "category_utilities"),
        category(PHONE_INTERNET, "category_phone_internet", UTILITIES),
        category(INFOSTAN, "category_infostan", UTILITIES),
        category(ELECTRICITY, "category_electricity", UTILITIES),
        category(CAR, "category_car"),
        category(FUEL, "category_fuel", CAR),
        category(PARKING, "category_parking", CAR),
        category(REGISTRATION, "category_registration", CAR),
        category(INCOME, "category_income"),
        category(SALARY, "category_salary", INCOME),
        category(PERSONAL, "category_personal"),
        category(DRUGSTORE, "category_drugstore", PERSONAL),
        category(PHARMACY, "category_pharmacy", PERSONAL),
        category(SHOPPING, "category_shopping"),
        category(CLOTHING, "category_clothing", SHOPPING),
        category(ONLINE, "category_online", SHOPPING),
        category(ELECTRONICS, "category_electronics", SHOPPING),
        category(HOME, "category_home", SHOPPING),
        category(LEISURE, "category_leisure"),
        category(ENTERTAINMENT, "category_entertainment", LEISURE),
        category(SUBSCRIPTIONS, "category_subscriptions", LEISURE),
        category(ONLINE_PAYMENTS, "category_online_payments"),
        category(VIRTUAL_CARD, "category_virtual_card", ONLINE_PAYMENTS),
        category(TRANSFERS, "category_transfers")
    )

    val keywordRules = buildList {
        addRules("EXP - Hrana / Namirnice - marketi", GROCERIES, TransactionType.EXPENSE,
            "MAXI", "C MARKET", "AMAN", "AROMA", "LIDL", "IDEA", "MP415 SM MIRIJEVO", "STKR SUMADIJA")
        addRules("EXP - Hrana / Brza hrana - pekare", FAST_FOOD, TransactionType.EXPENSE,
            "HLEB I KIFLE", "TRGOCENTAR", "TR KIFLICE", "KIFLICE-KEREFEKE")
        addRules("EXP - Hrana / Dostava - Glovo", DELIVERY, TransactionType.EXPENSE, "GLOVO")
        addRules("EXP - Hrana / Brza hrana - fast food", FAST_FOOD, TransactionType.EXPENSE,
            "GIROS SERBIA", "GIROS I AJSKRIMOS", "NUTYNO", "SMASH BURGERS", "CREPERIE HARIS", "NADIJE JASARI PR BANJA")
        addRules("EXP - Hrana / Kiosk - sitna hrana", KIOSK, TransactionType.EXPENSE,
            "STAMPA SISTEM", "MOJ KIOSK", "CITY")
        addRules("EXP - Komunalne / Telefon + Internet - Yettel", PHONE_INTERNET, TransactionType.EXPENSE,
            "YETTEL ERACUN APP", "YETTEL ERACUN", "YETTEL ERAČUN")
        addRules("EXP - Komunalne / Infostan", INFOSTAN, TransactionType.EXPENSE, "JKP INFOSTAN")
        addRules("EXP - Komunalne / Struja", ELECTRICITY, TransactionType.EXPENSE, "EPS")
        addRules("EXP - Auto / Gorivo", FUEL, TransactionType.EXPENSE,
            "MOL SERBIA", "NIS", "PETROL", "CORAL SRB")
        addRules("EXP - Auto / Parking", PARKING, TransactionType.EXPENSE,
            "PARKING", "PARKING SERVIS", "PG VUKOV SPOMENIK", "VEPP BABA VISNJINA")
        addRules("INC - Prihodi / Plata - JetBrains", SALARY, TransactionType.INCOME,
            "JETBRAINS D.O.O", "JETBRAINS DOO", "JETBRAINS")
        addRules("EXP - Licni / Drogerija - DM i Lilly", DRUGSTORE, TransactionType.EXPENSE,
            "DM", "LILLY", "J051", "J051 BEOGRAD ZVEZ")
        addRules("EXP - Licni / Apoteka", PHARMACY, TransactionType.EXPENSE,
            "APOTEKA", "PHARMACY", "BENU")
        addRules("EXP - Kupovina / Odeca", CLOTHING, TransactionType.EXPENSE,
            "TOM TAILOR", "JACK JONES", "SPRINGFIELD", "LC WAIKIKI", "PEEK AND CLOPPENBURG", "RESERVED")
        addRules("EXP - Kupovina / Online - Temu", ONLINE, TransactionType.EXPENSE, "TEMU", "TEMU.COM")
        addRules("EXP - Kupovina / Elektronika", ELECTRONICS, TransactionType.EXPENSE,
            "PC CENTAR", "APPLE STORE", "BEST BUY", "GIGATRON", "TEHNOMANIJA")
        addRules("EXP - Kupovina / Home", HOME, TransactionType.EXPENSE,
            "GEVOREST", "IKEA", "JYSK", "URADI SAM")
        addRules("EXP - Slobodno vreme / Zabava", ENTERTAINMENT, TransactionType.EXPENSE,
            "JUMP", "SKAKONICA", "BIOSKOP", "CINEPLEXX")
        addRules("EXP - Auto / Registracija", REGISTRATION, TransactionType.EXPENSE, "GC GROUP")
        addRules("EXP - Slobodno vreme / Pretplate - Apple i Google", SUBSCRIPTIONS, TransactionType.EXPENSE,
            "ITUNES.COM", "GOOGLE GOOGLE ONE")
        addExactRules("EXP - Online plaćanja / Virtuelna kartica - Dopuna", VIRTUAL_CARD,
            TransactionType.EXPENSE,
            "PRENOS U KORIST ${KnownOtpAccounts.LEGACY_VIRTUAL_CARD_ACCOUNT}",
            "INTERNI PRENOS SA RACUNA ${KnownOtpAccounts.CURRENT_ACCOUNT} " +
                "NA RACUN ${KnownOtpAccounts.VIRTUAL_CARD_ACCOUNT}")
        addAnyRules("Transfers - Filip i Violeta", TRANSFERS,
            "PRENOS U KORIST ${KnownOtpAccounts.FILIP_PETROVIC_ACCOUNT}",
            "PRENOS U KORIST ${KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT}",
            "PRILIV SA RACUNA ${KnownOtpAccounts.FILIP_PETROVIC_ACCOUNT}",
            "PRILIV SA RACUNA ${KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT}",
            KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT)
    }.flatten()

    private fun category(id: Long, nameKey: String, parentId: Long? = null) =
        CategoryEntity(id = id, nameKey = nameKey, parentId = parentId)

    private fun MutableList<List<KeywordRuleEntity>>.addRules(
        name: String,
        categoryId: Long,
        type: TransactionType,
        vararg keywords: String
    ) {
        add(keywords.map { keyword ->
            KeywordRuleEntity(
                name = name,
                keyword = keyword,
                categoryId = categoryId,
                transactionType = type.name,
                priority = if (keyword.length <= 3) 10 else 0,
                matchMode = if (keyword.length <= 3) "WHOLE_WORD" else "CONTAINS"
            )
        })
    }

    private fun MutableList<List<KeywordRuleEntity>>.addExactRules(
        name: String,
        categoryId: Long,
        type: TransactionType,
        vararg keywords: String
    ) {
        add(keywords.map { keyword ->
            KeywordRuleEntity(
                name = name,
                keyword = keyword,
                categoryId = categoryId,
                transactionType = type.name,
                matchMode = "EXACT"
            )
        })
    }

    private fun MutableList<List<KeywordRuleEntity>>.addAnyRules(
        name: String,
        categoryId: Long,
        vararg keywords: String
    ) {
        add(keywords.map { keyword ->
            KeywordRuleEntity(
                name = name,
                keyword = keyword,
                categoryId = categoryId,
                transactionType = "ANY",
                matchMode = "CONTAINS"
            )
        })
    }
}
