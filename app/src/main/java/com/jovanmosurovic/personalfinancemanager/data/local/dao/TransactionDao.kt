package com.jovanmosurovic.personalfinancemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM finance_transactions ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM finance_transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getById(transactionId: Long): TransactionEntity?

    @Query("SELECT * FROM finance_transactions WHERE categoryId IS NULL")
    suspend fun getUncategorized(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM finance_transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long)

    @Query("UPDATE finance_transactions SET categoryId = :categoryId, matchedRuleId = NULL, isManuallyCategorized = 1 WHERE id = :transactionId")
    suspend fun assignCategory(transactionId: Long, categoryId: Long)

    @Query("UPDATE finance_transactions SET categoryId = :categoryId, matchedRuleId = :ruleId, isManuallyCategorized = 0 WHERE id = :transactionId")
    suspend fun applyAutomaticCategory(transactionId: Long, categoryId: Long, ruleId: Long)
}
