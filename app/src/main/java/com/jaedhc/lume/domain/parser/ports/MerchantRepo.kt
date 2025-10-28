package com.jaedhc.lume.domain.parser.ports

interface MerchantRepo {
    /** Crea/recupera por nombre (usa tok/enc en data). */
    suspend fun resolveOrCreateByName(rawName: String?): MerchantRef?
    data class MerchantRef(val id: Long, val normalizedName: String?)
}