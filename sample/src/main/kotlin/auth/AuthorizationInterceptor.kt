package auth

import jakarta.servlet.http.HttpServletResponse
import kotlet.Handler
import kotlet.HttpCall
import kotlet.Interceptor
import kotlet.Routing
import kotlet.jwt.identity

/**
 * Requires the user to have the specified scopes for the routes in the block.
 */
fun Routing.requiredScopes(vararg scopes: Scope, block: Routing.() -> Unit) {
    use(AuthorizationInterceptor(scopes.toList()), block = block)
}

/**
 * Interceptor that checks if the user has the required scopes.
 *
 * Uses the `identity` extension function to get the user from the call.
 */
internal class AuthorizationInterceptor(
    private val requiredScopes: Collection<Scope>
) : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        if (requiredScopes.isEmpty()) {
            next(call)
            return
        }

        val user = call.identity<User>()
        if (user == null) {
            call.status = HttpServletResponse.SC_UNAUTHORIZED
            call.respondText("Unauthorized")
            return
        }

        val allowed = !user.isExpired() && user.hasRequiredScopes(requiredScopes)
        if (!allowed) {
            call.status = HttpServletResponse.SC_FORBIDDEN
            call.respondText("Forbidden")
            return
        }

        next(call)
    }
}