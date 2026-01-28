package kotlet.client

import com.sun.net.httpserver.Headers

/**
 * Configuration options for the HTTP client.
 */
data class ClientOptions(
    /**
     * The User-Agent header value to be sent with each request.
     * Defaults to an empty string (no User-Agent header).
     */
    val userAgent: String = "",

    /**
     * Whether to allow Gzip compression for requests.
     */
    val allowGzipRequests: Boolean = false,

    /**
     * Whether to allow Gzip compression for responses.
     */
    val allowGzipResponses: Boolean = true,

    /**
     * Additional headers to include in every request.
     */
    val additionalHeaders: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Default client options.
         *
         * - No User-Agent header is sent.
         * - Gzip compression for requests is disabled.
         * - Gzip compression for responses is enabled.
         * - No additional headers are included.
         */
        val DEFAULT = ClientOptions()
    }
}
