package kotlet

import java.util.*

/**
 * Routing provides a way to configure routes and global interceptors
 * @see Kotlet.routing
 */
@KotletDsl
@Suppress("TooManyFunctions")
class Routing internal constructor() {
    /**
     * Flag that indicates that all routes have been configured and sealed
     */
    private var sealed = false

    /**
     * List of all route handlers
     */
    private val routeHandlers = mutableListOf<RouteHandler>()

    /**
     * List of global interceptors
     */
    private val globalInterceptors = mutableListOf<InterceptorInstallation>()

    /**
     * Stack of interceptors for the injecting into the route
     * Used in [use] method
     */
    private val currentInterceptors = ArrayDeque<Interceptor>()

    /**
     * Stack of paths segments for the injecting into the route
     * Used in [route] method
     */
    private val currentSegments = ArrayDeque<String>()

    /**
     * Install global interceptors
     *
     * @param interceptors list of interceptors
     * @param order installation order (optional, default is [InstallOrder.LAST])
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   install(AuthInterceptor())
     *   get("/user") { call ->
     *     call.respondText("Hello, user!")
     *   }
     * }
     * ```
     */
    fun install(
        vararg interceptors: Interceptor,
        /**
         * Order of the interceptor in the chain
         */
        order: Int = InstallOrder.LAST,
    ) {
        if (sealed) {
            throw RoutingConfigurationException("All routes have been sealed, you can't install global interceptors")
        }

        interceptors.forEach { interceptor ->
            globalInterceptors.add(InterceptorInstallation(interceptor, order))
        }
    }

    /**
     * Use interceptors only for the specified routing block
     * @param interceptors list of interceptors
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *    use(AuthInterceptor()) {
     *      get("/user") { call ->
     *          call.respondText("Hello, user!")
     *      }
     *
     *      get("/admin") { call ->
     *          call.respondText("Hello, admin!")
     *      }
     *    }
     * }
     * ```
     */
    fun use(vararg interceptors: Interceptor, block: Routing.() -> Unit) {
        if (sealed) {
            throw RoutingConfigurationException("All routes have been sealed, you can't create another one")
        }

        currentInterceptors.addAll(interceptors)
        try {
            block(this)
        }
        finally {
            repeat(interceptors.size) {
                currentInterceptors.removeLast()
            }
        }
    }

    /**
     * GET method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   get { call ->
     *     call.respondText("Hello, user!")
     *   }
     * }
     * ```
     */
    fun get(
        handler: Handler,
    ) = get(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * GET method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   get("/users") { call ->
     *     call.respondText("Hello, user!")
     *   }
     * }
     * ```
     */
    fun get(
        path: String,
        handler: Handler,
    ) = get(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * GET method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   get("/users", routeOptions {}) { call ->
     *     call.respondText("Hello, user!")
     *   }
     * }
     * ```
     */
    fun get(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.GET, options, handler)


    /**
     * POST method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   post { call ->
     *     call.status = HttpServletResponse.SC_CREATED
     *   }
     * }
     * ```
     */
    fun post(
        handler: Handler,
    ) = post(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * POST method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   post("/users") { call ->
     *     call.status = HttpServletResponse.SC_CREATED
     *   }
     * }
     * ```
     */
    fun post(
        path: String,
        handler: Handler,
    ) = post(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * POST method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   post("/users", routeOptions {}) { call ->
     *     call.status = HttpServletResponse.SC_CREATED
     *   }
     * }
     * ```
     */
    fun post(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.POST, options, handler)

