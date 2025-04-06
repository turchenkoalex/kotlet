package kotlet

import jakarta.servlet.http.HttpServletRequest
import kotlet.RouteHelpers.RouteMatchResult
import kotlet.selector.OptionalParamSegmentSelector
import kotlet.selector.ParamSegmentSelector
import kotlet.selector.Selector
import kotlet.selector.StaticSegmentSelector
import kotlet.selector.TailSegmentSelector
import kotlet.selector.WildcardSegmentSelector
import java.util.logging.Level
import java.util.logging.Logger

private val log = Logger.getLogger(RoutesMatcher::class.java.simpleName)

/**
 * Routes matcher for all given routes.
 */
class RoutesMatcher(routes: List<Route>) {

    /**
     * All routes but root router '/' (defined below)
     */
    private val matchers: List<OneRouteMatcher>

    /**
     * Special case for installed root router '/'
     * It is a very common behavior in the industry - the root router not only catches '/' routes, but all unmatched
     * routes too. So, I think we have to implement this common thing here too – if a root router has been defined,
     * the router has to catch all unmatched routes as well.
     */
    private val rootMatcher: OneRouteMatcher?

    init {
        val allMatchers = routes.map { route ->
            val routeSelectors = RouteHelpers.prepareSelectorsList(route)
            try {
                RouteHelpers.checkSelectorsList(route.path, routeSelectors)
            } catch (expected: IllegalArgumentException) {
                log.log(Level.WARNING, "Invalid selectors for route ${route.path}: ${expected.message}", expected)
                throw RoutingConfigurationException(expected.message ?: "")
            }
            OneRouteMatcher(route, routeSelectors)
        }

        RouteHelpers.checkAllRoutes(allMatchers)

        rootMatcher = allMatchers.find { it.route.path == RouteHelpers.ROOT_ROUTE_PATH }

        matchers = allMatchers
            .filterNot { it.route.path == RouteHelpers.ROOT_ROUTE_PATH }
            .sortedWith(oneRouteMatcherComparator)
    }

    /**
     * Finds a route for the given request.
     * This method iterates through all routes and tries to match the request with each route.
     *
     * @returns a pair of the matched route and a map of parameters if a match is found, or null if no match is found.
     */
    fun findRoute(request: HttpServletRequest): Pair<Route, Map<String, String>>? {
        val matchedRoute = matchers.firstNotNullOfOrNull { routeMatcher ->
            when (val matchResult = routeMatcher.match(request)) {
                RouteMatchResult.Failure -> null
                is RouteMatchResult.Match -> Pair(routeMatcher.route, matchResult.parameters)
            }
        }

        return if (matchedRoute == null && rootMatcher != null) {
            // route not found, but root router exists
            // ok, root router will "match" this request
            Pair(rootMatcher.route, emptyMap())
        } else {
            matchedRoute
        }
    }
}

private val oneRouteMatcherComparator = Comparator<OneRouteMatcher> { first, second ->
    val firstWeight = first.calculateTotalWeight()
    val secondWeight = second.calculateTotalWeight()

    return@Comparator firstWeight.compareTo(secondWeight)
}

private fun OneRouteMatcher.calculateTotalWeight(): Int {
    // the more segments are - the less weight is
    val baseWeight = 1_000_000_000 - this.selectors.sumOf { it.weight() }

    // however, tail route is the most heavy
    return if (this.isTailLast()) {
        baseWeight + 1_000_000_000
    } else {
        baseWeight
    }
}

private fun OneRouteMatcher.isTailLast(): Boolean {
    return this.selectors.isNotEmpty() && this.selectors.last() is TailSegmentSelector
}

private fun Selector.weight(): Int {
    return when (this) {
        is StaticSegmentSelector -> 1003
        is ParamSegmentSelector -> 1002
        is OptionalParamSegmentSelector -> 1001
        is WildcardSegmentSelector -> 1000
        is TailSegmentSelector -> 0
        else -> error("Uncovered branch $this")
    }
}
