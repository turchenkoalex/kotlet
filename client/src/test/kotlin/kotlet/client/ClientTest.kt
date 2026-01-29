package kotlet.client

import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertNotNull
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientTest {
    private val client = Client.newClient(
        options = ClientOptions.DEFAULT.copy(allowGzipRequests = true)
    )

    @Test
    fun getGoogleComByteArray() {
        val response = client.get<ByteArray>(URI.create("https://www.google.com"))

        assertNotNull(response)

        assertTrue {
            requireNotNull(response)
            response.isNotEmpty()
        }
    }

    @Test
    fun getGoogleComString() {
        val response = client.get<String>(URI.create("https://www.google.com"))

        assertNotNull(response)

        assertTrue {
            requireNotNull(response)
            response.isNotEmpty()
        }
    }

    @Test
    fun `test 200 response with structured type throws DeserializationException`() {
        // This demonstrates the bug: 404 responses with empty body will fail to deserialize to a structured type
        val exception = assertFailsWith<DeserializationException> {
            client.get<TestResponse>(URI.create("https://httpbin.org/status/200"))
        }

        assertEquals(200, exception.statusCode)
    }

    @Test
    fun `test 404 response with structured type do not throws`() {
        val request = Request.get(URI.create("https://httpbin.org/status/404"))
        val response = client.send(request, TestResponse::class.java)

        assertEquals(404, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `test 500 response with structured type do not throws`() {
        val request = Request.get(URI.create("https://httpbin.org/status/500"))
        val response = client.send(request, String::class.java)

        assertEquals(500, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `test 204 No Content does not throw`() {
        // 204 should work fine since it's excluded
        val response = client.send(Request.get(URI.create("https://httpbin.org/status/204")), Unit::class.java)
        assertEquals(204, response.statusCode)
    }
}

@Serializable
data class TestResponse(val message: String)

