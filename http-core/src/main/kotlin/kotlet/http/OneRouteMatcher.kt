package kotlet.http

import jakarta.servlet.http.HttpServletRequest
import kotlet.http.RouteHelpers.RouteMatchResult
import kotlet.http.selector.Selector

internal data class OneRouteMatcher(val route: Route, val selectors: List<Selector>) {

    fun match(request: HttpServletRequest): RouteMatchResult {
        return RouteHelpers.matchRequestRouteSelectors(request, selectors)
    }

}
