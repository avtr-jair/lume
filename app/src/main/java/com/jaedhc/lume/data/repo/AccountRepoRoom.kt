package com.jaedhc.lume.data.repo

import com.jaedhc.lume.data.crypto.HmacTokenProvider
import com.jaedhc.lume.data.database.dao.AccountDao
import com.jaedhc.lume.domain.parser.ports.AccountRepo
import javax.inject.Inject

class AccountRepoRoom @Inject constructor(
    private val dao: AccountDao,
    private val hmac: HmacTokenProvider
) : AccountRepo {
    override suspend fun resolveByBankAndLast4(
        bankId: Long,
        last4: String
    ): AccountRepo.AccountRef? {
        val tok = hmac.token(last4)
        val acc = dao.findByBankAndLast4Tok(bankId, tok) ?: return null
        // labelEnc se descifra en otro repo si necesitas el label claro. Aquí devolvemos lo esencial.
        return AccountRepo.AccountRef(
            id = acc.id,
            bankId = acc.bancoId,
            label = null // si quieres descifrar el label aquí, inyecta CryptoEngine y hazlo
        )
    }
}