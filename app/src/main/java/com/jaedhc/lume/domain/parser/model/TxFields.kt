package com.jaedhc.lume.domain.parser.model

data class TxFields(
    val amount: Double,
    val dateIso: String,
    val merchant: String,
    val concept: String,
    val currency: String?
)