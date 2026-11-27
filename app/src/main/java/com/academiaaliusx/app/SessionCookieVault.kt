package com.academiaaliusx.app

import android.content.Context
import android.util.Base64
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.net.HttpCookie
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores API cookie snapshots as AES-GCM ciphertext protected by Android Keystore. */
class SessionCookieVault(context:Context) {
    private val preferences=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)

    @Synchronized fun save(cookies:List<HttpCookie>,host:String) {
        val plain=SessionCookieRules.encode(cookies,host)
        if(plain=="[]"||plain.isBlank()){preferences.edit().remove(ENTRY).commit();return}
        val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,getOrCreateKey())
        val payload=cipher.iv+cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        preferences.edit().putString(ENTRY,Base64.encodeToString(payload,Base64.NO_WRAP)).commit()
    }

    @Synchronized fun restore():List<HttpCookie> {
        val encoded=preferences.getString(ENTRY,null)?:return emptyList()
        return try {
            val payload=Base64.decode(encoded,Base64.NO_WRAP);require(payload.size>IV_LENGTH)
            val cipher=Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE,getOrCreateKey(),GCMParameterSpec(TAG_BITS,payload.copyOfRange(0,IV_LENGTH)))
            SessionCookieRules.decode(cipher.doFinal(payload.copyOfRange(IV_LENGTH,payload.size)).toString(Charsets.UTF_8),HOST)
        }catch(_:Exception){preferences.edit().remove(ENTRY).commit();emptyList()}
    }

    @Synchronized fun clear() {
        preferences.edit().remove(ENTRY).commit()
        try{KeyStore.getInstance(ANDROID_KEYSTORE).apply{load(null)}.deleteEntry(KEY_ALIAS)}catch(_:Exception){}
    }

    private fun getOrCreateKey():SecretKey {
        val store=KeyStore.getInstance(ANDROID_KEYSTORE).apply{load(null)}
        (store.getKey(KEY_ALIAS,null) as? SecretKey)?.let{return it}
        val generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,ANDROID_KEYSTORE)
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return generator.generateKey()
    }
    companion object {
        private const val PREFS="aax_secure_session";private const val ENTRY="encrypted_cookie_snapshot"
        private const val KEY_ALIAS="aax_api_session_aes_gcm";private const val ANDROID_KEYSTORE="AndroidKeyStore"
        private const val IV_LENGTH=12;private const val TAG_BITS=128;private const val HOST="academiaaliusx.com"
    }
}
