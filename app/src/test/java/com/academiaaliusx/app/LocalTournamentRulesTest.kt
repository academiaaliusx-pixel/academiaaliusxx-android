package com.academiaaliusx.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LocalTournamentRulesTest {
    @Test fun stripsControlsTrimsAndLimitsTitle() {
        assertEquals("Liga", LocalTournamentRules.sanitizeTitle("  Liga\u0000\n "))
        assertEquals(48, LocalTournamentRules.sanitizeTitle("x".repeat(80)).length)
    }

    @Test fun rejectsBlankTitle() {
        assertFalse(LocalTournamentRules.validTitle(" \n\u0000 "))
    }

    @Test fun addsOnlyValidUniqueIds() {
        val first = LocalTournamentRules.add(emptyList(), "a", "  Liga  ")
        assertEquals(listOf(LocalTournament("a", "Liga")), first)
        assertEquals(first, LocalTournamentRules.add(first, "a", "Otra"))
        assertEquals(first, LocalTournamentRules.add(first, "b", " \n "))
    }

    @Test fun encodesAndDecodesAndIgnoresCorruptRecords() {
        val source = listOf(LocalTournament("a", "Liga"), LocalTournament("b", "Copa"))
        val encoded = LocalTournamentRules.encodeEvents(source).toSet() + setOf("broken", "\tno-id", "c\t ")
        assertEquals(source, LocalTournamentRules.decodeEvents(encoded).sortedBy { it.id })
    }

    @Test fun ignoresDuplicateIdsWhenDecoding() {
        assertEquals(listOf(LocalTournament("a", "Liga")), LocalTournamentRules.decodeEvents(setOf("a\tLiga", "a\tCopa")))
    }
}
