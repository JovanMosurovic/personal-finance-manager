package com.jovanmosurovic.personalfinancemanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "finance_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("dateEpochDay")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amountMinor: Long,
    val currency: String = "RSD",
    val merchant: String,
    val note: String = "",
    val dateEpochDay: Long,
    val categoryId: Long? = null,
    val matchedRuleId: Long? = null,
    val isManuallyCategorized: Boolean = false
)
