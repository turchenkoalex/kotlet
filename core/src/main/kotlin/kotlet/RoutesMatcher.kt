package kotlet

import jakarta.servlet.http.HttpServletRequest

/**
 * Interface for matching routes based on the incoming HTTP request.
 */
interface RoutesMatcher {
    /**
     * Finds a route for the given request.
     *
     * @param request the HTTP request to match against the routes.
     * @returns a pair of the matched route and a map of parameters if a match is found, or null if no match is found.
     */
    fun findRoute(request: HttpServletRequest): Pair<Route, Map<String, String>>?

    companion object {
        /**
         * Creates a new instance of [RoutesMatcher] with the provided routes.
         *
         * @param routings the list of routes to match against.
         * @returns a new instance of [RoutesMatcher].
         */
        fun create(routings: List<Routing>): RoutesMatcher {
            val allRoutes = routings.map(Routing::getAllRoutes).flatten()
            return AllRoutesMatcher(allRoutes)
        }
    }
}
