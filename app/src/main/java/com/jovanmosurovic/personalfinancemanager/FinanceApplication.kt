package com.jovanmosurovic.personalfinancemanager

import android.app.Application
import androidx.room.Room
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.local.FinanceDatabase

class FinanceApplication : Application() {
    val database: FinanceDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FinanceDatabase::class.java,
            "personal_finance.db"
        ).build()
    }

    val repository: FinanceRepository by lazy {
        FinanceRepository(database)
    }
}
