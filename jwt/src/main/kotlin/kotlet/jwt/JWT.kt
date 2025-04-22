package kotlet.jwt

import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import kotlet.InstallOrder
import kotlet.Interceptor
import kotlet.Routing

object JWT {
    /**
     * Create a new JWT authentication interceptor
     * @param verifier JWT verifier
     * @param identityBuilder Identity builder
     */
    fun interceptor(
        verifier: JWTVerifier,
        identityBuilder: IdentityBuilder<*> = ::decodedJWTIdentityBuilder
    ): Interceptor {
        return JWTAuthenticationInterceptor(verifier, identityBuilder)
    }
}

/**
 * Install JWT authentication as global routing interceptor
 * @param verifier JWT verifier
 * @param identityBuilder Identity builder
 *
 * Example:
 * ```
 * Kotlet.routing {
 *   installJWTAuthentication(verifier, identityBuilder = ::tokenToUser)
 * }
 *
 * fun tokenToUser(jwtToken: DecodedJWT): User {
 *  val expires = jwt.expiresAtAsInstant
 *  return User(..)
 * }
 *
 * fun httpCall(httpCall: HttpCall) {
 *  val user = httpCall.identity<User>()
 *
 *  if (user == null) {
 *    // Unauthorized
 *  }
 *
 *  if (user.isExpired()) {
 *    // Token expired
 *  }
 *
 *  call.respondText("Hello, ${user.name}")
 * }
 * ```
 */
fun Routing.installJWTAuthentication(
    /**
     * JWT verifier
     */
    verifier: JWTVerifier,

    /**
     * Identity builder.
     * This function will be called with the decoded JWT and should return the identity object.
     * Default function returns the decoded JWT as is.
     */
    identityBuilder: IdentityBuilder<*> = ::decodedJWTIdentityBuilder,
    /**
     * Order of the interceptor in the chain
     */
    order: InstallOrder = InstallOrder.LAST,
) {
    val interceptor = JWT.interceptor(verifier, identityBuilder)
    install(interceptor, order = order)
}

/**
 * Install JWT authentication as routing interceptor, only for the specified routing block
 */
fun Routing.useJWTAuthentication(
    /**
     * JWT verifier
     */
    verifier: JWTVerifier,

    /**
     * Identity builder.
     * This function will be called with the decoded JWT and should return the identity object.
     * Default function returns the decoded JWT as is.
     */
    identityBuilder: IdentityBuilder<*> = ::decodedJWTIdentityBuilder,

    /**
     * Routing block
     */
    block: Routing.() -> Unit
) {
    val interceptor = JWT.interceptor(verifier, identityBuilder)
    use(interceptor, block = block)
}

/**
 * Just a simple identity builder that returns the same decoded JWT
 */
private fun decodedJWTIdentityBuilder(jwt: DecodedJWT): DecodedJWT {
    return jwt
}
