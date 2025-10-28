package com.jaedhc.lume.domain.parser.ports

import com.jaedhc.lume.domain.parser.model.TxFields

interface CategoryClassifier {
    suspend fun classify(fields: TxFields, fullText: String): Long?
}