    /**
     * PUT method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   put { call ->
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun put(
        handler: Handler,
    ) = put(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * PUT method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   put("/users/{id}") { call ->
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun put(
        path: String,
        handler: Handler,
    ) = put(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * PUT method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   put("/users/{id}", routeOptions {}) { call ->
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun put(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.PUT, options, handler)

    /**
     * PATCH method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   patch { call ->
     *     call.status = HttpServletResponse.SC_CREATED
     *   }
     * }
     * ```
     */
    fun patch(
        handler: Handler,
    ) = patch(RouteHelpers.ROOT_ROUTE_PATH, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * PATCH method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   patch("/users/{id}") { call ->
     *     call.status = HttpServletResponse.SC_CREATED
     *   }
     * }
     * ```
     */
    fun patch(
        path: String,
        handler: Handler,
    ) = patch(path, RouteOptions.EMPTY_OPTIONS, handler)


    /**
     * PATCH method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   patch("/users/{id}", routeOptions {}) { call ->
     *     call.status = HttpServletResponse.SC_CREATED
     *   }
     * }
     * ```
     */
    fun patch(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.PATCH, options, handler)

    /**
     * DELETE method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   delete { call ->
     *     call.status = HttpServletResponse.SC_NO_CONTENT
     *   }
     * }
     * ```
     */
    fun delete(
        handler: Handler,
    ) = delete(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * DELETE method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   delete("/users/{id}") { call ->
     *     call.status = HttpServletResponse.SC_NO_CONTENT
     *   }
     * }
     * ```
     */
    fun delete(
        path: String,
        handler: Handler,
    ) = delete(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * DELETE method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   delete("/users/{id}", routeOptions {}) { call ->
     *     call.status = HttpServletResponse.SC_NO_CONTENT
     *   }
     * }
     * ```
     */
    fun delete(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.DELETE, options, handler)

    /**
     * HEAD method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   head { call ->
     *     call.status = HttpServletResponse.SC_NO_CONTENT
     *   }
     * }
     * ```
     */
    fun head(
        handler: Handler,
    ) = head(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * HEAD method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   head("/users/{id}") { call ->
     *     call.status = HttpServletResponse.SC_NO_CONTENT
     *   }
     * }
     * ```
     */
    fun head(
        path: String,
        handler: Handler,
    ) = head(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * HEAD method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   head("/users/{id}", routeOptions {}) { call ->
     *     call.status = HttpServletResponse.SC_NO_CONTENT
     *   }
     * }
     * ```
     */
    fun head(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.HEAD, options, handler)

    /**
     * OPTIONS method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   options { call ->
     *     // CORS processing
     *     call.rawResponse.setHeader("Access-Control-Allow-Origin", "*")
     *     call.rawResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
     *     call.rawResponse.setHeader("Access-Control-Allow-Headers", "Content-Type")
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun options(
        handler: Handler,
    ) = options(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * OPTIONS method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   options("/users") { call ->
     *     // CORS processing
     *     call.rawResponse.setHeader("Access-Control-Allow-Origin", "*")
     *     call.rawResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
     *     call.rawResponse.setHeader("Access-Control-Allow-Headers", "Content-Type")
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun options(
        path: String,
        handler: Handler,
    ) = options(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * OPTIONS method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   options("/users", routeOptions {}) { call ->
     *     // CORS processing
     *     call.rawResponse.setHeader("Access-Control-Allow-Origin", "*")
     *     call.rawResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
     *     call.rawResponse.setHeader("Access-Control-Allow-Headers", "Content-Type")
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun options(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.OPTIONS, options, handler)

    /**
     * TRACE method route
     *
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   trace { call ->
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun trace(
        handler: Handler,
    ) = trace(RouteHelpers.ROOT_ROUTE_PATH, handler)

    /**
     * TRACE method route
     *
     * @param path route path
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   trace("/users") { call ->
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun trace(
        path: String,
        handler: Handler,
    ) = trace(path, RouteOptions.EMPTY_OPTIONS, handler)

    /**
     * TRACE method route
     *
     * @param path route path
     * @param options route settings block
     * @param handler route handler
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *   trace("/users", routeOptions {}) { call ->
     *     call.status = HttpServletResponse.SC_OK
     *   }
     * }
     * ```
     */
    fun trace(
        path: String,
        options: RouteOptions,
        handler: Handler,
    ) = createRoute(path, HttpMethod.TRACE, options, handler)

