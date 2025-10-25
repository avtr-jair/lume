package com.jaedhc.lume.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jaedhc.lume.data.database.dao.BankDao
import com.jaedhc.lume.data.database.entities.AccountEntity
import com.jaedhc.lume.data.database.entities.BankEntity
import com.jaedhc.lume.data.database.entities.CategoryEntity
import com.jaedhc.lume.data.database.converters.DbConverters
import com.jaedhc.lume.data.database.dao.AccountDao
import com.jaedhc.lume.data.database.dao.CategoryDao
import com.jaedhc.lume.data.database.dao.MerchantDao
import com.jaedhc.lume.data.database.dao.TransactionDao
import com.jaedhc.lume.data.database.entities.MerchantEntity
import com.jaedhc.lume.data.database.entities.TransactionEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        BankEntity::class,
        CategoryEntity::class,
        MerchantEntity::class,
        AccountEntity::class,
        TransactionEntity::class
    ]
)
@TypeConverters(DbConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantDao(): MerchantDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
}