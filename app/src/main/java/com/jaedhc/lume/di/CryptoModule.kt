package com.jaedhc.lume.di

import android.content.Context
import com.jaedhc.lume.BuildConfig
import com.jaedhc.lume.data.crypto.AesGcmSecurityCryptoEngine
import com.jaedhc.lume.data.crypto.AndroidKeystoreKeySupplier
import com.jaedhc.lume.data.crypto.NoOpCryptoEngine
import com.jaedhc.lume.domain.crypto.ports.CryptoEngine
import com.jaedhc.lume.domain.crypto.ports.CryptoKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}