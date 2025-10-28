package com.jaedhc.lume.di

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.jaedhc.lume.data.database.AppDatabase
import com.jaedhc.lume.data.database.dao.AccountDao
import com.jaedhc.lume.data.database.dao.BankDao
import com.jaedhc.lume.data.database.dao.CategoryDao
import com.jaedhc.lume.data.database.dao.MerchantDao
import com.jaedhc.lume.data.database.dao.TransactionDao
import com.jaedhc.lume.data.database.entities.BankEntity
import com.jaedhc.lume.data.database.entities.CategoryEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // DAOs (no hace falta @Singleton)
    @Provides fun provideBankDao(db: AppDatabase): BankDao = db.bankDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideMerchantDao(db: AppDatabase): MerchantDao = db.merchantDao() // ← FALTABA
    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides @Singleton
    fun runSeed(db: AppDatabase, appScope: CoroutineScope): Any {
        appScope.launch {
            db.withTransaction {
                val bankDao = db.bankDao()
                val banks = listOf("Banorte", "Nu", "BBVA", "Santander")
                banks.forEach { name ->
                    bankDao.insert(BankEntity(identifier = name))
                }

                val categoryDao = db.categoryDao()
                val categories = listOf("entretenimiento","comida","transporte","salud","finanzas","servicios","otros")
                categories.forEach { name ->
                    categoryDao.insert(CategoryEntity(name = name))
                }
            }
        }
        return Unit
    }
}