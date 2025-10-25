package com.jaedhc.lume.data.crypto

import android.content.Context
import androidx.security.crypto.MasterKeys
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import android.util.Log
import com.jaedhc.lume.domain.crypto.model.CipherText
import com.jaedhc.lume.domain.crypto.model.CryptoError
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine
import com.jaedhc.lume.domain.crypto.ports.CryptoKeyProvider
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.inject.Inject


class AesGcmSecurityCryptoEngine @Inject constructor(
    private val keyProvider: CryptoKeyProvider
) : CryptoEngine {

    override fun encrypt(
        plain: String,
        policy: KeyPolicy,
        aad: ByteArray?
    ): CipherText {
        val (alias, spec) = keyProvider.resolveActive(policy)
        val key = obtainKey(alias)

        // Genera IV explícito del largo que dicta la spec (12 bytes es lo típico en GCM)
        val iv = ByteArray(spec.ivBytes).also { SecureRandom().nextBytes(it) }

        val cipher = Cipher.getInstance("${spec.algo}/${spec.mode}/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(spec.tagBytes * 8, iv))
        if (aad != null) cipher.updateAAD(aad)
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return CipherText(iv, ct, spec = spec, aad = aad, keyAlias = alias)
    }

    override fun decrypt(
        cipher: CipherText,
        policy: KeyPolicy,
        aad: ByteArray?
    ): ByteArray {
        val candidates = keyProvider.resolveCandidates(policy)
        for ((alias, spec) in candidates) {
            try {
                val key = obtainKey(alias)
                val instance = Cipher.getInstance("${spec.algo}/${spec.mode}/NoPadding")
                instance.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(spec.tagBytes * 8, cipher.iv))
                aad?.let { instance.updateAAD(it) }
                return instance.doFinal(cipher.payload)
            } catch (_: AEADBadTagException) {
                // probar siguiente alias si falla tag, Rotación de llaves seguras de KeyPolicy
                Log.w("Crypto", "Fallo descifrado con alias $alias")
            }
        }
        throw CryptoError.IntegrityViolation()
    }

    private fun obtainKey(alias: String): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return ks.getKey(alias, null) as? SecretKey
            ?: throw CryptoError.KeyNotFound(alias)
    }

}