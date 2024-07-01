package kotlet.tracing

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpMethod
import kotlet.mocks.Mocks
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class ServletHttpServerAttributesGetterUnitTest {
    private val sanitizer = mockk<HttpCallSanitizer>()
    private val getter = ServletHttpServerAttributesGetter(sanitizer)

    @Test
    fun `getHttpRequestMethod returns method`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        assertEquals("GET", getter.getHttpRequestMethod(call))
    }

    @Test
    fun `getHttpRoute returns route`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET,
            routePath = "/test",
        )

        assertEquals("/test", getter.getHttpRoute(call))
    }

    @Test
    fun `getHttpRequestHeader returns header through sanitizer`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET,
            headers = mapOf("key" to "value")
        )

        every { sanitizer.getHttpRequestHeader(call, "key") } returns mutableListOf("sanitized value")

        assertEquals(mutableListOf("sanitized value"), getter.getHttpRequestHeader(call, "key"))
    }

    @Test
    fun `getHttpResponseStatusCode returns status code`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        val response = mockk<HttpServletResponse>()
        every { response.status } returns 202

        assertEquals(202, getter.getHttpResponseStatusCode(call, response, null))
    }

    @Test
    fun `getHttpResponseHeader returns header through sanitizer`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        val response = mockk<HttpServletResponse>()
        every { response.getHeaders("key") } returns listOf("value")
        every { sanitizer.getHttpResponseHeader(response, "key") } returns mutableListOf("sanitized value")

        assertEquals(mutableListOf("sanitized value"), getter.getHttpResponseHeader(call, response, "key"))
    }

    @Test
    fun `getUrlScheme returns scheme`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        every { call.rawRequest.scheme } returns "http"

        assertEquals("http", getter.getUrlScheme(call))
    }

    @Test
    fun `getUrlPath returns path through sanitizer`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET,
        )

        every { sanitizer.getUrlPath(call) } returns "/sanitized_test"

        assertEquals("/sanitized_test", getter.getUrlPath(call))
    }

    @Test
    fun `getUrlQuery returns query through sanitizer`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET,
        )

        every { sanitizer.getUrlQuery(call) } returns "query=sanitized"

        assertEquals("query=sanitized", getter.getUrlQuery(call))
    }

    @Test
    fun `getNetworkTransport returns TCP`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET,
        )

        assertEquals("tcp", getter.getNetworkTransport(call, null))
    }
}
