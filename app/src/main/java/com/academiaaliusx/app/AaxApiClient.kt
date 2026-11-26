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
    suspend fun register(nick:String,email:String,password:String):ApiResult=request("POST","/api/register",JSONObject().put("nick",nick).put("email",email).put("password",password))\n    suspend fun forgotPassword(email:String):ApiResult=request("POST","/api/forgot-password",JSONObject().put("email",email))\n    suspend fun resendVerification(email:String):ApiResult=request("POST","/api/resend-verification",JSONObject().put("email",email))\n    suspend fun resetPassword(token:String,password:String):ApiResult=request("POST","/api/reset-password",JSONObject().put("token",token).put("password",password))\n    suspend fun uploadAvatar(data:String):ApiResult=request("POST","/api/avatar",JSONObject().put("data",data))\n    suspend fun removeAvatar():ApiResult=request("POST","/api/avatar",JSONObject().put("remove",true))\n    suspend fun changePassword(current:String,newPassword:String):ApiResult=request("POST","/api/password",JSONObject().put("current_password",current).put("password",newPassword))
    suspend fun logout():ApiResult=request("POST","/api/logout",JSONObject())
    suspend fun tournaments():ApiResult=request("GET","/api/tournaments",null)
    suspend fun joinTournament(id:String):ApiResult=request("POST","/api/join",JSONObject().put("id",id))
    suspend fun leaveTournament(id:String):ApiResult=request("POST","/api/leave",JSONObject().put("id",id))
    suspend fun profile(nick:String):ApiResult=request("GET","/api/profile?nick="+URLEncoder.encode(nick,"UTF-8"),null)

    suspend fun standings(game:String):ApiResult=request("GET","/api/standings?game="+URLEncoder.encode(game,"UTF-8"),null)
    suspend fun podiums():ApiResult=request("GET","/api/podiums",null)
    suspend fun socialSearch(q:String):ApiResult=request("GET","/api/social/search?q="+URLEncoder.encode(q,"UTF-8"),null)
    suspend fun friends():ApiResult=request("GET","/api/social/friends",null)
    suspend fun friendRequest(uid:String):ApiResult=request("POST","/api/social/friend-request",JSONObject().put("user_id",uid))
    suspend fun friendAccept(id:String):ApiResult=request("POST","/api/social/friend-accept",JSONObject().put("id",id))
    suspend fun friendAction(id:String,action:String):ApiResult=request("POST","/api/social/friend-action",JSONObject().put("id",id).put("action",action))
    suspend fun teams():ApiResult=request("GET","/api/social/teams",null)
    suspend fun myTeam():ApiResult=request("GET","/api/social/my-team",null)
    suspend fun blocks():ApiResult=request("GET","/api/social/blocks",null)
    suspend fun teamDetail(id:String):ApiResult=request("GET","/api/social/team-detail?id="+URLEncoder.encode(id,"UTF-8"),null)
    suspend fun teamCreate(payload:JSONObject):ApiResult=request("POST","/api/social/team-create",payload)
    suspend fun teamJoin(id:String,message:String=""):ApiResult=request("POST","/api/social/team-join",JSONObject().put("team_id",id).put("message",message))
    suspend fun teamManage(payload:JSONObject):ApiResult=request("POST","/api/social/team-manage",payload)
    suspend fun myTeamRequests():ApiResult=request("GET","/api/social/my-team-requests",null)
    suspend fun teamRequestCancel(id:String):ApiResult=request("POST","/api/social/team-request-cancel",JSONObject().put("request_id",id))
    suspend fun teamRequests():ApiResult=request("GET","/api/social/team-requests",null)
    suspend fun teamRespond(id:String,accept:Boolean):ApiResult=request("POST","/api/social/team-respond",JSONObject().put("request_id",id).put("accept",accept))
    suspend fun blockUser(id:String):ApiResult=request("POST","/api/social/block",JSONObject().put("user_id",id))
    suspend fun blockAction(id:String,action:String):ApiResult=request("POST","/api/social/block-action",JSONObject().put("user_id",id).put("action",action))
    suspend fun setAvatarFrame(frame:String):ApiResult=request("POST","/api/avatar-frame",JSONObject().put("frame",frame))
    suspend fun challongeTest():ApiResult=request("POST","/api/admin/challonge-test",JSONObject())
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
