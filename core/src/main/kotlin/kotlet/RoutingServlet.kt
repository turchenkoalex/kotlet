package kotlet

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Servlet that handles all requests based on the provided routings.
 * It uses [AllRoutesMatcher] to find the route and [ErrorsHandler] to handle errors.
 * @property allRoutesMatcher Matcher that finds the route for the request.
 * @property errorsHandler Handler for errors that occur during request processing.
 */
internal class RoutingServlet(
    private val allRoutesMatcher: AllRoutesMatcher,
    private val errorsHandler: ErrorsHandler
) : HttpServlet() {
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        try {
            val (route, parameters) = allRoutesMatcher.findRoute(request)
                ?: return errorsHandler.routeNotFound(request, response)

            val httpMethod = HttpMethod.parse(request.method)
                ?: return errorsHandler.methodNotFound(request, response)

            val httpCall = HttpCallImpl(
                httpMethod = httpMethod,
                routePath = route.path,
                rawRequest = request,
                rawResponse = response,
                parameters = parameters
            )

            route.handler(httpCall)
        } catch (_: MethodNotFoundException) {
            errorsHandler.methodNotFound(request, response)
        } catch (expected: Throwable) {
            errorsHandler.internalServerError(request, response, expected)
        }
    }
}
