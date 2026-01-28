package kotlet.client

/**
 * Configuration options for the HTTP client.
 */
data class ClientOptions(
    /**
     * Whether to allow Gzip compression for requests.
     */
    val allowGzipRequests: Boolean,

    /**
     * Whether to allow Gzip compression for responses.
     */
    val allowGzipResponses: Boolean
) {
    companion object {
        /**
         * Default client options.
         *
         * - Gzip compression for requests is disabled.
         * - Gzip compression for responses is enabled.
         */
        val DEFAULT = ClientOptions(
            allowGzipRequests = false,
            allowGzipResponses = true
        )
    }
}
