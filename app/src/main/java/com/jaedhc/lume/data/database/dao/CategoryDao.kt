package com.jaedhc.lume.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaedhc.lume.data.database.entities.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cat: CategoryEntity): Long

    @Query("SELECT * FROM category ORDER BY name")
    suspend fun listAll(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CategoryEntity?
}
