package kotlet

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.attributes.emptyRouteAttributes
import kotlet.mocks.Mocks
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals

class HttpCallExtensionsTest {

    private fun createHttpCall(content: ByteArray, contentLength: Long = -1): HttpCall {
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>(relaxed = true)

        every { request.contentLengthLong } returns contentLength
        every { request.inputStream } returns object : ServletInputStream() {
            private val stream = ByteArrayInputStream(content)
            override fun read(): Int = stream.read()
            override fun read(b: ByteArray, off: Int, len: Int): Int = stream.read(b, off, len)
            override fun isFinished(): Boolean = false
            override fun isReady(): Boolean = true
            override fun setReadListener(listener: jakarta.servlet.ReadListener?) {}
        }

        return HttpCallImpl(
            httpMethod = HttpMethod.POST,
            routePath = "/test",
            rawRequest = request,
            rawResponse = response,
            parameters = emptyMap(),
            attributes = emptyRouteAttributes()
        )
    }

    @Test
    fun `receiveText should read text with default limit`() {
        val content = "Hello, World!".toByteArray()
        val call = createHttpCall(content)

        val result = call.receiveText()

        assertEquals("Hello, World!", result)
    }

    @Test
    fun `receiveText should throw when exceeding limit`() {
        val content = "x".repeat(1000).toByteArray()
        val call = createHttpCall(content)

        val exception = assertThrows<RequestBodyTooLargeException> {
            call.receiveText(maxSize = 100)
        }

        assertEquals(100, exception.maxSize)
    }

    @Test
    fun `receiveBytes should read bytes with default limit`() {
        val content = byteArrayOf(1, 2, 3, 4, 5)
        val call = createHttpCall(content)

        val result = call.receiveBytes()

        assertEquals(content.toList(), result.toList())
    }

    @Test
    fun `receiveBytes should throw when exceeding limit`() {
        val content = ByteArray(1000) { it.toByte() }
        val call = createHttpCall(content)

        val exception = assertThrows<RequestBodyTooLargeException> {
            call.receiveBytes(maxSize = 100)
        }

        assertEquals(100, exception.maxSize)
    }

    @Test
    fun `receiveLimitedStream should allow reading within limit`() {
        val content = "Small content".toByteArray()
        val call = createHttpCall(content)

        val stream = call.receiveLimitedStream(maxSize = 1000)
        val result = stream.readBytes()

        assertEquals("Small content", String(result))
    }

    @Test
    fun `receiveLimitedStream should throw when reading beyond limit`() {
        val content = ByteArray(1000) { it.toByte() }
        val call = createHttpCall(content)

        val stream = call.receiveLimitedStream(maxSize = 100)

        assertThrows<RequestBodyTooLargeException> {
            stream.readBytes()
        }
    }

    @Test
    fun `receiveText should work with custom charset`() {
        val content = "Привет, мир!".toByteArray(Charsets.UTF_8)
        val call = createHttpCall(content)

        val result = call.receiveText(charset = Charsets.UTF_8)

        assertEquals("Привет, мир!", result)
    }

    @Test
    fun `receiveBytes should handle empty body`() {
        val content = ByteArray(0)
        val call = createHttpCall(content)

        val result = call.receiveBytes()

        assertEquals(0, result.size)
    }

    @Test
    fun `receiveText should handle empty body`() {
        val content = ByteArray(0)
        val call = createHttpCall(content)

        val result = call.receiveText()

        assertEquals("", result)
    }

    @Test
    fun `receiveBytes should accept body of exactly maxSize`() {
        val content = ByteArray(100) { it.toByte() }
        val call = createHttpCall(content)

        val result = call.receiveBytes(maxSize = 100)

        assertEquals(content.toList(), result.toList())
    }

    @Test
    fun `receiveLimitedStream should accept body of exactly maxSize`() {
        val content = ByteArray(100) { it.toByte() }
        val call = createHttpCall(content)

        val result = call.receiveLimitedStream(maxSize = 100).readBytes()

        assertEquals(content.toList(), result.toList())
    }

    @Test
    fun `receiveBytes should throw when exceeding limit by one byte`() {
        val content = ByteArray(101)
        val call = createHttpCall(content)

        assertThrows<RequestBodyTooLargeException> {
            call.receiveBytes(maxSize = 100)
        }
    }

    @Test
    fun `receiveBytes should read body larger than buffer`() {
        val content = ByteArray(100_000) { (it % 251).toByte() }
        val call = createHttpCall(content)

        val result = call.receiveBytes()

        assertEquals(content.toList(), result.toList())
    }

    @Test
    fun `receiveBytes should fail fast on large content length`() {
        val call = createHttpCall(ByteArray(0), contentLength = 1000)

        val exception = assertThrows<RequestBodyTooLargeException> {
            call.receiveBytes(maxSize = 100)
        }

        assertEquals(100, exception.maxSize)
        assertEquals(1000, exception.bytesRead)
    }

    @Test
    fun `too large body should be handled with 413 by default`() {
        val routing = Kotlet.routing {
            post("/test") { call ->
                call.receiveText(maxSize = 10)
            }
        }
        val handler = Kotlet.handler(routing)
        val call = Mocks.httpCall(HttpMethod.POST, "/test", data = ByteArray(100))

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(413, call.status)
    }
}
