package com.jaedhc.lume.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.ChecksSdkIntAtLeast
import com.jaedhc.lume.domain.crypto.model.CipherSpec
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoKeyProvider
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeystoreKeySupplier(
    private val keyBits: Int = 256,
    private val ivBytes: Int = 12,
    private val tagBytes: Int = 16
): CryptoKeyProvider{
    override fun resolveActive(policy: KeyPolicy): Pair<String, CipherSpec> {
        val alias = policy.activeAlias
        ensureAesGcmKeyExists(alias, policy.requireHardwareBacked)
        return alias to defaultSpec(alias)
    }

    override fun resolveCandidates(policy: KeyPolicy): List<Pair<String, CipherSpec>> {
        val allAlias = buildList {
            add(policy.activeAlias)
            addAll(policy.fallbackAliases)
        }.distinct()

        return allAlias.map { alias -> alias to defaultSpec(alias) }
    }

    private fun defaultSpec(alias: String) = CipherSpec(
        algo = "AES",
        mode = "GCM",
        keyBits = keyBits,
        ivBytes = ivBytes,
        tagBytes = tagBytes,
        version = parseVersionFromAlias(alias) ?: 1
    )

    private fun parseVersionFromAlias(alias: String): Int? {
        // Permite alias como "app_aes_v3" → version 3
        val m = Regex("""[_\-]v(\d+)$""").find(alias) ?: return null
        return m.groupValues[1].toIntOrNull()
    }

    private fun ensureAesGcmKeyExists(alias: String, requireHardwareBacked: Boolean){
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = ks.getKey(alias, null) as? SecretKey
        if (existing != null) return

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(keyBits)
            .setRandomizedEncryptionRequired(true) // GCM requiere IV aleatorio

        if (requireHardwareBacked && supportsStrongBox()) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGen.init(builder.build())
        keyGen.generateKey()
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    private fun supportsStrongBox(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

}