package kotlet

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.attributes.emptyRouteAttributes

/**
 * Servlet that handles all requests based on the provided routings.
 * It uses [AllRoutesMatcher] to find the route and [ErrorsHandler] to handle errors.
 * @property routesMatcher Matcher that finds the route for the request.
 * @property errorsHandler Handler for errors that occur during request processing.
 */
internal class RoutingServlet(
    private val routesMatcher: RoutesMatcher,
    private val errorsHandler: ErrorsHandler
) : HttpServlet() {
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        try {
            val (route, parameters) = routesMatcher.findRoute(request)
                ?: return errorsHandler.routeNotFound(request, response)

            val httpMethod = HttpMethod.parse(request.method)
                ?: return errorsHandler.methodNotFound(request, response)

            // httpMethod not allowed for this route
            if (!route.allowedHttpMethods.contains(httpMethod)) {
                errorsHandler.methodNotFound(request, response)
                return
            }

            val attributes = route.attributes[httpMethod] ?: emptyRouteAttributes()

            val httpCall = HttpCallImpl(
                httpMethod = httpMethod,
                routePath = route.path,
                rawRequest = request,
                rawResponse = response,
                parameters = parameters,
                attributes = attributes,
            )

            route.handler(httpCall)
        } catch (expected: Exception) {
            errorsHandler.internalServerError(request, response, expected)
        }
    }

    override fun toString(): String {
        return "RoutingServlet(routesMatcher=$routesMatcher, errorsHandler=$errorsHandler)"
    }
}
