package com.academiaaliusx.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LocalTournamentRulesTest {
    @Test fun stripsControlsTrimsAndLimitsTitle() {
        assertEquals("Liga", LocalTournamentRules.sanitizeTitle("  Liga\u0000\n "))
        assertEquals(48, LocalTournamentRules.sanitizeTitle("x".repeat(80)).length)
    }

    @Test fun preservesSpacesWhileTyping() {
        assertEquals("Liga del ", LocalTournamentRules.cleanTitleInput("Liga del "))
        assertEquals("Liga del sábado", LocalTournamentRules.sanitizeTitle("Liga del sábado "))
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

    @Test fun removesOnlyRequestedEvent() {
        val source = listOf(LocalTournament("a", "Liga"), LocalTournament("b", "Copa"))
        assertEquals(listOf(LocalTournament("b", "Copa")), LocalTournamentRules.remove(source, "a"))
        assertEquals(source, LocalTournamentRules.remove(source, "missing"))
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
