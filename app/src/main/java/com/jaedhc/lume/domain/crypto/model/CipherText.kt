package com.jaedhc.lume.domain.crypto.model

/**
 * Resultado de cifrado: payload + IV + tag  + spec y metadatos.
 * Mantén ByteArray, el guardado/base64 ya lo hace la capa de datos.
 */

data class CipherText(
    val iv: ByteArray,
    val payload: ByteArray,  // ciphertext (incluye tag si el engine así lo decide)
    val tag: ByteArray? = null, // opcional si el provider separa TAG
    val spec: CipherSpec = CipherSpec(),
    val aad: ByteArray? = null, // Additional Authenticated Data (opcional)
    val keyAlias: String? = null // útil para auditoría/rotación
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CipherText

        if (!iv.contentEquals(other.iv)) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!tag.contentEquals(other.tag)) return false
        if (spec != other.spec) return false
        if (!aad.contentEquals(other.aad)) return false
        if (keyAlias != other.keyAlias) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + (tag?.contentHashCode() ?: 0)
        result = 31 * result + spec.hashCode()
        result = 31 * result + (aad?.contentHashCode() ?: 0)
        result = 31 * result + (keyAlias?.hashCode() ?: 0)
        return result
    }
}