package com.jaedhc.lume.domain.crypto.model

sealed class CryptoError(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class KeyNotFound(val alias: String) : CryptoError("Key not found: $alias")
    class InvalidCiphertext : CryptoError("Invalid ciphertext")
    class IntegrityViolation : CryptoError("Integrity/Authentication tag check failed")
    class UnsupportedSpec(val version: Int) : CryptoError("Unsupported cipher spec version: $version")
    class Internal(cause: Throwable? = null) : CryptoError("Internal crypto error", cause)
}