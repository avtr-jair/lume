package com.jaedhc.lume.data.repo

import androidx.room.withTransaction
import com.jaedhc.lume.data.crypto.HmacTokenProvider
import com.jaedhc.lume.data.crypto.qualifiers.TxKeyPolicy
import com.jaedhc.lume.data.database.AppDatabase
import com.jaedhc.lume.data.database.dao.TransactionDao
import com.jaedhc.lume.data.database.entities.TransactionEntity
import com.jaedhc.lume.data.mapper.CipherTextSerializer
import com.jaedhc.lume.domain.crypto.model.CipherText
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine
import com.jaedhc.lume.domain.parser.model.Transaction
import com.jaedhc.lume.domain.parser.ports.TransactionRepo
import javax.inject.Inject

class TransactionRepoRoom @Inject constructor(
    private val db: AppDatabase,
    private val dao: TransactionDao,
    private val crypto: CryptoEngine,
    @TxKeyPolicy private val txKeyPolicy: KeyPolicy,
    private val hmac: HmacTokenProvider
) : TransactionRepo {

    override suspend fun insert(tx: Transaction): Transaction {
        val entity = toEntity(tx)
        val newId = db.withTransaction {
            dao.insert(entity)
        }
        val stored = dao.findById(newId) ?: error("Insert failed (id=$newId)")
        return toDomain(stored)
    }

    override suspend fun findIdByBusinessHash(hash: String): Long? {
        return 1
    }

    private fun toEntity(tx: Transaction): TransactionEntity {
        val conceptEnc = tx.concept?.let {
            val cipher = crypto.encrypt(it, txKeyPolicy)
            CipherTextSerializer.serialize(cipher)
        }
        val conceptTko = tx.concept?.let { hmac.token(it) }
        return TransactionEntity(
            id = 0, // autogenerado
            date = tx.date,
            merchantId = tx.merchantId,
            conceptEnc = conceptEnc,
            conceptTok = conceptTko,
            amountMinor = tx.amountMinor,
            currency = tx.currency,
            categoryId = tx.categoryId ?: 1,
            income = tx.income,
            sourceAccId = tx.sourceAccountId,
            sourceAccBankId = tx.sourceBankId,
            hashMerchant = "",
            createdAt = tx.createdAt,
            updatedAt = tx.updatedAt
        )
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