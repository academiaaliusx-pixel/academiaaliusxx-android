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

    @Test fun detailFieldIsUsedWhenNoMessageOrError() {
        assertEquals("Detailed", ApiResponseRules.message(JSONObject().put("detail", "Detailed"), 400))
    }

    @Test fun messageFieldHasPriorityOverOtherFields() {
        assertEquals("Primary", ApiResponseRules.message(JSONObject().put("message", "Primary").put("error", "Secondary").put("detail", "Tertiary"), 400))
    }

    @Test fun ignoresNonStringFields() {
        assertEquals("La solicitud no se pudo completar (HTTP 400).", ApiResponseRules.message(JSONObject().put("message", 42).put("error", false).put("detail", JSONObject()), 400))
    }

    @Test fun handlesRequestTimeout() {
        assertEquals("La solicitud ha tardado demasiado. Inténtalo de nuevo.", ApiResponseRules.message(JSONObject(), 408))
    }

    @Test fun handlesOtherStatuses() {
        assertEquals("No se pudo completar la solicitud.", ApiResponseRules.message(JSONObject(), 200))
    }
}
