package com.ao3reader.domain.models

/**
 * AO3 content ratings.
 */
enum class Rating(val displayName: String) {
    NOT_RATED("Not Rated"),
    GENERAL("General Audiences"),
    TEEN("Teen And Up Audiences"),
    MATURE("Mature"),
    EXPLICIT("Explicit");

    companion object {
        fun fromString(value: String): Rating {
            return when {
                value.contains("Not Rated", ignoreCase = true) -> NOT_RATED
                value.contains("General", ignoreCase = true) -> GENERAL
                value.contains("Teen", ignoreCase = true) -> TEEN
                value.contains("Mature", ignoreCase = true) -> MATURE
                value.contains("Explicit", ignoreCase = true) -> EXPLICIT
                else -> NOT_RATED
            }
        }
    }
}
