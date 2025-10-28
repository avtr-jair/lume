package com.jaedhc.lume.data.repo

import com.jaedhc.lume.data.database.dao.MerchantDao
import com.jaedhc.lume.domain.parser.ports.CategoryRulesRepo
import javax.inject.Inject

class CategoryRulesRepoRoom @Inject constructor(
    private val merchantDao: MerchantDao
): CategoryRulesRepo {
    override suspend fun merchantKeyWords(): Map<Long, List<String>> {
        val all = merchantDao.latest(500) // o listAll()
        val map = mutableMapOf<Long, MutableList<String>>()
        for (m in all) {
            val cat = m.defaultCategoryId ?: continue
            // Usa el nombreTok como keyword principal si normalizas igual en classifier,
            // o mejor guarda y usa un "display/normalized" real en otra columna si lo tienes.
            val keyword = m.nameTok ?: continue
            map.getOrPut(cat) { mutableListOf() }.add(keyword)
        }
        return map
    }

    override suspend fun concpetKeyWords(): Map<Long, List<String>> {
        return emptyMap()
    }
}