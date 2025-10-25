package com.jaedhc.lume.domain.crypto.model

/**
 * Metadatos del esquema criptográfico (versiónable).
 * Es útil para rotación de llaves y compatibilidad hacia atrás.
 */
data class CipherSpec(
    val algo: String = "AES",
    val mode: String = "GCM",
    val keyBits: Int = 256,
    val ivBytes: Int = 12,
    val tagBytes: Int = 16,
    val version: Int = 1
)