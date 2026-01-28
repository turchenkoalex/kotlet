package kotlet

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.attributes.RouteAttributes
import java.io.InputStream

/**
 * This interface provides a way to interact with the HTTP request and response.
 */
interface HttpCall {
    /**
     * HTTP method of the request.
     */
    val httpMethod: HttpMethod

    /**
     * Route path of the request configured in the [Kotlet.routing].
     */
    val routePath: String

    /**
     * Raw jakarta [HttpServletRequest] request.
     */
    val rawRequest: HttpServletRequest

    /**
     * Raw jakarta [HttpServletResponse] response.
     */
    val rawResponse: HttpServletResponse

    /**
     * Parameters extracted from the route path.
     */
    val parameters: Map<String, String>

    /**
     * Attributes of the route.
     */
    val attributes: RouteAttributes

    /**
     * HTTP status code of the response.
     */
    var status: Int
        get() = rawResponse.status
        set(value) {
            rawResponse.status = value
        }

    /**
     * Respond with string content.
     * Writes the text to the response output stream.
     */
    fun respondText(text: String) {
        rawResponse.writer.write(text)
    }

    /**
     * Respond with bytes content.
     * Writes the bytes to the response output stream.
     * If the content type is not set, it will be set to "application/octet-stream".
     */
    fun respondBytes(data: ByteArray) {
        if (rawResponse.contentType == null) {
            rawResponse.contentType = "application/octet-stream"
        }

        rawResponse.outputStream.write(data)
    }

    /**
     * Respond with bytes content.
     * Writes the bytes from the input stream to the response output stream.
     * If the content type is not set, it will be set to "application/octet-stream".
     */
    fun respondBytes(inputStream: InputStream) {
        if (rawResponse.contentType == null) {
            rawResponse.contentType = "application/octet-stream"
        }

        inputStream.copyTo(rawResponse.outputStream)
    }

    /**
     * Respond with error status and message.
     */
    fun respondError(status: Int, message: String?) {
        rawResponse.sendError(status, message)
    }
}

/**
 * Internal implementation of [HttpCall].
 *
 * This is the concrete implementation used by the routing handler to wrap HTTP request/response
 * pairs along with extracted route parameters and attributes. It provides a clean abstraction
 * over the raw servlet API.
 *
 * @property httpMethod The HTTP method (GET, POST, etc.) of the request
 * @property routePath The configured route path pattern that matched this request
 * @property rawRequest The underlying Jakarta Servlet request
 * @property rawResponse The underlying Jakarta Servlet response
 * @property parameters Path parameters extracted from the route (e.g., {id} -> "123")
 * @property attributes Route-specific attributes configured for this path and method
 */
internal data class HttpCallImpl(
    override val httpMethod: HttpMethod,
    override val routePath: String,
    override val rawRequest: HttpServletRequest,
    override val rawResponse: HttpServletResponse,
    override val parameters: Map<String, String>,
    override val attributes: RouteAttributes,
) : HttpCall
