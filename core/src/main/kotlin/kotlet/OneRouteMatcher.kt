package kotlet

import jakarta.servlet.http.HttpServletRequest
import kotlet.RouteHelpers.RouteMatchResult
import kotlet.selector.Selector

internal data class OneRouteMatcher(val route: Route, val selectors: List<Selector>) {

    fun match(request: HttpServletRequest): RouteMatchResult {
        return RouteHelpers.matchRequestRouteSelectors(request, selectors)
    }

}
