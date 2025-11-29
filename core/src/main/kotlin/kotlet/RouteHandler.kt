package kotlet

/**
 * Settings for a route handler attached to a method.
 * This is an intermediate class used to build a [Route] and holds the interceptors and attributes for the route.
 */
internal data class RouteHandler(
    val path: String,
    val method: HttpMethod,
    val context: RouteContext,
    val handler: Handler,
) {
    companion object {
        fun createRoute(path: String, globalInterceptors: List<Interceptor>, handlers: List<RouteHandler>): Route {
            if (handlers.isEmpty()) {
                throw RoutingConfigurationException("Route for path $path must have at least one handler")
            }

            if (handlers.any { it.path != path }) {
                throw RoutingConfigurationException("All handlers must have the same path $path")
            }

            return Route(
                path = path,
                globalInterceptors = globalInterceptors,
                handlers = handlers.associate {
                    it.method to Interceptor.createRecursiveHandler(
                        interceptors = it.context.interceptors(),
                        handler = it.handler,
                    )
                },
                attributes = handlers.associate {
                    it.method to it.context.attributes()
                }
            )
        }
    }
}
