package auth

import java.util.*

enum class Scope {
    READ_POSTS,
    WRITE_POSTS,
    DELETE_POSTS;

    companion object {
        private val MAP = entries.associateBy { it.name.uppercase() }

        fun parseSet(value: String): Set<Scope> {
            val scopes = value.split(" ").mapNotNull {
                MAP[it.uppercase()]
            }
            if (scopes.isEmpty()) {
                return emptySet()
            }
            return EnumSet.copyOf(scopes)
        }
    }
}
