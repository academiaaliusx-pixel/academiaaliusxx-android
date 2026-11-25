package com.academiaaliusx.app

import java.lang.Character

data class LocalTournament(val id: String, val title: String)

object LocalTournamentRules {
    const val MAX_TITLE_LENGTH = 48

    // Preserve spaces while the user is typing; trim only when saving.
    fun cleanTitleInput(input: String): String =
        input.map { if (Character.isISOControl(it)) if (it == '\n' || it == '\r' || it == '\t') ' ' else null else it }
            .filterNotNull().joinToString("").take(MAX_TITLE_LENGTH)

    fun sanitizeTitle(input: String): String = cleanTitleInput(input).trim()

    fun validTitle(input: String): Boolean = sanitizeTitle(input).isNotBlank()

    fun add(current: List<LocalTournament>, id: String, input: String): List<LocalTournament> {
        val title = sanitizeTitle(input)
        if (id.isBlank() || title.isBlank() || current.any { it.id == id }) return current
        return current + LocalTournament(id, title)
    }

    fun remove(current: List<LocalTournament>, id: String): List<LocalTournament> =
        current.filterNot { it.id == id }

    fun visibleEvents(
        events: List<LocalTournament>,
        registrations: Set<String>,
        createdIds: Set<String>,
        query: String,
        mode: String
    ): List<LocalTournament> {
        val normalizedQuery = query.trim()
        return events.filter { event ->
            val matchesMode = when (mode) {
                "registered" -> event.id in registrations
                "created" -> event.id in createdIds
                else -> true
            }
            matchesMode && (normalizedQuery.isBlank() || event.title.contains(normalizedQuery, ignoreCase = true))
        }
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
