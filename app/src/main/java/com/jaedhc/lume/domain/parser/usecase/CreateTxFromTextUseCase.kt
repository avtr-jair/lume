package com.jaedhc.lume.domain.parser.usecase

import com.jaedhc.lume.domain.parser.model.OcrInput
import com.jaedhc.lume.domain.parser.model.Transaction
import com.jaedhc.lume.domain.parser.ports.AccountRepo
import com.jaedhc.lume.domain.parser.ports.MerchantRepo
import com.jaedhc.lume.domain.parser.ports.TransactionRepo
import com.jaedhc.lume.domain.parser.services.DefaultOcrEngine
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CreateTxFromTextUseCase @Inject constructor(
    private val ocrEngine: DefaultOcrEngine,     // orquestador OCR: recognizer + parser + classifier
    private val merchantRepo: MerchantRepo,
    private val accountRepo: AccountRepo,
    private val txRepo: TransactionRepo,
) {
    suspend operator fun invoke(
        input: OcrInput,
        bankIdHint: Long? = null,
        last4Hint: String? = null,
        income: Boolean
    ): Transaction {
        val result = ocrEngine.analyze(input)
        val f = result.fields

        // Merchant
        val merchant = merchantRepo.resolveOrCreateByName(f.merchant)

        // Cuenta (si hay suficientes pistas)
        val account = if (bankIdHint != null && !last4Hint.isNullOrBlank()) {
            accountRepo.resolveByBankAndLast4(bankIdHint, last4Hint)
        } else null

        // Category
        val categoryId = result.categoryId

        // Normalización de fecha
        val date = isoToDate(f.dateIso)

        // Monto a minor units
        val amountMinor = toMinor(f.amount)

        val tx = Transaction(
            id = 0L,                     // autogen en Room
            date = date,
            merchantId = merchant?.id,
            concept = f.concept,         // en claro; el repo lo cifrará
            amountMinor = amountMinor,
            currency = f.currency ?: "MXN",
            categoryId = categoryId,
            income = income,
            sourceAccountId = account?.id,
            sourceBankId = account?.bankId,
            createdAt = Date(),
            updatedAt = Date(),
            category = categoryId
        )
        return txRepo.insert(tx)
    }

    private fun toMinor(amount: Double?): Long =
        if (amount == null) 0L else BigDecimal(amount).movePointRight(2).setScale(0).longValueExact()


    private fun isoToDate(iso: String): Date {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.parse(iso) ?: Date()  // devuelve la fecha parseada o "hoy" si falla
        } catch (e: Exception) {
            Date() // fallback
        }
    }
}