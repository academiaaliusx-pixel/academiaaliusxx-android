package com.academiaaliusx.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerProfileRulesTest {
    @Test fun removesControlCharactersAndTrims() {
        assertEquals("Taylor", PlayerProfileRules.sanitizeName("  Taylor\u0000\n "))
    }

    @Test fun limitsNameLength() {
        assertEquals("123456789012345678901234", PlayerProfileRules.sanitizeName("123456789012345678901234567890"))
    }

    @Test fun blankDisplayNameFallsBack() {
        assertEquals("Jugador", PlayerProfileRules.displayName("  \n "))
    }

    @Test fun filtersUnknownGames() {
        assertEquals(setOf("Pokémon"), PlayerProfileRules.normalizeGames(setOf("Pokémon", "Unknown")))
    }

    @Test fun togglesKnownGameAndIgnoresUnknown() {
        assertEquals(setOf("Beyblade X", "Otros"), PlayerProfileRules.updateGame(setOf("Beyblade X"), "Otros", true))
        assertEquals(setOf("Beyblade X"), PlayerProfileRules.updateGame(setOf("Beyblade X", "Otros"), "Otros", false))
        assertEquals(setOf("Beyblade X"), PlayerProfileRules.updateGame(setOf("Beyblade X"), "Unknown", true))
    }
}
