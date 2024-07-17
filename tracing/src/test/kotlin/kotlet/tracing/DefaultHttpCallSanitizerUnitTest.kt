package kotlet.tracing

import io.mockk.every
import kotlet.HttpMethod
import kotlet.mocks.Mocks
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultHttpCallSanitizerUnitTest {
    @Test
    fun `default sanitizer just return values`() {
        val call = Mocks.httpCall(
            method = HttpMethod.GET,
            headers = mapOf("key" to "value, value2")
        )

        call.rawResponse.addHeader("Content-Type", "application/json")

        every { call.rawRequest.requestURI } returns "/"
        every { call.rawRequest.queryString } returns "query=1"

        assertEquals(mutableListOf("value", "value2"), DefaultHttpCallSanitizer.getHttpRequestHeader(call, "key"))
        assertEquals(
            mutableListOf("application/json"),
            DefaultHttpCallSanitizer.getHttpResponseHeader(call.rawResponse, "Content-Type")
        )
        assertEquals("/", DefaultHttpCallSanitizer.getUrlPath(call))
        assertEquals("query=1", DefaultHttpCallSanitizer.getUrlQuery(call))
    }
}
