package com.jaedhc.lume.domain.crypto.ports

import com.jaedhc.lume.domain.crypto.model.CipherSpec
import com.jaedhc.lume.domain.crypto.model.KeyPolicy

/**
 * Puerto para que el dominio pida llaves/metadata según una política.
 * La implementación puede usar Android Keystore, archivos, KMS, etc. (en data/crypto).
 */
interface CryptoKeyProvider {
    /** Devuelve alias activo a usar para cifrar, y spec asociada (versión, iv size, etc.). */
    fun resolveActive(policy: KeyPolicy): Pair<String, CipherSpec>

    /** Devuelve la lista de alias aceptados para intentos de decrifrado (rotaciones previas). */
    fun resolveCandidates(policy: KeyPolicy): List<Pair<String, CipherSpec>>
}