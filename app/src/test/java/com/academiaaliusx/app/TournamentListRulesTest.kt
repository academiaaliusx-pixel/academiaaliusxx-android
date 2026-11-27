package com.academiaaliusx.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentListRulesTest {
    private fun event(name: String, game: String, status: String, joined: Any = 0) =
        JSONObject().put("name", name).put("game", game).put("status", status).put("joined", joined)

    private val sample = listOf(
        event("Copa Nova", "Pokémon", "open", 1),
        event("Liga de invierno", "Beyblade X", "open", false),
        event("Final regional", "Inazuma Eleven", "finished", "true"),
        event("Copa de Bakugan", "Bakugan", "cancelled", 0)
    )

    @Test fun queryMatchesTitleCaseInsensitivelyAndTrims() {
        assertEquals(listOf("Copa Nova"), TournamentListRules.filter(sample, "  copa nOva ").map(TournamentListRules::title))
    }

    @Test fun queryMatchesGameAndStatus() {
        assertEquals(2, TournamentListRules.filter(sample, "open").size)
        assertEquals(listOf("Final regional"), TournamentListRules.filter(sample, "inazuma").map(TournamentListRules::title))
    }

    @Test fun openStatusIsCaseInsensitiveAndTrimmed() {
        assertTrue(TournamentListRules.isOpen(JSONObject().put("status", " OPEN ")))
        assertFalse(TournamentListRules.isOpen(JSONObject().put("status", "closed")))
    }

    @Test fun openFilterOnlyIncludesOpenEvents() {
        assertEquals(2, TournamentListRules.filter(sample, "", TournamentListFilter.OPEN).size)
        assertTrue(TournamentListRules.filter(sample, "", TournamentListFilter.OPEN).all { it.optString("status") == "open" })
    }

    @Test fun joinedFilterAcceptsBooleanNumericAndStringValues() {
        assertEquals(listOf("Copa Nova", "Final regional"), TournamentListRules.filter(sample, "", TournamentListFilter.JOINED).map(TournamentListRules::title))
        assertTrue(TournamentListRules.isJoined(JSONObject().put("joined", "yes")))
        assertFalse(TournamentListRules.isJoined(JSONObject().put("joined", 0)))
    }

    @Test fun queryAndFilterAreCombinedAndEmptyResultIsEmpty() {
        assertEquals(listOf("Copa Nova"), TournamentListRules.filter(sample, "pokémon", TournamentListFilter.JOINED).map(TournamentListRules::title))
        assertTrue(TournamentListRules.filter(sample, "missing", TournamentListFilter.ALL).isEmpty())
    }

    @Test fun titleFallsBackToTitleFieldAndThenLabel() {
        assertEquals("Alternative", TournamentListRules.title(JSONObject().put("title", "Alternative")))
        assertEquals("Torneo", TournamentListRules.title(JSONObject()))
    }
}
