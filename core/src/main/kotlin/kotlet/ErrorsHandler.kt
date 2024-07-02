package kotlet

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Interface for handling errors
 */
interface ErrorsHandler {
    /**
     * Handling not found routes.
     *
     * If a route is not found, the [RoutingServlet] will call the [routeNotFound] method.
     * By default, it will send a 404 error with the message "Not found".
     */
    fun routeNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found")
    }

    /**
     * Handling method not allowed.
     *
     * If a method is not allowed, the [RoutingServlet] will call the [methodNotAllowed] method.
     * By default, it will send a 405 error with the message "Method not allowed".
     */
    fun methodNotAllowed(request: HttpServletRequest, response: HttpServletResponse) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed")
    }

    /**
     * Handling internal server errors.
     *
     * If an internal server error occurs, the [RoutingServlet] will call the [internalServerError] method.
     * By default, it will send a 500 error with the message "Internal server error".
     */
    fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Throwable) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error")
    }

    companion object {
        internal val DEFAULT = object : ErrorsHandler {}
    }
}