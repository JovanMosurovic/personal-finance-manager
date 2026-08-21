package com.jovanmosurovic.personalfinancemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordRuleDao {
    @Query("SELECT * FROM keyword_rules ORDER BY categoryId, priority DESC, id ASC")
    fun observeAll(): Flow<List<KeywordRuleEntity>>

    @Query("SELECT * FROM keyword_rules WHERE enabled = 1 ORDER BY priority DESC, LENGTH(keyword) DESC, id ASC")
    suspend fun getActiveRules(): List<KeywordRuleEntity>

    @Query("SELECT * FROM keyword_rules WHERE categoryId = :categoryId")
    suspend fun getForCategory(categoryId: Long): List<KeywordRuleEntity>

    @Query("SELECT COUNT(*) FROM keyword_rules")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<KeywordRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rule: KeywordRuleEntity)
}
