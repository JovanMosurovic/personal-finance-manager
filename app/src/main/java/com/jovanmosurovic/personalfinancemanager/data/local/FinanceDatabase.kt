package com.jovanmosurovic.personalfinancemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jovanmosurovic.personalfinancemanager.data.local.dao.CategoryDao
import com.jovanmosurovic.personalfinancemanager.data.local.dao.KeywordRuleDao
import com.jovanmosurovic.personalfinancemanager.data.local.dao.TransactionDao
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.KnownOtpAccounts

@Database(
    entities = [
        CategoryEntity::class,
        KeywordRuleEntity::class,
        TransactionEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun keywordRuleDao(): KeywordRuleDao

    abstract fun transactionDao(): TransactionDao
}

val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(db: SupportSQLiteDatabase) = Unit
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS budgets")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO categories (id, nameKey, parentId, isSystem)
            VALUES (800, 'category_online_payments', NULL, 1)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO categories (id, nameKey, parentId, isSystem)
            VALUES (801, 'category_virtual_card', 800, 1)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO keyword_rules (
                name, keyword, categoryId, transactionType, priority, enabled, matchMode
            )
            SELECT
                'EXP - Online plaćanja / Virtuelna kartica - Dopuna',
                '9120726623676',
                801,
                'EXPENSE',
                0,
                1,
                'EXACT'
            WHERE NOT EXISTS (
                SELECT 1
                FROM keyword_rules
                WHERE categoryId = 801 AND keyword = '9120726623676'
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM keyword_rules
            WHERE categoryId = 801
                AND keyword = 'PRENOS U KORIST'
                AND name = 'EXP - Online plaćanja / Virtuelna kartica - Prenos u korist'
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO keyword_rules (
                name, keyword, categoryId, transactionType, priority, enabled, matchMode
            )
            SELECT
                'EXP - Online plaćanja / Virtuelna kartica - Dopuna',
                '9120726623676',
                801,
                'EXPENSE',
                0,
                1,
                'EXACT'
            WHERE NOT EXISTS (
                SELECT 1
                FROM keyword_rules
                WHERE categoryId = 801 AND keyword = '9120726623676'
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            UPDATE finance_transactions
            SET categoryId = NULL, matchedRuleId = NULL
            WHERE categoryId = 801 AND isManuallyCategorized = 0
            """.trimIndent()
        )
        db.execSQL(
            """
            DELETE FROM keyword_rules
            WHERE categoryId = 801
                AND keyword = '9120726623676'
                AND name = 'EXP - Online plaćanja / Virtuelna kartica - Dopuna'
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO keyword_rules (
                name, keyword, categoryId, transactionType, priority, enabled, matchMode
            )
            SELECT
                'EXP - Online plaćanja / Virtuelna kartica - Dopuna',
                'PRENOS U KORIST 9120726623676',
                801,
                'EXPENSE',
                0,
                1,
                'EXACT'
            WHERE NOT EXISTS (
                SELECT 1
                FROM keyword_rules
                WHERE categoryId = 801
                    AND keyword = 'PRENOS U KORIST 9120726623676'
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO keyword_rules (
                name, keyword, categoryId, transactionType, priority, enabled, matchMode
            )
            SELECT
                'EXP - Online plaćanja / Virtuelna kartica - Dopuna',
                'INTERNI PRENOS SA RACUNA 325930070633456009 NA RACUN 325912072662367691',
                801,
                'EXPENSE',
                0,
                1,
                'EXACT'
            WHERE NOT EXISTS (
                SELECT 1
                FROM keyword_rules
                WHERE categoryId = 801
                    AND keyword = 'INTERNI PRENOS SA RACUNA 325930070633456009 NA RACUN 325912072662367691'
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val legacyVirtualCardTransfer =
            "PRENOS U KORIST ${KnownOtpAccounts.LEGACY_VIRTUAL_CARD_ACCOUNT}"
        val currentVirtualCardTransfer =
            "INTERNI PRENOS SA RACUNA ${KnownOtpAccounts.CURRENT_ACCOUNT} " +
                "NA RACUN ${KnownOtpAccounts.VIRTUAL_CARD_ACCOUNT}"

        db.execSQL(
            """
            UPDATE finance_transactions
            SET categoryId = NULL, matchedRuleId = NULL
            WHERE categoryId = 801 AND isManuallyCategorized = 0
            """.trimIndent()
        )
        db.execSQL(
            """
            DELETE FROM keyword_rules
            WHERE categoryId = 801
                AND name LIKE 'EXP - Online plaćanja / Virtuelna kartica%'
                AND keyword NOT IN ('$legacyVirtualCardTransfer', '$currentVirtualCardTransfer')
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE keyword_rules
            SET matchMode = 'EXACT'
            WHERE categoryId = 801
                AND keyword IN ('$legacyVirtualCardTransfer', '$currentVirtualCardTransfer')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO keyword_rules (
                name, keyword, categoryId, transactionType, priority, enabled, matchMode
            )
            SELECT
                'EXP - Online plaćanja / Virtuelna kartica - Dopuna',
                '$legacyVirtualCardTransfer',
                801,
                'EXPENSE',
                0,
                1,
                'EXACT'
            WHERE NOT EXISTS (
                SELECT 1 FROM keyword_rules
                WHERE categoryId = 801 AND keyword = '$legacyVirtualCardTransfer'
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO keyword_rules (
                name, keyword, categoryId, transactionType, priority, enabled, matchMode
            )
            SELECT
                'EXP - Online plaćanja / Virtuelna kartica - Dopuna',
                '$currentVirtualCardTransfer',
                801,
                'EXPENSE',
                0,
                1,
                'EXACT'
            WHERE NOT EXISTS (
                SELECT 1 FROM keyword_rules
                WHERE categoryId = 801 AND keyword = '$currentVirtualCardTransfer'
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO categories (id, nameKey, parentId, isSystem)
            VALUES (900, 'category_transfers', NULL, 1)
            """.trimIndent()
        )

        val transferKeywords = listOf(
            "PRENOS U KORIST ${KnownOtpAccounts.FILIP_PETROVIC_ACCOUNT}",
            "PRENOS U KORIST ${KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT}",
            "PRILIV SA RACUNA ${KnownOtpAccounts.FILIP_PETROVIC_ACCOUNT}",
            "PRILIV SA RACUNA ${KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT}",
            KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT
        )

        transferKeywords.forEach { keyword ->
            db.execSQL(
                """
                INSERT INTO keyword_rules (
                    name, keyword, categoryId, transactionType, priority, enabled, matchMode
                )
                SELECT
                    'Transfers - Filip i Violeta',
                    '$keyword',
                    900,
                    'ANY',
                    0,
                    1,
                    'CONTAINS'
                WHERE NOT EXISTS (
                    SELECT 1 FROM keyword_rules
                    WHERE categoryId = 900 AND keyword = '$keyword'
                )
                """.trimIndent()
            )
        }
    }
}
