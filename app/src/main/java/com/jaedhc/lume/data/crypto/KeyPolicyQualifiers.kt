// data/crypto/qualifiers/KeyPolicyQualifiers.kt
package com.jaedhc.lume.data.crypto.qualifiers

import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class TxKeyPolicy

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class MerchantKeyPolicy

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class AccountKeyPolicy

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class RealCrypto

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class NoOpCrypto