    /**
     * All routes configured in [block] will be nested under the specified [path]
     *
     * @param path route path
     * @param block routing block
     * @throws RoutingConfigurationException if routing is sealed
     *
     * Example:
     * ```
     * Kotlet.routing {
     *
     *  route("/users") {
     *    get("/") { call ->
     *      call.respondText("Hello, user!")
     *    }
     *
     *    get("/{id}") { call ->
     *      val id = call.parameters["id"]
     *      call.respondText("Hello, user $id!")
     *    }
     *  }
     *
     *  route("/api") {
     *    route("/v1") {
     *      get("/posts") {
     *        call.respondText("Hello, posts v1!")
     *      }
     *    }
     *
     *    route("/v2") {
     *      get("/posts") {
     *        call.respondText("Hello, posts v2!")
     *      }
     *    }
     *  }
     */
    fun route(
        path: String,
        block: Routing.() -> Unit
    ) {
        if (sealed) {
            throw RoutingConfigurationException("All routes have been sealed, you can't create another one")
        }

        if (path.isEmpty() || path == RouteHelpers.ROOT_ROUTE_PATH) {
            throw RoutingConfigurationException("Route path can't be empty or equal to '/'")
        }

        currentSegments.add(path)
        try {
            block(this)
        }
        finally {
            currentSegments.removeLast()
        }
    }

    /**
     * Create a route with the specified [path], [method] and [options]
     */
    private fun createRoute(
        path: String,
        method: HttpMethod,
        options: RouteOptions,
        handler: Handler,
    ) {
        if (sealed) {
            throw RoutingConfigurationException("All routes have been sealed, you can't create another one")
        }

        val routePath = buildRoutePath(currentSegments, path)

        if (routeHandlers.any { it.path == routePath && it.method == method }) {
            throw RoutingConfigurationException(
                "Route $routePath has more than one handler for the same HTTP method: [$method]"
            )
        }

        val mergedOptions = options.merge(currentInterceptors)
        routeHandlers += RouteHandler(routePath, method, mergedOptions, handler)
    }

    /**
     * Combine all registered routes and return a list of them
     */
    internal fun getAllRoutes(): List<Route> {
        // seal all settings and return a copy of the list
        sealed = true

        val orderedGlobalInterceptors = globalInterceptors.toOrderedInterceptorList()

        val routes = routeHandlers.groupBy(RouteHandler::path).map { (path, handlers) ->
            RouteHandler.createRoute(
                path = path,
                globalInterceptors = orderedGlobalInterceptors,
                handlers = handlers,
            )
        }

        return routes
    }

    /**
     * Get all registered routes
     */
    val registeredRoutes: List<RegisteredRoute>
        get() {
            return routeHandlers.map { route ->
                RegisteredRoute(
                    path = route.path,
                    method = route.method,
                    interceptors = globalInterceptors.toOrderedInterceptorList() + route.options.interceptors,
                    attributes = route.options.attributes,
                )
            }
        }
}

private fun buildRoutePath(segments: Collection<String>, path: String): String {
    if (segments.isEmpty()) {
        return normalizePathSegment(path)
    }

    val segmentsPath = segments.joinToString("", transform = ::normalizePathSegment)
    if (path == RouteHelpers.ROOT_ROUTE_PATH || path.isEmpty()) {
        return segmentsPath
    }

    return segmentsPath + normalizePathSegment(path)
}

private fun normalizePathSegment(segment: String): String {
    val normalizedSegment = segment
        .replace("//", "/")
        .removeSuffix("/")

    return if (normalizedSegment.startsWith(RouteHelpers.ROOT_ROUTE_PATH)) {
        normalizedSegment
    } else {
        "/$normalizedSegment"
    }
}

private data class InterceptorInstallation(
    val interceptor: Interceptor,
    val order: Int,
)

private fun List<InterceptorInstallation>.toOrderedInterceptorList(): List<Interceptor> {
    return sortedBy { it.order }
        .map { it.interceptor }
        .toList()
}
