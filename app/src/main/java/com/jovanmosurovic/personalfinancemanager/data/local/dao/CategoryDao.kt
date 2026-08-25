package com.jovanmosurovic.personalfinancemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY parentId, id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getById(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY id")
    suspend fun getChildren(parentId: Long): List<CategoryEntity>

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM categories")
    suspend fun nextId(): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity)

    @Query("UPDATE categories SET nameKey = :name WHERE id = :categoryId AND isSystem = 0")
    suspend fun renameCustomCategory(categoryId: Long, name: String)

    @Query("DELETE FROM categories WHERE id = :categoryId AND isSystem = 0")
    suspend fun deleteById(categoryId: Long)
}
