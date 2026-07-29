package com.shawon.fundflow.core.di

import android.content.Context
import androidx.room.Room
import com.shawon.fundflow.data.local.FundFlowDatabase
import com.shawon.fundflow.data.local.dao.BudgetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FundFlowDatabase {
        return Room.databaseBuilder(
            context,
            FundFlowDatabase::class.java,
            "fundflow.db"
        ).build()
    }

    @Provides
    fun provideBudgetDao(database: FundFlowDatabase): BudgetDao {
        return database.budgetDao()
    }
}
