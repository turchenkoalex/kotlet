package kotlet.http

import jakarta.servlet.http.HttpServlet

object Kotlet {
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
        val allRoutes = routings.map(Routing::getAllRoutes).flatten()
        val allRoutesMatcher = AllRoutesMatcher(allRoutes)

        return RoutingServlet(
            allRoutesMatcher = allRoutesMatcher,
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

