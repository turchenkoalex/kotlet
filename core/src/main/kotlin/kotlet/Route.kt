package kotlet

/**
 * Handler for HTTP calls.
 */
typealias Handler = (call: HttpCall) -> Unit

/**
 * Route configuration.
 * Contains a route path and a list of handlers for different HTTP methods.
 */
internal data class Route(
    /**
     * Route path.
     */
    val path: String,

    /**
     * Link to the global interceptors.
     */
    private val globalInterceptors: List<Interceptor>,

    /**
     * Handlers for different HTTP methods.
     */
    private val handlers: Map<HttpMethod, Handler>,
) {
    /**
     * Handler with all global interceptors applied.
     */
    val handler = Interceptor.createRecursiveHandler(globalInterceptors, ::handleCall)

    /**
     * Handle the HTTP call.
     * Find the handler for the HTTP method and call it.
     * If the handler is not found, throw a [MethodNotAllowedException].
     *
     * @param httpCall HTTP call to handle.
     * @throws MethodNotAllowedException if the handler is not found.
     */
    private fun handleCall(httpCall: HttpCall) {
        val routeHandler = handlers[httpCall.httpMethod]
            ?: throw MethodNotAllowedException()

        routeHandler(httpCall)
    }

    companion object {
        fun createRoute(globalInterceptors: List<Interceptor>, handlers: List<RouteHandler>): Route {
            if (handlers.isEmpty()) {
                throw RoutingConfigurationException("Route must have at least one handler")
            }

            val path = handlers.first().path
            if (handlers.any { it.path != path }) {
                throw RoutingConfigurationException("All handlers must have the same path")
            }

            return Route(
                path = path,
                globalInterceptors = globalInterceptors,
                handlers = handlers.associate {
                    it.method to Interceptor.createRecursiveHandler(
                        it.settings.interceptors,
                        it.handler
                    )
                }
            )
        }
    }
}

/**
 * Settings for a route handler attached to a method.
 */
internal data class RouteHandler(
    val path: String,
    val method: HttpMethod,
    val handler: Handler,
    val settings: RouteSettings
)

/**
 * Settings for a route.
 */
class RouteSettings(
    internal val interceptors: List<Interceptor>,
) {
    class RouteSettingsBuilder(
        interceptors: List<Interceptor>
    ) {
        private val interceptors = interceptors.toMutableList()

        /**
         * Add an interceptor to the route.
         */
        fun withInterceptor(interceptor: Interceptor): RouteSettingsBuilder {
            interceptors.add(interceptor)
            return this
        }

        internal fun build(): RouteSettings {
            return RouteSettings(
                interceptors = interceptors,
            )
        }
    }
}
