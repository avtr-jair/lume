package com.jaedhc.lume.data.database

import androidx.room.withTransaction
import com.jaedhc.lume.data.database.dao.BankDao
import com.jaedhc.lume.data.database.dao.CategoryDao
import com.jaedhc.lume.data.database.entities.BankEntity
import com.jaedhc.lume.data.database.entities.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

// data/seed/SeedRunner.kt
@Singleton
class SeedRunner @Inject constructor(
    private val db: AppDatabase,
    private val bankDao: BankDao,
    private val categoryDao: CategoryDao,
    @ApplicationScope private val appScope: CoroutineScope, // tu scope de app
) {
    fun run() {
        appScope.launch {
            db.withTransaction {
                listOf("Banorte","Nu","BBVA","Santander")
                    .forEach { bankDao.insert(BankEntity(identifier = it)) }

                listOf("entretenimiento","comida","transporte","salud","finanzas","servicios","otros")
                    .forEach { categoryDao.insert(CategoryEntity(name = it)) }
            }
        }
    }
}
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
