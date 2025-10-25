package com.jaedhc.lume.domain.crypto.model

/**
 * Política declarativa para seleccionar/rotar llaves desde el dominio.
 * La implementación concreta vive en data/crypto.
 */
data class KeyPolicy(
    val activeAlias: String,                            // alias actual (p. ej. "app_aes_v2")
    val fallbackAliases: List<String> = emptyList(),    // para decrifrado legacy
    val requireHardwareBacked: Boolean = false,         // preferencia (Android Keystore)
    val allowNoOp: Boolean = false                      // Activar/Desactivar Cifrado
)