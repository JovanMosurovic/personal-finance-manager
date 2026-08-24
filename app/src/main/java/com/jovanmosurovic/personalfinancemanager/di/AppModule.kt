package com.jovanmosurovic.personalfinancemanager.di

import android.content.Context
import androidx.room.Room
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.local.FinanceDatabase
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_1_3
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_2_3
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_3_4
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_4_5
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_5_6
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_6_7
import com.jovanmosurovic.personalfinancemanager.data.local.MIGRATION_7_8
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFinanceDatabase(
        @ApplicationContext context: Context
    ): FinanceDatabase = Room.databaseBuilder(
        context,
        FinanceDatabase::class.java,
        "personal_finance.db"
    ).addMigrations(
        MIGRATION_1_3,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8
    ).build()

    @Provides
    @Singleton
    fun provideFinanceRepository(database: FinanceDatabase): FinanceRepository =
        FinanceRepository(database)
}
