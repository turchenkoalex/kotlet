package kotlet.cors

import java.time.Duration

/**
 * The result of evaluating [CorsRules] for a given request.
 * Can be either [Headers] or [Error].
 */
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
        val allowHeaders: String,

        /**
         * The HTTP Access-Control-Max-Age response header indicates how long the results of a preflight request
         * (that is, the information contained in the Access-Control-Allow-Methods and Access-Control-Allow-Headers
         * headers) can be cached. Not specified if empty.
         */
        val maxAgeSeconds: String
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
            /**
             * The Access-Control-Allow-Origin response header indicates whether the response can be shared with
             * requesting code from the given origin.
             * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin
             */
            allowOrigin: String,

            /**
             * The Access-Control-Allow-Methods response header specifies one or more methods allowed when accessing
             * a resource in response to a preflight request.
             * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Methods
             */
            allowMethods: List<String>,

            /**
             * The Access-Control-Allow-Headers response header is used in response to a preflight request which
             * includes the Access-Control-Request-Headers to indicate which HTTP headers can be used during the
             * actual request. https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Headers
             */
            allowHeaders: List<String>,

            /**
             * The HTTP Access-Control-Max-Age response header indicates how long the results of a preflight request
             * (that is, the information contained in the Access-Control-Allow-Methods and Access-Control-Allow-Headers
             * headers) can be cached. Not specified if empty.
             */
            maxAge: Duration = Duration.ZERO,
        ): CorsResponse {
            val maxAgeSeconds = if (maxAge.seconds > 0) {
                maxAge.seconds.toString()
            } else {
                ""
            }

            return Headers(
                allowOrigin = allowOrigin,
                allowMethods = allowMethods.distinct().joinToString(separator = ", "),
                allowHeaders = allowHeaders.distinct().joinToString(separator = ", "),
                maxAgeSeconds = maxAgeSeconds
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

