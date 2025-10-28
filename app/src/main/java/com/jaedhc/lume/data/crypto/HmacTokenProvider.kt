package com.jaedhc.lume.data.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class HmacTokenProvider @Inject constructor (
    private val secretKey: SecretKey
){
    private val mac: Mac

    init {
        val keySpec = SecretKeySpec(secretKey.toString().toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac = Mac.getInstance("HmacSHA256")
        mac.init(keySpec)
    }

    fun token(value: String): String {
        val bytes = mac.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}