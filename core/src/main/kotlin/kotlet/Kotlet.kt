package kotlet

import jakarta.servlet.http.HttpServlet

/**
 * Kotlet: A blend of Kotlin and Servlet with simple routing.
 * It provides a simple and intuitive API for creating HTTP restful services.
 */
object Kotlet {
    /**
     * Creates a new [HttpServlet] that will handle all requests based on the provided routings.
     */
    fun servlet(
        /**
         * Routing in the application.
         */
        vararg routing: Routing,

        /**
         * Handler for errors that occur during request processing.
         */
        errorsHandler: ErrorsHandler? = null,
    ): HttpServlet {
        return servlet(
            routings = routing.toList(),
            errorsHandler = errorsHandler
        )
    }

    /**
     * Creates a new [HttpServlet] that will handle all requests based on the provided routings.
     */
    fun servlet(
        /**
         * List of all routings in the application.
         */
        routings: List<Routing>,

        /**
         * Handler for errors that occur during request processing.
         */
        errorsHandler: ErrorsHandler? = null,
    ): HttpServlet {
        return RoutingServlet(
            routingHandler = handler(
                routings = routings,
                errorsHandler = errorsHandler
            )
        )
    }

    /**
     * Creates a new [RoutingHandler] that will handle all requests based on the provided routings.
     */
    fun handler(
        /**
         * Routing in the application.
         */
        vararg routing: Routing,

        /**
         * Handler for errors that occur during request processing.
         */
        errorsHandler: ErrorsHandler? = null,
    ): RoutingHandler {
        return handler(
            routings = routing.toList(),
            errorsHandler = errorsHandler
        )
    }

    /**
     * Creates a new [RoutingHandler] that will handle all requests based on the provided routings.
     */
    fun handler(
        /**
         * List of all routings in the application.
         */
        routings: List<Routing>,

        /**
         * Handler for errors that occur during request processing.
         */
        errorsHandler: ErrorsHandler? = null
    ): RoutingHandler {
        val routesMatcher = RoutesMatcher.create(routings)

        return RoutingHandlerImpl(
            routesMatcher = routesMatcher,
            errorsHandler = errorsHandler ?: ErrorsHandler.DEFAULT
        )
    }

    /**
     * Create a routing and set up routes
     *
     * Example:
     * ```
     * val routing = Kotlet.routing {
     *   get("/users") { call ->
     *     call.respondText("Hello, user!")
     *   }
     *
     *   get("/users/{id}") { call ->
     *     val id = call.parameters["id"]
     *     call.respondText("Hello, user $id!")
     *   }
     *
     *    post("/users") { call ->
     *      call.respondText("User created!")
     *    }
     *
     *    put("/users/{id}") { call ->
     *      val id = call.parameters["id"]
     *      call.respondText("User updated $id!")
     *    }
     *
     *    delete("/users/{id}") { call ->
     *      val id = call.parameters["id"]
     *      call.respondText("User deleted $id!")
     *    }
     *  }
     * ```
     */
    fun routing(init: Routing.() -> Unit): Routing {
        val mapping = Routing()
        mapping.init()
        return mapping
    }
}

