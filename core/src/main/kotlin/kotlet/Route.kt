package kotlet

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
     * If the handler is not found, throw a [MethodNotFoundException].
     *
     * @param httpCall HTTP call to handle.
     * @throws MethodNotFoundException if the handler is not found.
     */
    private fun handleCall(httpCall: HttpCall) {
        val routeHandler = handlers[httpCall.httpMethod]
            ?: throw MethodNotFoundException()

        routeHandler(httpCall)
    }
}
