package com.jaedhc.lume.data.mapper

import android.util.Base64
import com.jaedhc.lume.domain.crypto.model.CipherText
import org.json.JSONObject

object CipherTextSerializer {

    fun serialize(ct: CipherText): String {
        val json = JSONObject()
        json.put("iv", Base64.encodeToString(ct.iv, Base64.NO_WRAP))
        json.put("payload", Base64.encodeToString(ct.payload, Base64.NO_WRAP))
        json.put("tag", ct.tag?.let { Base64.encodeToString(it, Base64.NO_WRAP) })
        json.put("keyAlias", ct.keyAlias)
        return json.toString()
    }

    fun deserialize(s: String): CipherText {
        val obj = JSONObject(s)
        val iv = Base64.decode(obj.getString("iv"), Base64.NO_WRAP)
        val payload = Base64.decode(obj.getString("payload"), Base64.NO_WRAP)
        val tag = obj.optString("tag", null)?.let { Base64.decode(it, Base64.NO_WRAP) }
        val alias = obj.getString("keyAlias")
        return CipherText(
            iv = iv,
            payload = payload,
            tag = tag,
            aad = null,
            keyAlias = alias
        )
    }
}
