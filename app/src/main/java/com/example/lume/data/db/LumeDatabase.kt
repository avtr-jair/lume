package com.example.lume.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, AccountEntity::class],
    version = 3,
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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class LumeDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val catDao = database.transactionDao()
                    val count = catDao.getCategoriesSnapshot().size
                    if (count == 0) {
                        val defaultCategories = listOf(
                            CategoryEntity("comida", "Comida", "Restaurant", "#FACC15", 0),
                            CategoryEntity("transporte", "Transporte", "DirectionsCar", "#60A5FA", 1),
                            CategoryEntity("entretenimiento", "Entretenimiento", "ConfirmationNumber", "#A78BFA", 2),
                            CategoryEntity("salud", "Salud", "MedicalServices", "#F87171", 3),
                            CategoryEntity("finanzas", "Finanzas", "Payments", "#34D399", 4),
                            CategoryEntity("servicios", "Servicios", "Lightbulb", "#FB923C", 5),
                            CategoryEntity("otros", "Otros", "Category", "#94A3B8", 6)
                        )
                        defaultCategories.forEach { catDao.insertCategory(it) }
                    }
                }
            }
        }
    }
}
