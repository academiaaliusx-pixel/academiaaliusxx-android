package com.academiaaliusx.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpCookie

/** Bounds and validates cookies before they are encrypted and persisted. */
object SessionCookieRules {
    const val MAX_COOKIE_COUNT = 50
    const val MAX_COOKIE_JSON_LENGTH = 64 * 1024
    private const val MAX_NAME_LENGTH = 256
    private const val MAX_VALUE_LENGTH = 4096
    private const val MAX_PATH_LENGTH = 2048
    private const val MAX_MAX_AGE = 10L * 365 * 24 * 60 * 60

    fun encode(cookies: List<HttpCookie>, host: String): String {
        val normalizedHost = host.lowercase()
        val array = JSONArray()
        cookies.asSequence().filter { validCookie(it, normalizedHost) }.take(MAX_COOKIE_COUNT).forEach { cookie ->
            array.put(JSONObject()
                .put("name", cookie.name).put("value", cookie.value)
                .put("domain", (cookie.domain ?: normalizedHost).lowercase().trimStart('.'))
                .put("path", cookie.path ?: "/").put("secure", cookie.secure)
                .put("httpOnly", cookie.isHttpOnly).put("maxAge", cookie.maxAge).put("version", cookie.version))
        }
        val encoded = array.toString()
        require(encoded.length <= MAX_COOKIE_JSON_LENGTH) { "Cookie data exceeds the persistence limit." }
        return encoded
    }

    fun decode(encoded: String, host: String): List<HttpCookie> {
        if (encoded.length > MAX_COOKIE_JSON_LENGTH) return emptyList()
        val normalizedHost = host.lowercase()
        return try {
            val array = JSONArray(encoded)
            if (array.length() > MAX_COOKIE_COUNT) return emptyList()
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val name=item.optString("name");val value=item.optString("value")
                val domain=item.optString("domain",normalizedHost).lowercase().trimStart('.')
                val path=item.optString("path","/");val maxAge=item.optLong("maxAge",-1L)
                if(name.isBlank()||name.length>MAX_NAME_LENGTH||value.length>MAX_VALUE_LENGTH||
                    domain!=normalizedHost||path.isBlank()||path.length>MAX_PATH_LENGTH||!path.startsWith("/")||
                    maxAge < -1L||maxAge>MAX_MAX_AGE)return@mapNotNull null
                val cookie=try{HttpCookie(name,value)}catch(_:IllegalArgumentException){return@mapNotNull null}
                cookie.domain=domain;cookie.path=path;cookie.secure=item.optBoolean("secure",false)
                cookie.isHttpOnly=item.optBoolean("httpOnly",false);cookie.maxAge=maxAge
                cookie.version=item.optInt("version",0).coerceIn(0,1)
                if(cookie.hasExpired())null else cookie
            }
        }catch(_:Exception){emptyList()}
    }

    private fun validCookie(cookie:HttpCookie,host:String):Boolean {
        val domain=(cookie.domain?:host).lowercase().trimStart('.');val path=cookie.path?: "/"
        return cookie.name.isNotBlank()&&cookie.name.length<=MAX_NAME_LENGTH&&cookie.value.length<=MAX_VALUE_LENGTH&&
            domain==host&&path.startsWith("/")&&path.length<=MAX_PATH_LENGTH&&cookie.maxAge>=-1L&&
            cookie.maxAge<=MAX_MAX_AGE&&!cookie.hasExpired()
    }
}
