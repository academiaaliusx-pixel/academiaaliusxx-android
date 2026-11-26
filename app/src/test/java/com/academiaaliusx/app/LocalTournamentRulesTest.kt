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

    @Test fun editUpdatesOnlyTargetAndTrimsTitle() {
        val source = listOf(LocalTournament("a", "Liga"), LocalTournament("b", "Copa"))
        assertEquals(listOf(LocalTournament("a", "Liga nueva"), source[1]), LocalTournamentRules.edit(source, "a", " Liga nueva "))
        assertEquals(source, LocalTournamentRules.edit(source, "a", " \n "))
        assertEquals(source, LocalTournamentRules.edit(source, "missing", "Otra"))
    }

    @Test fun enforcesCreatedTournamentLimitAndSafeIds() {
        val full = (1..LocalTournamentRules.MAX_CREATED_TOURNAMENTS).map { LocalTournament("id-$it", "Evento $it") }
        assertEquals(full, LocalTournamentRules.add(full, "extra", "Otro"))
        assertEquals(emptyList<LocalTournament>(), LocalTournamentRules.add(emptyList(), "bad\tID", "Liga"))
        assertEquals(emptyList<LocalTournament>(), LocalTournamentRules.add(emptyList(), "x".repeat(129), "Liga"))
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

    @Test fun encodingRejectsUnsafeIdsAndCapsOutput() {
        val unsafe = listOf(LocalTournament("bad\tID", "Liga"), LocalTournament("x".repeat(129), "Copa"))
        assertEquals(emptyList<String>(), LocalTournamentRules.encodeEvents(unsafe))
        val excess = (1..110).map { LocalTournament("id-$it", "Evento $it") }
        assertEquals(LocalTournamentRules.MAX_CREATED_TOURNAMENTS, LocalTournamentRules.encodeEvents(excess).size)
    }

    @Test fun ignoresDuplicateIdsWhenDecoding() {
        assertEquals(listOf(LocalTournament("a", "Liga")), LocalTournamentRules.decodeEvents(setOf("a\tLiga", "a\tCopa")))
    }

    @Test fun shareTextClearlyLabelsLocalAndDemoEvents() {
        assertEquals("Academia Alius X\n\nLiga\nTorneo local creado en este dispositivo\nInscripción guardada localmente.\n\nEste evento no está publicado ni tiene inscripción online.", LocalTournamentRules.shareText(" Liga ", true, true))
        assertEquals("Academia Alius X\n\nCopa\nEvento de demostración\nAún no tienes una inscripción local.\n\nEste evento no está publicado ni tiene inscripción online.", LocalTournamentRules.shareText("Copa", false, false))
    }

    @Test fun filtersByRegistrationCreationAndSearch() {
        val events = listOf(LocalTournament("a", "Beyblade X"), LocalTournament("b", "Pokémon"), LocalTournament("c", "Liga del sábado"))
        val registered = setOf("b", "c")
        val created = setOf("c")
        assertEquals(listOf(events[1], events[2]), LocalTournamentRules.visibleEvents(events, registered, created, "", "registered"))
        assertEquals(listOf(events[2]), LocalTournamentRules.visibleEvents(events, registered, created, "", "created"))
        assertEquals(listOf(events[1]), LocalTournamentRules.visibleEvents(events, registered, created, "  pokémon ", "all"))
        assertEquals(emptyList<LocalTournament>(), LocalTournamentRules.visibleEvents(events, registered, created, "inexistente", "all"))
    }
}
