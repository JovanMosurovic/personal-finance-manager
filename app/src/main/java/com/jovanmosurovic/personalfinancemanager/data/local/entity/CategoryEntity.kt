package com.jovanmosurovic.personalfinancemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val nameKey: String,
    val parentId: Long? = null,
    val isSystem: Boolean = true
)
