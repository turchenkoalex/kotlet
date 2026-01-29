package kotlet.client

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class HeaderTest {
    private val client = Client.newClient()

    @Test
    fun `custom Content-Type header is overwritten`() {
        // This test demonstrates a bug: user-provided Content-Type header is ignored
        val customContentType = "application/xml"

        val request = client.buildRequest(
            method = "POST",
            uri = URI.create("https://httpbin.org/post"),
            headers = mapOf("Content-Type" to customContentType),
            req = "test body",
            clazz = String::class.java
        )

        // The request should have the custom Content-Type, but it will be overwritten with application/json
        val actualContentType = request.headers["Content-Type"]
        println("Expected: $customContentType")
        println("Actual: $actualContentType")

        // This assertion will fail, demonstrating the bug
        assertEquals(
            customContentType, actualContentType,
            "User-provided Content-Type should not be overwritten"
        )
    }

    @Test
    fun `Content-Type is set automatically when not provided`() {
        val request = client.buildRequest(
            method = "POST",
            uri = URI.create("https://httpbin.org/post"),
            headers = emptyMap(),
            req = "test body",
            clazz = String::class.java
        )

        val contentType = request.headers["Content-Type"]
        assertEquals(
            "application/json", contentType,
            "Content-Type should be set automatically"
        )
    }

    @Test
    fun `Content-Type not set for requests without body`() {
        val request = client.buildRequest(
            method = "GET",
            uri = URI.create("https://httpbin.org/get"),
            headers = emptyMap(),
            req = null,
            clazz = Unit::class.java
        )

        val contentType = request.headers["Content-Type"]
        assertEquals(
            null, contentType,
            "Content-Type should not be set for GET requests"
        )
    }

    @Test
    fun `Content-Encoding set when gzip enabled and body exists`() {
        val clientWithGzip = Client.newClient(
            options = ClientOptions.DEFAULT.copy(allowGzipRequests = true)
        )

        val request = clientWithGzip.buildRequest(
            method = "POST",
            uri = URI.create("https://httpbin.org/post"),
            headers = emptyMap(),
            req = "test body",
            clazz = String::class.java
        )

        val contentEncoding = request.headers["Content-Encoding"]
        assertEquals(
            "gzip", contentEncoding,
            "Content-Encoding should be set when gzip is enabled"
        )
    }
}
