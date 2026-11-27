package com.academiaaliusx.app

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.lang.Character

data class LocalBackupPayload(
    val playerName: String,
    val games: Set<String>,
    val registrations: Set<String>,
    val createdTournaments: List<LocalTournament>
)

data class LocalBackupRestorePreview(val playerName: String, val gamesCount: Int, val registrationsCount: Int, val createdTournamentsCount: Int)

object LocalBackupRules {
    fun preview(payload: LocalBackupPayload): LocalBackupRestorePreview = LocalBackupRestorePreview(payload.playerName, payload.games.size, payload.registrations.size, payload.createdTournaments.size)

    private const val MAX_BACKUP_BYTES = 256 * 1024
    private const val MAX_REGISTRATIONS = 2000
    private const val MAX_TOURNAMENTS = 100
    private const val MAX_ID_LENGTH = 128
    private val BUILT_IN_EVENT_IDS = setOf("bx-local", "pokemon-local", "ie-local")

    fun json(name: String, games: Set<String>, registrations: Set<String>, tournaments: List<LocalTournament>): String {
        val safeName = PlayerProfileRules.displayName(name)
        val safeTournaments = LocalTournamentRules.decodeEvents(LocalTournamentRules.encodeEvents(tournaments).toSet()).sortedBy { it.id }
        val validEventIds = BUILT_IN_EVENT_IDS + safeTournaments.map { it.id }
        return buildString {
            append("{\n  \"schemaVersion\": 1,\n  \"playerName\": \"").append(escape(safeName)).append("\",\n")
            val safeGames = PlayerProfileRules.normalizeGames(games).sorted()
            val safeRegistrations = registrations.asSequence()
                .filter { it.isNotBlank() && it.length <= MAX_ID_LENGTH && it == it.trim() && it.none { ch -> Character.isISOControl(ch) } }
                .filter { it in validEventIds }
                .distinct().sorted().take(MAX_REGISTRATIONS).toList()
            append("  \"games\": ").append(array(safeGames)).append(",\n")
            append("  \"registrations\": ").append(array(safeRegistrations)).append(",\n")
            append("  \"createdTournaments\": [")
            val sorted = safeTournaments
            sorted.forEachIndexed { index, event ->
                if (index > 0) append(",")
                append("\n    { \"id\": \"").append(escape(event.id)).append("\", \"title\": \"").append(escape(LocalTournamentRules.sanitizeTitle(event.title))).append("\" }")
            }
            if (sorted.isNotEmpty()) append("\n  ")
            append("]\n}")
        }
    }

    fun parse(raw: String): LocalBackupPayload {
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) { "La copia supera el tamaño máximo permitido." }
        val root = try { JsonParser.parseString(raw) } catch (_: Exception) {
            throw IllegalArgumentException("El archivo no contiene JSON válido.")
        }
        require(root.isJsonObject) { "El archivo debe contener un objeto JSON." }
        val obj = root.asJsonObject
        require(obj.get("schemaVersion")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asJsonPrimitive?.asString == "1") {
            "Versión de copia no compatible."
        }
        val playerName = obj.get("playerName")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
            ?: throw IllegalArgumentException("Falta el nombre del jugador.")
        require(playerName.length <= 128) { "El nombre del jugador es demasiado largo." }

        val games = readStringArray(obj, "games", 20).let { PlayerProfileRules.normalizeGames(it.toSet()) }
        val registrationList = readStringArray(obj, "registrations", MAX_REGISTRATIONS).map { rawId ->
            val id = rawId
            require(id.isNotBlank() && id == id.trim() && id.length <= MAX_ID_LENGTH && id.none { Character.isISOControl(it) }) {
                "Identificador de inscripción inválido."
            }
            id
        }
        require(registrationList.distinct().size == registrationList.size) { "La copia contiene inscripciones duplicadas." }
        val registrations = registrationList.toSet()

        val eventsElement = obj.get("createdTournaments")
        require(eventsElement != null && eventsElement.isJsonArray) { "Falta la lista de torneos creados." }
        val eventsArray = eventsElement.asJsonArray
        require(eventsArray.size() <= MAX_TOURNAMENTS) { "La copia contiene demasiados torneos." }
        val events = eventsArray.map { element ->
            require(element.isJsonObject) { "Un torneo de la copia tiene un formato incorrecto." }
            val event = element.asJsonObject
            val id = event.get("id")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
                ?: throw IllegalArgumentException("Falta el identificador de un torneo.")
            val title = event.get("title")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
                ?: throw IllegalArgumentException("Falta el título de un torneo.")
            require(id.isNotBlank() && id == id.trim() && id.length <= MAX_ID_LENGTH && id.none { Character.isISOControl(it) }) { "Identificador de torneo inválido." }
            require(LocalTournamentRules.validTitle(title)) { "El título de un torneo está vacío." }
            LocalTournament(id, LocalTournamentRules.sanitizeTitle(title))
        }
        require(events.map { it.id }.distinct().size == events.size) { "La copia contiene identificadores de torneo duplicados." }
        require(events.none { it.id in BUILT_IN_EVENT_IDS }) { "La copia contiene un identificador reservado para un torneo de muestra." }
        val validEventIds = BUILT_IN_EVENT_IDS + events.map { it.id }
        require(registrations.all { it in validEventIds }) { "La copia contiene inscripciones a eventos que no existen." }
        return LocalBackupPayload(PlayerProfileRules.displayName(playerName), games, registrations, events)
    }

    private fun readStringArray(obj: JsonObject, key: String, maxItems: Int): List<String> {
        val element = obj.get(key)
        require(element != null && element.isJsonArray) { "Falta la lista '$key'." }
        val array = element.asJsonArray
        require(array.size() <= maxItems) { "La lista '$key' es demasiado larga." }
        return array.map { value ->
            require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "La lista '$key' contiene un valor inválido." }
            value.asString
        }
    }

    private fun array(values: List<String>): String = values.joinToString(prefix = "[", postfix = "]") { value -> "\""+escape(value)+"\""}
    private fun escape(value: String): String = buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '\b' -> append("\\b")
                '\u000c' -> append("\\f")
                else -> if (char.code < 0x20) append("\\u").append(char.code.toString(16).padStart(4, '0')) else append(char)
            }
        }
    }
}
