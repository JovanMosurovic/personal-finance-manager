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

@Database(
    entities = [
        CategoryEntity::class,
        KeywordRuleEntity::class,
        TransactionEntity::class
    ],
    version = 3,
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
