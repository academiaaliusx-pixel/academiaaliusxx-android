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
        expectInvalid("""{"schemaVersion":1.5,"playerName":"A","games":[],"registrations":[],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":1.0,"playerName":"A","games":[],"registrations":[],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":"1","playerName":"A","games":[],"registrations":[],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":[],"createdTournaments":[{"id":"x","title":"Liga"},{"id":"x","title":"Otra"}]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":[],"createdTournaments":[{"id":"bx-local","title":"Torneo duplicado"}]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":["pokemon-local","pokemon-local"],"createdTournaments":[]}""")
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
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":[" padded "],"createdTournaments":[]}""")
        expectInvalid("""{"schemaVersion":1,"playerName":"A","games":[],"registrations":[],"createdTournaments":[{"id":" padded ","title":"Liga"}]}""")
    }

    @Test fun exportLimitsAndSanitizesRecordsSoBackupRemainsRestorable() {
        val registrations = (1..2005).map { "reg-$it" }.toSet() + setOf("bad\tID", " ")
        val tournaments = (1..110).map { LocalTournament("id-$it", "Evento $it") } +
            listOf(LocalTournament("bad\tID", "Unsafe"), LocalTournament(" ", "Empty ID"))
        val restored = LocalBackupRules.parse(LocalBackupRules.json("Jugador", setOf("Pokémon", "No válido"), registrations, tournaments))
        assertEquals(2000, restored.registrations.size)
        assertTrue(restored.registrations.none { it.contains('\t') || it.isBlank() })
        assertEquals(LocalTournamentRules.MAX_CREATED_TOURNAMENTS, restored.createdTournaments.size)
        assertTrue(restored.createdTournaments.none { it.id.contains('\t') || it.id.isBlank() })
        assertEquals(setOf("Pokémon"), restored.games)
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
