package kotlet.jwt

import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import kotlet.http.Interceptor
import kotlet.http.Routing

object JWT {
    /**
     * Create a new JWT authentication interceptor
     * @param verifier JWT verifier
     * @param identityBuilder Identity builder
     */
    fun interceptor(verifier: JWTVerifier, identityBuilder: IdentityBuilder<*> = ::decodedJWTIdentityBuilder): Interceptor {
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
    verifier: JWTVerifier,
    identityBuilder: IdentityBuilder<*> = ::decodedJWTIdentityBuilder
) {
    val interceptor = JWT.interceptor(verifier, identityBuilder)
    install(interceptor)
}

/**
 * Install JWT authentication as routing interceptor, only for the specified routing block
 */
fun Routing.useJWTAuthentication(
    verifier: JWTVerifier,
    identityBuilder: IdentityBuilder<*> = ::decodedJWTIdentityBuilder,
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