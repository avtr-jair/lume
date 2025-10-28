package com.jaedhc.lume.di

import android.content.Context
import com.jaedhc.lume.BuildConfig
import com.jaedhc.lume.data.crypto.AesGcmSecurityCryptoEngine
import com.jaedhc.lume.data.crypto.AndroidKeystoreKeySupplier
import com.jaedhc.lume.data.crypto.HmacTokenProvider
import com.jaedhc.lume.data.crypto.NoOpCryptoEngine
import com.jaedhc.lume.data.crypto.qualifiers.AccountKeyPolicy
import com.jaedhc.lume.data.crypto.qualifiers.MerchantKeyPolicy
import com.jaedhc.lume.data.crypto.qualifiers.TxKeyPolicy
import com.jaedhc.lume.domain.crypto.model.KeyPolicy
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine
import com.jaedhc.lume.domain.crypto.ports.CryptoKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier annotation class RealCrypto
@Qualifier annotation class NoOpCrypto

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides @Singleton
    fun provideCryptoKeyProvider(
        @ApplicationContext context: Context
    ): CryptoKeyProvider{
        return AndroidKeystoreKeySupplier(
            keyBits = 256,
            ivBytes = 12,
            tagBytes = 16
        )
    }

    // REAl AES Funcional
    @Provides @Singleton @RealCrypto
    fun provideAesGcmCryptoEngine(
        keyProvider: CryptoKeyProvider
    ): CryptoEngine = AesGcmSecurityCryptoEngine(keyProvider)

    // FAKER Para pruebas
    @Provides @Singleton @NoOpCrypto
    fun provideNoOpCryptoEngine(): CryptoEngine = NoOpCryptoEngine()

    @Provides @Singleton
    fun provideCryptoEngine(
        @RealCrypto real: CryptoEngine,
        @NoOpCrypto noOp: CryptoEngine
    ): CryptoEngine = if(BuildConfig.ENCRYPTION_ENABLED) real else noOp

    // --- KeyPolicy por dominio ---
    @Provides @Singleton @TxKeyPolicy
    fun provideTxKeyPolicy(): KeyPolicy = KeyPolicy(
        activeAlias = "aes_tx_v1",
        fallbackAliases = listOf("aes_tx_v0"),
        requireHardwareBacked = true,
        allowNoOp = false
    )

    @Provides @Singleton @MerchantKeyPolicy
    fun provideMerchantKeyPolicy(): KeyPolicy = KeyPolicy(
        activeAlias = "aes_merchant_v1",
        fallbackAliases = emptyList(),
        requireHardwareBacked = true,
        allowNoOp = false
    )

    @Provides @Singleton @AccountKeyPolicy
    fun provideAccountKeyPolicy(): KeyPolicy = KeyPolicy(
        activeAlias = "aes_account_v1",
        fallbackAliases = listOf("aes_account_v0"),
        requireHardwareBacked = true,
        allowNoOp = false
    )

    // --- HMAC ---
    @Provides @Singleton @Named("HmacKey")
    fun provideHmacKey(): SecretKey {
        // SOLO PARA DEV/POC: usa una clave fija. En prod, guárdala segura/rotada.
        val keyBytes = ByteArray(32) { 0x2A } // 32 bytes
        return SecretKeySpec(keyBytes, "HmacSHA256")
    }

    @Provides @Singleton
    fun provideHmacProvider(@Named("HmacKey") key: SecretKey): HmacTokenProvider =
        HmacTokenProvider(key)
}