package kotlet

import jakarta.servlet.http.HttpServletRequest

/**
 * Routes matcher.
 * This interface is used to find a route for a given request.
 */
interface RoutesMatcher {
    /**
     * Finds a route for the given request.
     * This method iterates through all routes and tries to match the request with each route.
     *
     * @returns a pair of the matched route and a map of parameters if a match is found, or null if no match is found.
     */
    fun findRoute(request: HttpServletRequest): Pair<Route, Map<String, String>>?

    companion object {
        fun build(routes: List<Route>): RoutesMatcher {
            return AllRoutesMatcher(routes)
        }
    }
}
