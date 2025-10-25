package com.jaedhc.lume.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaedhc.lume.data.database.entities.BankEntity

@Dao
interface BankDao{
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bank: BankEntity): Long

    @Query("SELECT * FROM bank ORDER BY identifier")
    suspend fun listAll(): List<BankEntity>

    @Query("SELECT * FROM bank WHERE identifier = :identifier")
    suspend fun get(identifier: String): BankEntity?

}