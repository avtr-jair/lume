package com.jaedhc.lume.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaedhc.lume.data.database.entities.TransactionEntity
import java.util.Date

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tx: TransactionEntity): Long

    @Query("SELECT * FROM tx WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TransactionEntity?

    // Filtro por banco usando el campo denormalizado de la cuenta
    @Query("""
        SELECT * FROM tx
        WHERE sourceAccBankId = :bankId
        ORDER BY date DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun listByBank(
        bankId: Long,
        limit: Int,
        offset: Int
    ): List<TransactionEntity>

    // Rango de fechas + categoría + montos (en claro)
    @Query("""
        SELECT * FROM tx
        WHERE date BETWEEN :from AND :to
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND amountMinor BETWEEN :minAmount AND :maxAmount
        ORDER BY date DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun search(
        from: Date,
        to: Date,
        categoryId: Long?,
        minAmount: Long,
        maxAmount: Long,
        limit: Int,
        offset: Int
    ): List<TransactionEntity>

    // Idempotencia por hash lógico (OCR/import)
    @Query("SELECT id FROM tx WHERE hashMerchant = :hash LIMIT 1")
    suspend fun findIdByBusinessHash(hash: String): Long?
}
