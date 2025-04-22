package kotlet.cors

import kotlet.InstallOrder
import kotlet.Interceptor
import kotlet.Routing

private const val ALLOW_ALL_ORIGINS = "*"

private val DEFAULT_ALLOWED_HEADERS =
    listOf("Accept", "Authorization", "Accept-Language", "Content-Language", "Content-Type")

private val ALLOW_ALL_METHODS = listOf("*")


object CORS {
    /**
     * CORS rules that allows all origins, all methods and all headers
     */
    val allowAll: CorsRules = run {
        val headers = CorsResponse.headers(
            allowOrigin = ALLOW_ALL_ORIGINS,
            allowMethods = ALLOW_ALL_METHODS,
            allowHeaders = DEFAULT_ALLOWED_HEADERS,
        )
        ConstantCorsRules(headers)
    }

    /**
     * Create a new CORS rules that allows requests from the given origins
     */
    fun allowOrigin(origin: String): CorsRules {
        val headers = CorsResponse.headers(
            allowOrigin = origin,
            allowMethods = ALLOW_ALL_METHODS,
            allowHeaders = DEFAULT_ALLOWED_HEADERS,
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
