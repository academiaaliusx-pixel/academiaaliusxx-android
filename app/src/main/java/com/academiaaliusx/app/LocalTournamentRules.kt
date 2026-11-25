package com.academiaaliusx.app

import java.lang.Character

data class LocalTournament(val id: String, val title: String)

object LocalTournamentRules {
    const val MAX_TITLE_LENGTH = 48

    fun sanitizeTitle(input: String): String =
        input.filterNot { Character.isISOControl(it) }.trim().take(MAX_TITLE_LENGTH)

    fun validTitle(input: String): Boolean = sanitizeTitle(input).isNotBlank()

    fun add(current: List<LocalTournament>, id: String, input: String): List<LocalTournament> {
        val title = sanitizeTitle(input)
        if (id.isBlank() || title.isBlank() || current.any { it.id == id }) return current
        return current + LocalTournament(id, title)
    }

    fun encodeEvents(events: List<LocalTournament>): List<String> =
        events.map { it.id + "\t" + sanitizeTitle(it.title) }.filter { it.substringAfter("\t").isNotBlank() }

    fun decodeEvents(records: Set<String>): List<LocalTournament> =
        records.mapNotNull { record ->
            val separator = record.indexOf('\t')
            if (separator <= 0 || separator == record.lastIndex) return@mapNotNull null
            val id = record.substring(0, separator).trim()
            val title = sanitizeTitle(record.substring(separator + 1))
            if (id.isBlank() || title.isBlank()) null else LocalTournament(id, title)
        }.distinctBy { it.id }
}
