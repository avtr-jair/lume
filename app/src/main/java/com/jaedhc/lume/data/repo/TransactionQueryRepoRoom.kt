package com.jaedhc.lume.data.repo

import com.jaedhc.lume.data.crypto.qualifiers.TxKeyPolicy
import com.jaedhc.lume.data.database.dao.TransactionDao
import com.jaedhc.lume.data.database.entities.TransactionEntity
import com.jaedhc.lume.data.mapper.CipherTextSerializer
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine
import com.jaedhc.lume.domain.parser.model.Transaction
import com.jaedhc.lume.domain.parser.ports.TransactionQueryRepo
import java.util.Date
import javax.inject.Inject

class TransactionQueryRepoRoom @Inject constructor(
    private val dao: TransactionDao,
    private val crypto: CryptoEngine,
    @TxKeyPolicy private val txKeyPolicy: KeyPolicy
) : TransactionQueryRepo {

    override suspend fun search(
        from: Date,
        to: Date,
        categoryId: Long?,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        bankId: Long?,
        limit: Int,
        offset: Int
    ): List<Transaction> {
        val min = minAmountMinor ?: Long.MIN_VALUE
        val max = maxAmountMinor ?: Long.MAX_VALUE
        val rows = dao.search(
            from = from,
            to = to,
            categoryId = categoryId,
            minAmount = min,
            maxAmount = max, limit, offset)
        return rows.map(::toDomain)
    }

    private fun toDomain(e: TransactionEntity): Transaction {
        val concept = e.conceptEnc?.let {
            val ct = CipherTextSerializer.deserialize(it)
            val bytes = crypto.decrypt(ct, txKeyPolicy)
            bytes.decodeToString()
        }
        return Transaction(
            id = e.id,
            date = e.date,
            merchantId = e.merchantId,
            concept = concept,
            amountMinor = e.amountMinor,
            currency = e.currency,
            categoryId = e.categoryId,
            income = e.income,
            sourceAccountId = e.sourceAccId,
            sourceBankId = e.sourceAccBankId,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt,
            category = e.categoryId
        )
    }
}