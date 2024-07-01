package auth

import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant

data class User(
    val expires: Instant?,
    val scopes: Set<Scope>
) {
    fun hasRequiredScopes(requiredScopes: Collection<Scope>): Boolean {
        return requiredScopes.all(scopes::contains)
    }

    fun isExpired(): Boolean {
        if (expires == null) {
            return false
        }

        return Instant.now() > expires
    }

    companion object {
        fun fromJWT(jwt: DecodedJWT): User? {
            val scopeClaim = jwt.getClaim("scope")
            if (scopeClaim.isNull || scopeClaim.isMissing) {
                return null
            }

            val scopeValue = scopeClaim.asString()
            val scopes = Scope.parseSet(scopeValue)

            val expires = jwt.expiresAtAsInstant

            return User(
                expires = expires,
                scopes = scopes
            )
        }
    }
}