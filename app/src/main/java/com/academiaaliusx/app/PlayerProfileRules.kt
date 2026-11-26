package com.academiaaliusx.app

import java.lang.Character

object PlayerProfileRules {
    const val DEFAULT_NAME = "Jugador"
    const val MAX_NAME_LENGTH = 24

    val availableGames = listOf("Beyblade X", "Pokémon", "Inazuma Eleven", "Otros")

    // Keep trailing spaces during editing; trim only when normalizing a saved/display name.
    fun cleanNameInput(input: String): String =
        input.filterNot { Character.isISOControl(it) }.take(MAX_NAME_LENGTH)

    fun sanitizeName(input: String): String = cleanNameInput(input).trim()

    fun displayName(input: String): String = sanitizeName(input).ifBlank { DEFAULT_NAME }

    fun normalizeGames(input: Set<String>): Set<String> =
        input.filter { it in availableGames }.toSet()

    fun updateGame(current: Set<String>, game: String, enabled: Boolean): Set<String> {
        if (game !in availableGames) return normalizeGames(current)
        val normalized = normalizeGames(current)
        return if (enabled) normalized + game else normalized - game
    }
}
