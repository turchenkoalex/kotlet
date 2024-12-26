package kotlet.cors

sealed class CorsResponse {
    internal data class Headers(
        /**
         * The Access-Control-Allow-Origin response header indicates whether the response can be shared with requesting
         * code from the given origin.
         * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin
         */
        val allowOrigin: String,

        /**
         * The Access-Control-Allow-Methods response header specifies one or more methods allowed when accessing a
         * resource in response to a preflight request.
         * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Methods
         */
        val allowMethods: String,

        /**
         * The Access-Control-Allow-Headers response header is used in response to a preflight request which includes
         * the Access-Control-Request-Headers to indicate which HTTP headers can be used during the actual request.
         * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Headers
         */
        val allowHeaders: String
    ) : CorsResponse()

    internal data class Error(
        val statusCode: Int,
        val message: String,
    ) : CorsResponse()

    companion object {
        /**
         * Creates a [CorsResponse] with the given headers.
         */
        fun headers(
            allowOrigin: String,
            allowMethods: List<String>,
            allowHeaders: List<String>,
        ): CorsResponse {
            return Headers(
                allowOrigin = allowOrigin,
                allowMethods = allowMethods.distinct().joinToString(separator = ", "),
                allowHeaders = allowHeaders.distinct().joinToString(separator = ", "),
            )
        }

        /**
         * Creates a [CorsResponse] with error status code and message.
         */
        fun error(
            statusCode: Int,
            message: String,
        ): CorsResponse {
            return Error(statusCode, message)
        }
    }
}

