package com.academiaaliusx.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * First-stage native API client. Keeps the HttpOnly session cookie in an in-memory
 * CookieManager; it deliberately does not persist raw session credentials yet.
 */
object AaxApiClient {
    private const val BASE = "https://academiaaliusx.com"
    private val cookies = CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER)

    data class ApiResult(val ok: Boolean, val status: Int, val body: JSONObject)

    suspend fun me(): ApiResult = request("GET", "/api/me", null)

    suspend fun login(identifier: String, password: String): ApiResult =
        request("POST", "/api/login", JSONObject().put("id", identifier).put("password", password))

    suspend fun logout(): ApiResult = request("POST", "/api/logout", JSONObject())

    suspend fun register(nick: String, email: String, password: String): ApiResult =
        request("POST", "/api/register", JSONObject().put("nick", nick).put("email", email).put("password", password))

    private suspend fun request(method: String, path: String, payload: JSONObject?): ApiResult =
        withContext(Dispatchers.IO) {
            val connection = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = false
                cookies.cookieStore.cookies.forEach { setRequestProperty("Cookie", it.name + "=" + it.value) }
                if (method == "POST") {
                    setRequestProperty("Origin", BASE)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.use { it.write((payload ?: JSONObject()).toString().toByteArray(Charsets.UTF_8)) }
                }
            }
            try {
                cookies.put(URL(BASE + path).toURI(), connection.headerFields)
                val status = connection.responseCode
                val stream = if (status >= 400) connection.errorStream else connection.inputStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
                ApiResult(status in 200..299, status, try { JSONObject(text) } catch (_: Exception) { JSONObject().put("raw", text) })
            } finally { connection.disconnect() }
        }
}
