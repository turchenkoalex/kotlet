package kotlet

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Servlet that handles all requests based on the provided routings.
 * It uses [AllRoutesMatcher] to find the route and [ErrorsHandler] to handle errors.
 * @property routingHandler Handler for routing requests.
 */
internal class RoutingServlet(
    private val routingHandler: RoutingHandler,
) : HttpServlet() {

    /**
     * Delegate the service method to the routing handler.
     */
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        routingHandler.service(request, response)
    }

    override fun toString(): String {
        return "Servlet(routingHandler=$routingHandler)"
    }
}
