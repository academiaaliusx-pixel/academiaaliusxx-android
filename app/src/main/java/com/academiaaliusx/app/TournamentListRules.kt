package com.academiaaliusx.app

import org.json.JSONObject
import java.util.Locale

enum class TournamentListFilter { ALL, OPEN, JOINED }

/** In-memory filtering only: it does not change server data or assume an API search contract. */
object TournamentListRules {
    fun title(item: JSONObject): String =
        item.optString("name").takeIf { it.isNotBlank() }
            ?: item.optString("title").takeIf { it.isNotBlank() }
            ?: "Torneo"

    fun game(item: JSONObject): String = item.optString("game")

    fun isJoined(item: JSONObject): Boolean {
        val value = item.opt("joined")
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() > 0
            else -> (value?.toString()?.trim()?.lowercase(Locale.ROOT) ?: "") in setOf("true", "1", "yes")
        }
    }

    fun filter(
        items: List<JSONObject>,
        query: String,
        filter: TournamentListFilter = TournamentListFilter.ALL
    ): List<JSONObject> {
        val normalized = query.trim().lowercase(Locale.ROOT)
        return items.filter { item ->
            val status = item.optString("status").trim().lowercase(Locale.ROOT)
            val matchesFilter = when (filter) {
                TournamentListFilter.ALL -> true
                TournamentListFilter.OPEN -> status == "open"
                TournamentListFilter.JOINED -> isJoined(item)
            }
            val matchesQuery = normalized.isBlank() ||
                listOf(title(item), game(item), status, item.optString("date"))
                    .any { it.lowercase(Locale.ROOT).contains(normalized) }
            matchesFilter && matchesQuery
        }
    }
}
