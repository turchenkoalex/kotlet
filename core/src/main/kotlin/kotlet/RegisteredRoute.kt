package kotlet

import kotlet.attributes.RouteAttributes

/**
 * Represents a registered route in [kotlet.Routing].
 */
@ConsistentCopyVisibility
data class RegisteredRoute internal constructor(
    /**
     * The path of the route.
     */
    val path: String,

    /**
     * The HTTP method of the route.
     */
    val method: HttpMethod,

    /**
     * List of interceptors for the route.
     */
    val interceptors: List<Interceptor>,

    /**
     * The attributes of the registered route.
     */
    val attributes: RouteAttributes,
)
