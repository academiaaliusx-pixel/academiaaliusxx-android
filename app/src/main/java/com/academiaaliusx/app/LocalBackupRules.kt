package com.academiaaliusx.app

object LocalBackupRules {
    fun json(name: String, games: Set<String>, registrations: Set<String>, tournaments: List<LocalTournament>): String {
        val safeName = PlayerProfileRules.displayName(name)
        return buildString {
            append("{\n  \"schemaVersion\": 1,\n  \"playerName\": \"").append(escape(safeName)).append("\",\n")
            append("  \"games\": ").append(array(games.sorted())).append(",\n")
            append("  \"registrations\": ").append(array(registrations.sorted())).append(",\n")
            append("  \"createdTournaments\": [")
            val sorted = tournaments.sortedBy { it.id }
            sorted.forEachIndexed { index, event ->
                if (index > 0) append(",")
                append("\n    { \"id\": \"").append(escape(event.id)).append("\", \"title\": \"").append(escape(LocalTournamentRules.sanitizeTitle(event.title))).append("\" }")
            }
            if (sorted.isNotEmpty()) append("\n  ")
            append("]\n}")
        }
    }
    private fun array(values: List<String>): String = values.joinToString(prefix = "[", postfix = "]") { value -> "\"" + escape(value) + "\"" }
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
