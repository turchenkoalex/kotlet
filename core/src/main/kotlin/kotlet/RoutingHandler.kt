package kotlet

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.attributes.emptyRouteAttributes

/**
 * Functional interface for handling HTTP requests as a servlet.
 */
interface RoutingHandler {
    /**
     * Handles the HTTP request and response.
     */
    fun service(request: HttpServletRequest, response: HttpServletResponse)
}

/**
 * Default implementation of [RoutingHandler].
 * It uses [AllRoutesMatcher] to find the route and [ErrorsHandler] to handle errors.
 * @property routesMatcher Matcher that finds the route for the request.
 * @property errorsHandler Handler for errors that occur during request processing.
 */
internal class RoutingHandlerImpl(
    private val routesMatcher: RoutesMatcher,
    private val errorsHandler: ErrorsHandler
) : RoutingHandler {
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        try {
            val (route, parameters) = routesMatcher.findRoute(request)
                ?: return errorsHandler.routeNotFound(request, response)

            val httpMethod = HttpMethod.parse(request.method)
                ?: return errorsHandler.methodNotFound(request, response)

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
        } catch (_: MethodNotFoundException) {
            errorsHandler.methodNotFound(request, response)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw e
        } catch (expected: Exception) {
            errorsHandler.internalServerError(request, response, expected)
        }
    }

    override fun toString(): String {
        return "RoutingHandler(routesMatcher=$routesMatcher, errorsHandler=$errorsHandler)"
    }
}
