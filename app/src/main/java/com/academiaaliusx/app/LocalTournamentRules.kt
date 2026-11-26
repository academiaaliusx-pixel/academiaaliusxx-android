package com.academiaaliusx.app

import java.lang.Character
import java.util.Locale

data class LocalTournament(val id: String, val title: String)

object LocalTournamentRules {
    const val MAX_TITLE_LENGTH = 48
    const val MAX_CREATED_TOURNAMENTS = 100
    private const val MAX_ID_LENGTH = 128

    // Preserve spaces while the user is typing; trim only when saving.
    fun cleanTitleInput(input: String): String =
        input.map { if (Character.isISOControl(it)) if (it == '\n' || it == '\r' || it == '\t') ' ' else null else it }
            .filterNotNull().joinToString("").take(MAX_TITLE_LENGTH)

    fun sanitizeTitle(input: String): String = cleanTitleInput(input).trim()

    fun validTitle(input: String): Boolean = sanitizeTitle(input).isNotBlank()

    fun add(current: List<LocalTournament>, id: String, input: String): List<LocalTournament> {
        val title = sanitizeTitle(input)
        if (id.isBlank() || id.length > MAX_ID_LENGTH || id != id.trim() || id.any { Character.isISOControl(it) } || title.isBlank() || current.size >= MAX_CREATED_TOURNAMENTS || current.any { it.id == id }) return current
        return current + LocalTournament(id, title)
    }

    // Edit only an existing event; invalid titles leave the original list untouched.
    fun edit(current: List<LocalTournament>, id: String, input: String): List<LocalTournament> {
        val title = sanitizeTitle(input)
        if (title.isBlank() || current.none { it.id == id }) return current
        return current.map { event -> if (event.id == id) event.copy(title = title) else event }
    }

    fun remove(current: List<LocalTournament>, id: String): List<LocalTournament> =
        current.filterNot { it.id == id }

    fun shareText(title: String, registered: Boolean, created: Boolean): String = buildString {
        append("Academia Alius X\n\n")
        append(sanitizeTitle(title)).append("\n")
        append(if (created) "Torneo local creado en este dispositivo" else "Evento de demostración").append("\n")
        append(if (registered) "Inscripción guardada localmente." else "Aún no tienes una inscripción local.").append("\n\n")
        append("Este evento no está publicado ni tiene inscripción online.")
    }

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

    fun sortByTitle(events: List<LocalTournament>, alphabetical: Boolean): List<LocalTournament> =
        if (alphabetical) events.sortedBy { it.title.lowercase(Locale.ROOT) } else events

    fun encodeEvents(events: List<LocalTournament>): List<String> =
        events.filter { it.id.isNotBlank() && it.id.length <= MAX_ID_LENGTH && it.id == it.id.trim() && it.id.none { ch -> Character.isISOControl(ch) } }.take(MAX_CREATED_TOURNAMENTS).map { it.id + "\t" + sanitizeTitle(it.title) }.filter { it.substringAfter("\t").isNotBlank() }

    fun decodeEvents(records: Set<String>): List<LocalTournament> =
        records.mapNotNull { record ->
            val separator = record.indexOf('\t')
            if (separator <= 0 || separator == record.lastIndex) return@mapNotNull null
            val id = record.substring(0, separator).trim()
            val title = sanitizeTitle(record.substring(separator + 1))
            if (id.isBlank() || id.length > MAX_ID_LENGTH || id != id.trim() || id.any { Character.isISOControl(it) } || title.isBlank()) null else LocalTournament(id, title)
        }.distinctBy { it.id }.take(MAX_CREATED_TOURNAMENTS)
}
