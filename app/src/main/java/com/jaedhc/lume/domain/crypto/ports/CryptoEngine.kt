package com.jaedhc.lume.domain.crypto.ports

import com.jaedhc.lume.domain.crypto.model.CipherText
import com.jaedhc.lume.domain.crypto.model.KeyPolicy


interface CryptoEngine {
    /**
     * Cifra bytes con la llave activa (según policy) y devuelve CipherText con metadatos.
     * @param plain bytes en claro (no se copian internamente: el caller decide si borrar/wipe).
     * @param policy selecciona alias y spec (rotación).
     * @param aad optional Additional Authenticated Data (GCM).
     */
    fun encrypt(plain: String, policy: KeyPolicy, aad: ByteArray? = null): CipherText

    /**
     * Descifra usando alias/spec presentes en el CipherText y/o candidates de policy.
     * Implementaciones pueden:
     *  - Usar cipher.keyAlias si viene seteado (happy path)
     *  - Probar con fallbacks de policy si no hay alias o falla
     */
    fun decrypt(cipher: CipherText, policy: KeyPolicy, aad: ByteArray? = cipher.aad): ByteArray

}