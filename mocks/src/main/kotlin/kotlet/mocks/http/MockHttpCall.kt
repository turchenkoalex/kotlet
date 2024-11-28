package kotlet.mocks.http

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.HttpMethod
import kotlet.attributes.RouteAttributes
import kotlet.attributes.emptyRouteAttributes
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.Enumeration

/**
 * A mock implementation of [HttpCall].
 */
class MockHttpCall(
    override val httpMethod: HttpMethod,
    override val routePath: String,
    headers: Map<String, String>,
    requestData: ByteArray,
    async: Boolean
) : HttpCall {
    private var contentTypeField: String = ""
    private var statusField: Int = 200
    private val responseStream = ByteArrayOutputStream()

    override val parameters: Map<String, String> = emptyMap()
    override val rawRequest: HttpServletRequest
    override val rawResponse: HttpServletResponse
    override val attributes: RouteAttributes = emptyRouteAttributes()

    /**
     * The response data as a [ByteArray].
     */
    val responseData: ByteArray
        get() = responseStream.toByteArray()

    val responseHeaders: MutableMap<String, String> = mutableMapOf()

    init {
        rawRequest = createHttpRequestMock(
            path = routePath,
            methodName = httpMethod.name,
            async = async,
            headers = headers,
            requestData = requestData
        )

        val responseHeaders = mutableMapOf<String, String>()
        rawResponse = mockk {
            every { outputStream } returns ByteArrayServletOutputStream(responseStream)
            every { setHeader(any(), any()) } answers {
                responseHeaders[this.firstArg()] = this.secondArg()
            }

            // contentTypeField is a private field, so we need to use a setter to set it
            every { contentType = any() } answers { contentTypeField = this.firstArg() }
            every { contentType } answers { contentTypeField }

            // statusField is a private field, so we need to use a setter to set it
            every { status = any() } answers { statusField = this.firstArg() }
            every { status } answers { statusField }

            every { addHeader(any(), any()) } answers {
                responseHeaders[this.firstArg()] = this.secondArg()
            }
            every { getHeader(any()) } answers {
                responseHeaders[this.firstArg()]
            }
            every { getHeaders(any()) } answers {
                val header = responseHeaders[this.firstArg()] ?: ""
                header.split(",").map(String::trim)
            }
        }
    }
}

private fun createHttpRequestMock(
    path: String,
    methodName: String,
    async: Boolean,
    headers: Map<String, String>,
    requestData: ByteArray,
): HttpServletRequest {
    val attributes = mutableMapOf<String, Any>()

    return mockk {
        every { requestURI } returns path
        every { queryString } returns ""
        every { inputStream } returns ByteArrayServletInputStream(requestData)
        every { getHeader(any()) } answers {
            headers[this.firstArg()]
        }
        every { getHeaders(any()) } answers {
            val header = headers[this.firstArg()] ?: ""
            Collections.enumeration(header.split(",").map(String::trim))
        }
        every { getAttribute(any()) } answers {
            attributes[this.firstArg()]
        }
        every { setAttribute(any(), any()) } answers {
            attributes[this.firstArg()] = this.secondArg()
        }
        every { removeAttribute(any()) } answers {
            attributes.remove(this.firstArg())
        }
        every { isAsyncStarted } returns async
        every { method } returns methodName
    }
}
