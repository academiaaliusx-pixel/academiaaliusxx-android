package com.academiaaliusx.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LocalBackupRulesTest {
    @Test fun exportsAllLocalDataAndEscapesJsonStrings() {
        val json = LocalBackupRules.json("  Ana \"X\"  ", setOf("Pokémon", "Beyblade X"), setOf("b", "a"), listOf(LocalTournament("t1", "Liga\\Final")))
        assertTrue(json.contains("\"playerName\": \"Ana \\\"X\\\"\""))
        assertTrue(json.contains("\"games\": [\"Beyblade X\", \"Pokémon\"]"))
        assertTrue(json.contains("\"registrations\": [\"a\", \"b\"]"))
        assertTrue(json.contains("\"id\": \"t1\", \"title\": \"Liga\\\\Final\""))
        assertTrue(json.contains("\"schemaVersion\": 1"))
    }

    @Test fun roundTripsExportedBackupIntoValidatedPayload() {
        val original = LocalBackupRules.json(
            "Jugador Uno",
            setOf("Pokémon", "Beyblade X"),
            setOf("pokemon-local", "custom-1"),
            listOf(LocalTournament("custom-1", "Liga de prueba"))
        )
        val restored = LocalBackupRules.parse(original)
        assertEquals("Jugador Uno", restored.playerName)
        assertEquals(setOf("Pokémon", "Beyblade X"), restored.games)
        assertEquals(setOf("pokemon-local", "custom-1"), restored.registrations)
        assertEquals(listOf(LocalTournament("custom-1", "Liga de prueba")), restored.createdTournaments)
    }

    @Test fun rejectsUnsupportedSchemaAndDuplicateTournamentIds() {
        expectInvalid("""{"schemaVersion":2,"playerName":"A","games":[],"registrations":[],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":[],"createdTournaments":[{"id":"x","title":"Liga"},{"id":"x","title":"Otra"}]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":[],"createdTournaments":[{"id":"bad\tID","title":"Liga"}]}""")
    }

    @Test fun rejectsMalformedJsonAndOversizedInput() {
        expectInvalid("no es JSON")
        expectInvalid(" ".repeat(300 * 1024))
    }


    @Test fun rejectsUnsafeRegistrationIds() {
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":["bad\tID"],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":["   "],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":["xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"],"createdTournaments":[]}""")
    }

    private fun expectInvalid(value: String) {
        try {
            LocalBackupRules.parse(value)
            fail("Expected backup validation to reject input")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
