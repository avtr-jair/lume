package com.jaedhc.lume.domain.parser.ports

interface CategoryRulesRepo{
    suspend fun merchantKeyWords(): Map<Long, List<String>>
    suspend fun concpetKeyWords(): Map<Long, List<String>>
}