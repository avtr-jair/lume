package com.jaedhc.lume.domain.parser.model

import java.util.Date

data class Transaction(
    val id: Long,
    val date: Date,
    val merchantId: Long?,
    val concept: String?,
    val amountMinor: Long,
    val currency: String = "MXN",
    val categoryId: Long?,
    val income: Boolean = true,
    val sourceAccountId: Long?,
    val sourceBankId: Long?, // domain/model/Account
    val category: Long?, // domain/model/Category
    val createdAt: Date,
    val updatedAt: Date
)

data class TransactionWithRefs(
    val tx: Transaction,
    val accountLabel: String?,    // descifrada (labelEnc de la cuenta)
    val categoryName: String,     // de catálogo
    val merchantName: String?     // descifrada (nameEnc)
)