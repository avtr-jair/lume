package com.example.lume.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LumeDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: LumeDatabase? = null

        fun getDatabase(context: Context): LumeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumeDatabase::class.java,
                    "lume_database"
                )
                .addCallback(LumeDatabaseCallback(CoroutineScope(Dispatchers.IO)))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class LumeDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val catDao = database.transactionDao()
                    val defaultCategories = listOf(
                        CategoryEntity("comida", "Comida", null),
                        CategoryEntity("transporte", "Transporte", null),
                        CategoryEntity("entretenimiento", "Entretenimiento", null),
                        CategoryEntity("salud", "Salud", null),
                        CategoryEntity("finanzas", "Finanzas", null),
                        CategoryEntity("servicios", "Servicios", null),
                        CategoryEntity("otros", "Otros", null)
                    )
                    defaultCategories.forEach { catDao.insertCategory(it) }
                }
            }
        }
    }
}
