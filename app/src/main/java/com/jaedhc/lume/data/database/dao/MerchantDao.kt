package com.jaedhc.lume.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaedhc.lume.data.database.entities.MerchantEntity

@Dao
interface MerchantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(m: MerchantEntity): Long

    @Query("SELECT * FROM merchant WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MerchantEntity?

    // búsqueda por igualdad usando token (si lo generas)
    @Query("SELECT * FROM merchant WHERE nameTok = :tok LIMIT 1")
    suspend fun findByTok(tok: String): MerchantEntity?

    @Query("SELECT * FROM merchant ORDER BY id DESC LIMIT :limit")
    suspend fun latest(limit: Int = 50): List<MerchantEntity>
}
