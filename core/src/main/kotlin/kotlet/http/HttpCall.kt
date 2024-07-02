package kotlet.http

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.InputStream

/**
 * This interface provides a way to interact with the HTTP request and response.
 *
 * @author Vasily Vasilkov (vasily.vasilkov@lightspeedhq.com)
 */
interface HttpCall {
    /**
     * HTTP method of the request.
     */
    val httpMethod: HttpMethod

    /**
     * Route path of the request configured in the [routing].
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

internal data class HttpCallImpl(
    override val httpMethod: HttpMethod,
    override val routePath: String,
    override val rawRequest: HttpServletRequest,
    override val rawResponse: HttpServletResponse,
    override val parameters: Map<String, String>
) : HttpCall

