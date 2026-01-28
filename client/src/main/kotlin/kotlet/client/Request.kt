package kotlet.client

import java.net.URI
import java.net.http.HttpRequest

/**
 * Generic HTTP request representation.
 */
class Request(
    val method: String,
    val uri: URI,
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray? = null
) {
    internal fun toHttpRequest(): HttpRequest {
        val builder = HttpRequest.newBuilder(uri)

        headers.forEach { (key, value) ->
            builder.header(key, value)
        }

        val bodyPublisher = if (body != null) {
            HttpRequest.BodyPublishers.ofByteArray(body)
        } else {
            HttpRequest.BodyPublishers.noBody()
        }

        builder.method(method, bodyPublisher)

        return builder.build()
    }

    companion object {
        /**
         * Creates a GET request.
         */
        fun get(uri: URI, headers: Map<String, String> = emptyMap()): Request {
            return Request(
                method = "GET",
                uri = uri,
                headers = headers,
                body = null
            )
        }

        /**
         * Creates a POST request.
         */
        fun post(uri: URI, body: ByteArray? = null, headers: Map<String, String> = emptyMap()): Request {
            return Request(
                method = "POST",
                uri = uri,
                headers = headers,
                body = body
            )
        }

        /**
         * Creates a PUT request.
         */
        fun put(uri: URI, body: ByteArray? = null, headers: Map<String, String> = emptyMap()): Request {
            return Request(
                method = "PUT",
                uri = uri,
                headers = headers,
                body = body
            )
        }

        /**
         * Creates a DELETE request.
         */
        fun delete(uri: URI, headers: Map<String, String> = emptyMap()): Request {
            return Request(
                method = "DELETE",
                uri = uri,
                headers = headers,
                body = null
            )
        }
    }
}
