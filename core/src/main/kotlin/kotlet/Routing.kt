package kotlet

import java.util.*

/**
 * Routing provides a way to configure routes and global interceptors
 * @see Kotlet.routing
 */
@KotletDsl
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
    private val globalInterceptors = mutableListOf<Interceptor>()

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
        order: InstallOrder = InstallOrder.LAST,
    ) {
        if (sealed) {
            throw RoutingConfigurationException("All routes have been sealed, you can't install global interceptors")
        }

        when (order) {
            InstallOrder.FIRST -> {
                interceptors.reversed().forEach(globalInterceptors::addFirst)
            }

            InstallOrder.LAST -> {
                globalInterceptors.addAll(interceptors)
            }
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
        } finally {
            repeat(interceptors.size) {
                currentInterceptors.removeLast()
            }
        }
    }

    /**
     * GET method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.GET, handler, settingsBlock)

    /**
     * POST method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.POST, handler, settingsBlock)

    /**
     * PUT method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.PUT, handler, settingsBlock)

    /**
     * PATCH method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.PATCH, handler, settingsBlock)

    /**
     * DELETE method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.DELETE, handler, settingsBlock)

    /**
     * HEAD method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.HEAD, handler, settingsBlock)

    /**
     * OPTIONS method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.OPTIONS, handler, settingsBlock)

    /**
     * TRACE method route
     *
     * @param path route path
     * @param handler route handler
     * @param settingsBlock route settings block
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
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.TRACE, handler, settingsBlock)

    /**
     * All routes configured in [block] will be nested under the specified [path]
     *
     * @param path route path
     * @param block routing block
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

        currentSegments.add(path)
        try {
            block(this)
        } finally {
            currentSegments.removeLast()
        }
    }

    private fun createRoute(
        path: String,
        method: HttpMethod,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit
    ) {
        if (sealed) {
            throw RoutingConfigurationException("All routes have been sealed, you can't create another one")
        }

        val routeSettingsBuilder = RouteSettings.RouteSettingsBuilder(currentInterceptors)
        routeSettingsBuilder.settingsBlock()
        val settings = routeSettingsBuilder.build()

        val routePath = buildRoutePath(currentSegments, path)

        if (routeHandlers.any { it.path == path && it.method == method }) {
            throw RoutingConfigurationException("Route $path has more than one handler for the same HTTP method: [$method]")
        }

        routeHandlers += RouteHandler(routePath, method, handler, settings)
    }

    internal fun getAllRoutes(): List<Route> {
        // seal all settings and return a copy of the list
        sealed = true

        val globalInterceptors = globalInterceptors.toList()

        val routes = routeHandlers.groupBy(RouteHandler::path).map { (path, handlers) ->
            RouteHandler.createRoute(
                path = path,
                globalInterceptors = globalInterceptors,
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
                    attributes = route.settings.attributes,
                )
            }
        }
}

private fun buildRoutePath(segments: Collection<String>, path: String): String {
    if (segments.isEmpty()) {
        return path
    }

    val segmentsPath = segments.joinToString("", transform = ::normalizePathSegment)
    if (path == RouteHelpers.ROOT_ROUTE_PATH || path.isEmpty()) {
        return segmentsPath
    }

    return segmentsPath + normalizePathSegment(path)
}

private fun normalizePathSegment(segment: String): String {
    return if (segment.startsWith(RouteHelpers.ROOT_ROUTE_PATH)) {
        segment
    } else {
        "/$segment"
    }
}
