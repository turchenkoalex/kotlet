package kotlet.http

import jakarta.servlet.http.HttpServletRequest
import kotlet.http.RouteHelpers.RouteMatchResult
import kotlet.http.selector.*
import java.util.logging.Level
import java.util.logging.Logger

private val log = Logger.getLogger(AllRoutesMatcher::class.java.simpleName)

internal class AllRoutesMatcher(routes: List<Route>) {

    /**
     * All routes but root router '/' (defined below)
     */
    private val matchers: List<OneRouteMatcher>

    /**
     * Special case for installed root router '/'
     * It is a very common behavior in the industry - the root router not only catches '/' routes, but all unmatched routes too
     * So, I think we have to implement this common thing here too – if a root router has been defined, the router
     * has to catch all unmatched routes as well.
     */
    private val rootMatcher: OneRouteMatcher?

    init {
        val allMatchers = routes.map { route ->
            val routeSelectors = RouteHelpers.prepareSelectorsList(route)
            @Suppress("SwallowedException")
            try {
                RouteHelpers.checkSelectorsList(route.path, routeSelectors)
            } catch (e: IllegalArgumentException) {
                log.log(Level.WARNING, "Invalid selectors for route ${route.path}: ${e.message}", e)
                throw RoutingConfigurationException(e.message ?: "")
            }
            OneRouteMatcher(route, routeSelectors)
        }

        RouteHelpers.checkAllRoutes(allMatchers)

        rootMatcher = allMatchers.find { it.route.path == RouteHelpers.ROOT_ROUTE_PATH }

        matchers = allMatchers
            .filterNot { it.route.path == RouteHelpers.ROOT_ROUTE_PATH }
            .sortedWith(oneRouteMatcherComparator)
    }

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
