package com.jaedhc.lume.data.crypto

import com.jaedhc.lume.domain.crypto.model.CipherSpec
import com.jaedhc.lume.domain.crypto.model.CipherText
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine

class NoOpCryptoEngine : CryptoEngine {

    private val spec = CipherSpec(
        algo = "NOOP",
        mode = "NONE",
        keyBits = 0,
        ivBytes = 0,
        tagBytes = 0,
        version = 0
    )

    override fun encrypt(
        plain: String,
        policy: KeyPolicy,
        aad: ByteArray?
    ): CipherText {
        return CipherText(
            iv = ByteArray(0),
            payload = plain.toByteArray(Charsets.UTF_8),
            tag = null,
            spec = spec,
            aad = null,
            keyAlias = "noop"
        )
    }

    override fun decrypt(
        cipher: CipherText,
        policy: KeyPolicy,
        aad: ByteArray?
    ): ByteArray {
        return cipher.payload.clone()
    }

}