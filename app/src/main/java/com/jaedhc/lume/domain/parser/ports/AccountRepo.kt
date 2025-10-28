package com.jaedhc.lume.domain.parser.ports

interface AccountRepo {
    suspend fun resolveByBankAndLast4(bankId: Long, last4: String): AccountRef?
    data class AccountRef(val id: Long, val bankId: Long, val label: String?)
}