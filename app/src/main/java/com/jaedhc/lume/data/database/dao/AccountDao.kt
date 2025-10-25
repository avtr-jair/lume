package com.jaedhc.lume.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaedhc.lume.data.database.entities.AccountEntity

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(acc: AccountEntity): Long

    @Query("""
        SELECT * FROM account
        WHERE bancoId = :bankId
        ORDER BY isActive DESC, id DESC
    """)
    suspend fun listByBank(bankId: Long): List<AccountEntity>

    @Query("""
        SELECT * FROM account
        WHERE bancoId = :bankId AND last4_tok = :last4Tok
        LIMIT 1
    """)
    suspend fun findByBankAndLast4Tok(bankId: Long, last4Tok: String): AccountEntity?

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AccountEntity?
}
