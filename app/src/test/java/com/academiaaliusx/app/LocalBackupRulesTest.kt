package com.academiaaliusx.app

import org.junit.Assert.assertTrue
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
}
