package kotlet

import jakarta.servlet.http.HttpServletRequest
import kotlet.RouteHelpers.RouteMatchResult
import kotlet.selector.Selector

/**
 * Route matcher for a single route.
 */
internal data class OneRouteMatcher(val route: Route, val selectors: List<Selector>) {

    fun match(request: HttpServletRequest): RouteMatchResult {
        return RouteHelpers.matchRequestRouteSelectors(request, selectors)
    }

}
