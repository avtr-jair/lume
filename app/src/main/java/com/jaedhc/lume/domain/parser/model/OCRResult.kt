package com.jaedhc.lume.domain.parser.model

data class OcrResult(
    val text: String,         // Texto plano completo reconocido (para auditoría/clasificación)
    val fields: TxFields,     // Campos estructurados parseados
    val categoryId: Long?     // Categoría propuesta según reglas (editable en BD)
)