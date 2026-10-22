package kotlet

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.logging.Level
import java.util.logging.Logger

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
     * If a method is not found, the [RoutingServlet] will call the [methodNotFound] method.
     * By default, it will send a 405 error with the message "Method not allowed".
     */
    fun methodNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed")
    }

    /**
     * Handling internal server errors.
     *
     * If an internal server error occurs, the [RoutingServlet] will call the [internalServerError] method.
     * By default, it will send a 500 error with the message "Internal server error".
     */
    fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
        log.log(Level.SEVERE, "Internal server error during request processing: ${e.message}", e)
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error")
    }

    /**
     * Handling too large request body.
     *
     * If a handler throws [RequestBodyTooLargeException] (see [receiveText], [receiveBytes], [receiveLimitedStream]),
     * the [RoutingServlet] will call the [requestBodyTooLarge] method.
     * By default, it will send a 413 error with the message "Request body too large".
     */
    fun requestBodyTooLarge(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: RequestBodyTooLargeException,
    ) {
        response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request body too large")
    }

    companion object {
        private val log = Logger.getLogger(ErrorsHandler::class.qualifiedName)

        /**
         * Default implementation of [ErrorsHandler].
         */
        internal val DEFAULT = object : ErrorsHandler {
            override fun toString(): String {
                return "DefaultErrorsHandler"
            }
        }
    }
}
