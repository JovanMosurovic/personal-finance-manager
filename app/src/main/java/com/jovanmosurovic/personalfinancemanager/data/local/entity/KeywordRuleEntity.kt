package com.jovanmosurovic.personalfinancemanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "keyword_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("keyword")]
)
data class KeywordRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val keyword: String,
    val categoryId: Long,
    val transactionType: String = "ANY",
    val priority: Int = 0,
    val enabled: Boolean = true,
    val matchMode: String = "CONTAINS"
)
