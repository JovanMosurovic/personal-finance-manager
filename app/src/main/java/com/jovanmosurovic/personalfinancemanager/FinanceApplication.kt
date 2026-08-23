package com.jovanmosurovic.personalfinancemanager

import android.app.Application
import androidx.room.Room
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.local.FinanceDatabase
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_1_3
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_2_3

class FinanceApplication : Application() {
    val database: FinanceDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FinanceDatabase::class.java,
            "personal_finance.db"
        ).addMigrations(MIGRATION_1_3, MIGRATION_2_3).build()
    }

    val repository: FinanceRepository by lazy {
        FinanceRepository(database)
    }
}
