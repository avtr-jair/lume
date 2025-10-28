package com.jaedhc.lume.domain.parser.services

import com.jaedhc.lume.domain.parser.model.TxFields
import com.jaedhc.lume.domain.parser.ports.CategoryClassifier
import com.jaedhc.lume.domain.parser.ports.CategoryRulesRepo

class RulesCategoryClassifier (
    private val rulesRepo: CategoryRulesRepo
) : CategoryClassifier {

    override suspend fun classify(
        fields: TxFields,
        fullText: String
    ): Long? {
        // Prepara texto unificado en minúsculas para buscar coincidencias
        val haystack = buildString {
            appendLine(fullText.lowercase())
            appendLine(fields.merchant.lowercase())
            appendLine(fields.concept.lowercase())
        }

        // Obtiene todas las reglas desde BD
        val merchantRules = rulesRepo.merchantKeyWords()
        val conceptRules = rulesRepo.concpetKeyWords()

        // Primero intenta coincidencias por merchant
        for ((categoryId, keywords) in merchantRules) {
            if (keywords.any { haystack.contains(it.lowercase()) }) {
                return categoryId
            }
        }

        // Si no hay match, intenta por concepto
        for ((categoryId, keywords) in conceptRules) {
            if (keywords.any { haystack.contains(it.lowercase()) }) {
                return categoryId
            }
        }

        return null
    }
}