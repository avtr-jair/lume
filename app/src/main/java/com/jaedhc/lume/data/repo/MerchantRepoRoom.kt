package com.jaedhc.lume.data.repo

import com.jaedhc.lume.data.crypto.HmacTokenProvider
import com.jaedhc.lume.data.crypto.qualifiers.MerchantKeyPolicy
import com.jaedhc.lume.data.database.dao.MerchantDao
import com.jaedhc.lume.data.database.entities.MerchantEntity
import com.jaedhc.lume.data.mapper.CipherTextSerializer
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine
import com.jaedhc.lume.domain.parser.ports.MerchantRepo
import javax.inject.Inject

class MerchantRepoRoom @Inject constructor(
    private val dao: MerchantDao,
    private val crypto: CryptoEngine,
    @MerchantKeyPolicy private val merchantKeyPolicy: KeyPolicy,
    private val hmac: HmacTokenProvider
) : MerchantRepo {
    override suspend fun resolveOrCreateByName(rawName: String?): MerchantRepo.MerchantRef? {
        val norm = rawName?.trim()?.lowercase().takeIf { it!!.isNotBlank() } ?: return null
        val tok = hmac.token(norm)

        val existing = dao.findByTok(tok)
        if (existing != null) return MerchantRepo.MerchantRef(existing.id, norm)

        val enc = rawName?.let {
            val cipher = crypto.encrypt(rawName, merchantKeyPolicy)
            CipherTextSerializer.serialize(cipher)
        }
        val entity = MerchantEntity(
            id = 0,
            nameEnc = enc,
            nameTok = tok,
            defaultCategoryId = null
        )
        val id = dao.insert(entity)
        val saved = if (id == -1L) dao.findByTok(tok)!! else dao.findById(id)!!
        return MerchantRepo.MerchantRef(saved.id, norm)
    }
}