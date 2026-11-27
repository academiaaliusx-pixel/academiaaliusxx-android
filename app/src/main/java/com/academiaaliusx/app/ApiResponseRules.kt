package com.academiaaliusx.app

import org.json.JSONObject

/** Normalizes error text from API responses without assuming a specific endpoint contract. */
object ApiResponseRules {
    fun message(body: JSONObject, status: Int): String {
        for (key in listOf("message", "error", "detail")) {
            val value = body.opt(key)
            if (value is String && value.isNotBlank()) return value.trim()
        }
        return when (status) {
            401 -> "Tu sesión ha caducado o necesitas iniciar sesión."
            403 -> "No tienes permiso para realizar esta acción."
            404 -> "No se encontró el recurso solicitado."
            408, 504 -> "La solicitud ha tardado demasiado. Inténtalo de nuevo."
            in 500..599 -> "El servidor no ha podido completar la solicitud. Inténtalo más tarde."
            in 400..499 -> "La solicitud no se pudo completar (HTTP $status)."
            else -> "No se pudo completar la solicitud."
        }
    }
}
