package com.jovanmosurovic.personalfinancemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 8,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun keywordRuleDao(): KeywordRuleDao

    abstract fun transactionDao(): TransactionDao
}
