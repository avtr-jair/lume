package com.jaedhc.lume.domain.parser.model

sealed class OcrInput {
    data class Bytes(val data: ByteArray, val mime: String? = null) : OcrInput() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Bytes

            if (!data.contentEquals(other.data)) return false
            if (mime != other.mime) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + (mime?.hashCode() ?: 0)
            return result
        }
    }

    data class Base64(val b64: String, val mime: String? = null) : OcrInput()
    data class PlainText(val text: String) : OcrInput() // Útil para pruebas o cuando ya tienes texto
}