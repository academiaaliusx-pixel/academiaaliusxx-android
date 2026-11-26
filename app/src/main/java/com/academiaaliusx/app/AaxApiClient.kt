package com.academiaaliusx.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

object AaxApiClient {
    private const val BASE = "https://academiaaliusx.com"
    private val cookies = CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER)
    data class ApiResult(val ok:Boolean,val status:Int,val body:JSONObject)

    suspend fun me():ApiResult=request("GET","/api/me",null)
    suspend fun login(identifier:String,password:String):ApiResult=request("POST","/api/login",JSONObject().put("id",identifier).put("password",password))
    suspend fun register(nick:String,email:String,password:String):ApiResult=request("POST","/api/register",JSONObject().put("nick",nick).put("email",email).put("password",password))
    suspend fun logout():ApiResult=request("POST","/api/logout",JSONObject())
    suspend fun tournaments():ApiResult=request("GET","/api/tournaments",null)
    suspend fun joinTournament(id:String):ApiResult=request("POST","/api/join",JSONObject().put("id",id))
    suspend fun leaveTournament(id:String):ApiResult=request("POST","/api/leave",JSONObject().put("id",id))
    suspend fun profile(nick:String):ApiResult=request("GET","/api/profile?nick="+URLEncoder.encode(nick,"UTF-8"),null)

    private suspend fun request(method:String,path:String,payload:JSONObject?):ApiResult=withContext(Dispatchers.IO){
        val url=URL(BASE+path)
        val connection=(url.openConnection() as HttpURLConnection).apply{
            requestMethod=method;connectTimeout=15000;readTimeout=15000;instanceFollowRedirects=false
            val cookieHeader=cookies.getCookieStore().getCookies().joinToString("; "){it.name+"="+it.value}
            if(cookieHeader.isNotBlank())setRequestProperty("Cookie",cookieHeader)
            if(method=="POST"){setRequestProperty("Origin",BASE);setRequestProperty("Content-Type","application/json");doOutput=true}
        }
        try{
            if(method=="POST")connection.outputStream.use{it.write((payload?:JSONObject()).toString().toByteArray(Charsets.UTF_8))}
            val status=connection.responseCode
            cookies.put(url.toURI(),connection.headerFields)
            val stream=if(status>=400)connection.errorStream else connection.inputStream
            val raw=stream?.bufferedReader(Charsets.UTF_8)?.use{it.readText()}?:"{}"
            ApiResult(status in 200..299,status,try{JSONObject(raw)}catch(_:Exception){JSONObject().put("raw",raw)})
        }finally{connection.disconnect()}
    }
}
