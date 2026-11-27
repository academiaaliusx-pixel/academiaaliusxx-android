package com.academiaaliusx.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiResponseRulesTest {
    @Test fun prefersServerMessageAndTrims() {
        assertEquals("Try again", ApiResponseRules.message(JSONObject().put("message", " Try again "), 500))
    }

    @Test fun fallsBackToErrorField() {
        assertEquals("Denied", ApiResponseRules.message(JSONObject().put("error", "Denied"), 400))
    }

    @Test fun handlesUnauthorized() {
        assertEquals("Tu sesión ha caducado o necesitas iniciar sesión.", ApiResponseRules.message(JSONObject(), 401))
    }

    @Test fun handlesServerFailure() {
        assertEquals("El servidor no ha podido completar la solicitud. Inténtalo más tarde.", ApiResponseRules.message(JSONObject(), 503))
    }

    @Test fun handlesUnknownClientStatus() {
        assertEquals("La solicitud no se pudo completar (HTTP 422).", ApiResponseRules.message(JSONObject(), 422))
    }

    @Test fun ignoresBlankServerMessages() {
        assertEquals("No se encontró el recurso solicitado.", ApiResponseRules.message(JSONObject().put("message", "  "), 404))
    }
}
