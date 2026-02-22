package com.example.lume.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TxFields(
    @SerialName("amount") val amount: Double?,
    @SerialName("currency") val currency: String? = null,
    @SerialName("date") val date: String?,
    @SerialName("merchant") val merchant: String?,
    @SerialName("concept") val concept: String?,
    @SerialName("category") val category: String?,
    @SerialName("suggested_category") val suggested_category: String? = null,
    @SerialName("type") val type: String?, // "ingreso" or "egreso"
    @SerialName("is_subscription") val is_subscription: Boolean = false
)

data class OcrResult(val text: String, val fields: TxFields, val category: Category)

@Serializable
data class StructuralOcrResult(
    val amount_candidates: List<Double>,
    val date_candidates: List<String>,
    val text_lines: List<String>,
    val categories: List<String>
)

enum class Category { COMIDA, TRANSPORTE, ENTRETENIMIENTO, SALUD, FINANZAS, SERVICIOS, OTROS }
