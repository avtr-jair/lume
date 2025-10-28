package com.jaedhc.lume.domain.parser.ports

import com.jaedhc.lume.domain.parser.model.Transaction

interface TransactionRepo {
    suspend fun insert(tx: Transaction): Transaction
    suspend fun findIdByBusinessHash(hash: String): Long?
}