package com.example.lume.data

data class TxFields(
    val amount: Double?, val currency: String?,
    val dateIso: String?, val merchant: String?, val concept: String?
)

data class OcrResult(val text: String, val fields: TxFields, val category: Category)

enum class Category { COMIDA, TRANSPORTE, ENTRETENIMIENTO, SALUD, FINANZAS, SERVICIOS, OTROS }
