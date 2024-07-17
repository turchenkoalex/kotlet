package kotlet.mocks.http

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.HttpMethod
import java.io.ByteArrayOutputStream

/**
 * A mock implementation of [HttpCall].
 */
class MockHttpCall(
    override val httpMethod: HttpMethod,
    headers: Map<String, String>,
    requestData: ByteArray,
) : HttpCall {
    private var contentTypeField: String = ""
    private var statusField: Int = 200
    private val responseStream = ByteArrayOutputStream()

    override val routePath = "/"
    override val parameters: Map<String, String> = emptyMap()
    override val rawRequest: HttpServletRequest
    override val rawResponse: HttpServletResponse


    /**
     * The response data as a [ByteArray].
     */
    val responseData: ByteArray
        get() = responseStream.toByteArray()

    init {
        val attributes = mutableMapOf<String, Any>()

        rawRequest = mockk {
            every { inputStream } returns ByteArrayServletInputStream(requestData)
            every { getHeader(any()) } answers {
                headers[this.firstArg()]
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
            every { isAsyncStarted } returns false
        }

        rawResponse = mockk {
            every { outputStream } returns ByteArrayServletOutputStream(responseStream)

            // contentTypeField is a private field, so we need to use a setter to set it
            every { contentType = any() } answers { contentTypeField = this.firstArg() }
            every { contentType } answers { contentTypeField }

            // statusField is a private field, so we need to use a setter to set it
            every { status = any() } answers { statusField = this.firstArg() }
            every { status } answers { statusField }
        }
    }
}
