package kotlet.http

import java.util.LinkedList
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class Routing internal constructor() {
    private val sealed = AtomicBoolean(false)
    private val routeHandlers = CopyOnWriteArrayList<RouteHandler>()
    private val globalInterceptors = CopyOnWriteArrayList<Interceptor>()
    private val currentInterceptors = LinkedList<Interceptor>()

    /**
     * Install global interceptors
     *
     * @param interceptors list of interceptors
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
    fun install(vararg interceptors: Interceptor) {
        if (sealed.get()) {
            throw RoutingConfigurationException("All routes have been sealed, you can't install global interceptors")
        }

        globalInterceptors.addAll(interceptors)
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
        if (sealed.get()) {
            throw RoutingConfigurationException("All routes have been sealed, you can't create another one")
        }

        currentInterceptors.addAll(interceptors)
        block(this)
        repeat(interceptors.size) {
            currentInterceptors.removeLast()
        }
    }

    fun get(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.GET, handler, settingsBlock)

    fun post(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.POST, handler, settingsBlock)

    fun put(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.PUT, handler, settingsBlock)

    fun patch(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.PATCH, handler, settingsBlock)

    fun delete(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.DELETE, handler, settingsBlock)

    fun head(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.HEAD, handler, settingsBlock)

    fun options(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.OPTIONS, handler, settingsBlock)

    fun trace(
        path: String,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit = {}
    ) = createRoute(path, HttpMethod.TRACE, handler, settingsBlock)

    private fun createRoute(
        path: String,
        method: HttpMethod,
        handler: Handler,
        settingsBlock: RouteSettings.RouteSettingsBuilder.() -> Unit
    ) {
        if (sealed.get()) {
            throw RoutingConfigurationException("All routes have been sealed, you can't create another one")
        }

        val routeSettingsBuilder = RouteSettings.RouteSettingsBuilder(currentInterceptors)
        routeSettingsBuilder.settingsBlock()
        val settings = routeSettingsBuilder.build()

        routeHandlers += RouteHandler(path, method, handler, settings)
    }

    internal fun getAllRoutes(): List<Route> {
        // seal all settings and return a copy of the list
        sealed.set(true)

        val globalInterceptors = globalInterceptors.toList()

        val routes = routeHandlers.groupBy(RouteHandler::path).map { (_, handlers) ->
            Route.createRoute(globalInterceptors, handlers)
        }

        return routes
    }
}
