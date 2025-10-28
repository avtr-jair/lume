package com.jaedhc.lume.domain.parser.ports

import com.jaedhc.lume.domain.parser.model.Transaction
import java.util.Date

interface TransactionQueryRepo {
    suspend fun search(
        from: Date,
        to: Date,
        categoryId: Long?,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        bankId: Long?,
        limit: Int,
        offset: Int
    ): List<Transaction>
}