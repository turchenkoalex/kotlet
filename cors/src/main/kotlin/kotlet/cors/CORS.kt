package kotlet.cors

import kotlet.InstallOrder
import kotlet.Interceptor
import kotlet.Routing
import java.time.Duration

private val DEFAULT_ALLOWED_HEADERS =
    listOf("Accept", "Authorization", "Accept-Language", "Content-Language", "Content-Type")

private val DEFAULT_MAX_AGE = Duration.ofMinutes(10)

/**
 * CORS related utilities
 */
object CORS {

    /**
     * Special value to allow all methods
     */
    const val ALL_ORIGINS = "*"

    /**
     * Special value to allow all methods
     */
    val ALL_METHODS = listOf("*")

    /**
     * CORS rules that allows all origins, all methods and all headers
     * with a max age of 10 minutes
     */
    val allowAll: CorsRules = allowOrigin(ALL_ORIGINS)

    /**
     * Create a new CORS rules that allows requests from the given origins
     * with all methods and with a max age of 10 minutes
     */
    fun allowOrigin(origin: String): CorsRules {
        val headers = CorsResponse.headers(
            allowOrigin = origin,
            allowMethods = ALL_METHODS,
            allowHeaders = DEFAULT_ALLOWED_HEADERS,
            maxAge = DEFAULT_MAX_AGE
        )
        return ConstantCorsRules(headers)
    }

    /**
     * Create a new CORS interceptor with the given [CorsRules] rules
     */
    fun interceptor(rules: CorsRules): Interceptor {
        return CorsInterceptor(rules)
    }
}

/**
 * Install cors rules to routing
 *
 * Example:
 *
 * ```
 * Kotlet.routing {
 *   installCORS(CORS.allowAll)
 *
 *   get("/hello", ::hello)
 * }
 * ```
 *
 * when OPTIONS request is sent to /hello, the response will contain the following headers:
 *  Access-Control-Allow-Origin: *
 *  Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
 *  Access-Control-Allow-Headers: Content-Type, Authorization
 *
 *  This will allow any origin to send requests to /hello
 *
 */
fun Routing.installCORS(
    /**
     * CORS rules to apply to the routing
     */
    rules: CorsRules,

    /**
     * Order of the interceptor in the chain
     */
    order: Int = InstallOrder.LAST,
) {
    install(CORS.interceptor(rules), order = order)
}
