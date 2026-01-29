package kotlet.client

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * HTTP client for sending requests and receiving responses.
 * @param httpClient The underlying HTTP client to use for sending requests.
 * @param serializer The serializer to use for request and response bodies.
 * @param options The client options to configure behavior.
 * @constructor Creates a new Client instance.
 */
class Client(
    private val httpClient: HttpClient,
    private val serializer: ClientSerializer,
    private val options: ClientOptions,
) : Closeable {

    /**
     * Default headers to include in every request.
     */
    private val defaultHeaders: Map<String, String>

    init {
        val headers = mutableMapOf(
            "Accept" to serializer.contentType
        )

        if (options.userAgent.isNotEmpty()) {
            headers["User-Agent"] = options.userAgent
        }

        if (options.allowGzipResponses) {
            headers[ACCEPT_ENCODING] = GZIP
        }

        if (options.additionalHeaders.isNotEmpty()) {
            headers.putAll(options.additionalHeaders)
        }

        defaultHeaders = headers.toMap()
    }


    /**
     * Sends a GET request to the specified URI and returns the response deserialized into the specified class.
     *
     * @param uri The URI to send the GET request to.
     * @param headers Optional headers to include in the request.
     * @return The response body deserialized into the specified class, or null if no body is present.
     */
    inline fun <reified TRes : Any> get(
        uri: URI,
        headers: Map<String, String> = emptyMap()
    ): TRes? {
        val request = buildRequest("GET", uri, headers, null, Unit::class.java)
        return send(request, TRes::class.java).body
    }

    /**
     * Sends a POST request to the specified URI with the given request body and
     * returns the response deserialized into the specified class.
     *
     * @param uri The URI to send the POST request to.
     * @param req The request body to send.
     * @param headers Optional headers to include in the request.
     * @return The response body deserialized into the specified class, or null if no body is present.
     */
    inline fun <reified TReq : Any, reified TRes : Any> post(
        uri: URI,
        req: TReq? = null,
        headers: Map<String, String> = emptyMap()
    ): TRes? {
        val request = buildRequest("POST", uri, headers, req, TReq::class.java)
        return send(request, TRes::class.java).body
    }

    /**
     * Sends a PUT request to the specified URI with the given request body
     * and returns the response deserialized into the specified class.
     *
     * @param uri The URI to send the PUT request to.
     * @param req The request body to send.
     * @param headers Optional headers to include in the request.
     * @return The response body deserialized into the specified class, or null if no body is present.
     */
    inline fun <reified TReq : Any, reified TRes : Any> put(
        uri: URI,
        req: TReq? = null,
        headers: Map<String, String> = emptyMap()
    ): TRes? {
        val request = buildRequest("PUT", uri, headers, req, TReq::class.java)
        return send(request, TRes::class.java).body
    }

    /**
     * Sends a DELETE request to the specified URI and returns the response deserialized into the specified class.
     *
     * @param uri The URI to send the DELETE request to.
     * @param headers Optional headers to include in the request.
     * @return The response body deserialized into the specified class, or null if no body is present.
     */
    inline fun <reified TRes : Any> delete(
        uri: URI,
        headers: Map<String, String> = emptyMap()
    ): TRes? {
        val request = buildRequest("DELETE", uri, headers, null, Unit::class.java)
        return send(request, TRes::class.java).body
    }

    /**
     * Sends the given request and returns the response deserialized into the specified class.
     *
     * @param request The request to send.
     * @param clazz The class to deserialize the response body into.
     * @return The response containing the deserialized body.
     * @throws java.io.IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     */
    fun <TRes : Any> send(request: Request, clazz: Class<TRes>): Response<TRes> {
        val httpRequest = request.toHttpRequest()
        val httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
        val statusCode = httpResponse.statusCode()

        val bodyIsRequired = statusCode in 200..299 && statusCode != 204 && clazz != Unit::class.java
        val responseBody = if (bodyIsRequired) {
            val inputStream = if (httpResponse.isGzipped()) {
                GZIPInputStream(httpResponse.body())
            } else {
                httpResponse.body()
            }

            try {
                inputStream.use { io -> serializer.deserializeFromStream(io, clazz) }
            } catch (expected: RuntimeException) {
                throw DeserializationException(
                    statusCode = statusCode,
                    message = "Failed to deserialize response body to ${clazz.name}",
                    cause = expected
                )
            }
        } else {
            null
        }

        return Response(
            statusCode = httpResponse.statusCode(),
            headers = httpResponse.headers().map(),
            body = responseBody
        )
    }

    /**
     * Closes the underlying HTTP client and releases any resources associated with it.
     */
    override fun close() {
        httpClient.close()
    }

    companion object {
        /**
         * Creates a new Client instance with the specified HTTP client builder and serializer.
         * @param httpBuilder The HTTP client builder to use. Defaults to a new HttpClient.Builder.
         * @param serializer The serializer to use for request and response bodies. Defaults to KotlinxClientSerializer.
         * @param options The client options to configure behavior. Defaults to ClientOptions.DEFAULT.
         *
         * @return A new Client instance.
         */
        fun newClient(
            httpBuilder: HttpClient.Builder = HttpClient.newBuilder(),
            serializer: ClientSerializer = KotlinxClientSerializer,
            options: ClientOptions = ClientOptions.DEFAULT
        ): Client {
            return Client(httpBuilder.build(), serializer, options)
        }
    }

    /**
     * Builds a Request object with the specified parameters.
     * @param method The HTTP method (e.g., "GET", "POST").
     * @param uri The URI for the request.
     * @param headers The headers to include in the request.
     * @param req The request body object to serialize, or null if no body is needed.
     * @param clazz The class of the request body object.
     * @return A Request object representing the HTTP request.
     * @throws IllegalArgumentException If serialization fails.
     */
    fun <TReq : Any> buildRequest(
        method: String,
        uri: URI,
        headers: Map<String, String>,
        req: TReq?,
        clazz: Class<TReq>
    ): Request {
        if (req == null) {
            val requestHeaders = buildHeaders(contentExists = false, headers)
            return Request(
                method = method,
                uri = uri,
                headers = requestHeaders,
            )
        }

        val body = serializeBodyToByteArray(req, clazz)
        val requestHeaders = buildHeaders(contentExists = body != null, headers)

        return Request(
            method = method,
            uri = uri,
            headers = requestHeaders,
            body = body
        )
    }

    private fun buildHeaders(contentExists: Boolean, headers: Map<String, String>): Map<String, String> {
        val requestHeaders = requestAdditionalHeaders(contentExists, headers)
        if (requestHeaders.isEmpty()) {
            return defaultHeaders
        }
        return defaultHeaders + requestHeaders
    }

    private fun requestAdditionalHeaders(contentExists: Boolean, headers: Map<String, String>): Map<String, String> {
        if (!contentExists) {
            return headers
        }

        val requestHeaders = headers.toMutableMap()
        requestHeaders[CONTENT_TYPE] = serializer.contentType
        if (options.allowGzipRequests) {
            requestHeaders[CONTENT_ENCODING] = GZIP
        }
        return requestHeaders
    }

    private fun <TReq : Any> serializeBodyToByteArray(req: TReq, clazz: Class<TReq>): ByteArray? {
        if (options.allowGzipRequests) {
            return ByteArrayOutputStream().use { out ->
                GZIPOutputStream(out).use { gzip ->
                    serializer.serializeToStream(req, clazz, gzip)
                }
                out.toByteArray()
            }
        }

        if (req is ByteArray) {
            return req
        }

        if (req is String) {
            return req.toByteArray()
        }

        return ByteArrayOutputStream().use { out ->
            serializer.serializeToStream(req, clazz, out)
            out.toByteArray()
        }
    }
}

private const val GZIP = "gzip"
private const val ACCEPT_ENCODING = "Accept-Encoding"
private const val CONTENT_ENCODING = "Content-Encoding"
private const val CONTENT_TYPE = "Content-Type"

private fun HttpResponse<*>.isGzipped(): Boolean {
    return this.contentEncoding() == GZIP
}

private fun HttpResponse<*>.contentEncoding(): String? {
    return this.headers()
        .firstValue(CONTENT_ENCODING)
        .orElse(null)
}
