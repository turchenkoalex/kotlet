package kotlet.http

import io.mockk.*
import jakarta.servlet.http.HttpServletResponse
import kotlet.http.cors.CORS
import kotlin.test.Test

class CORSUnitTest {
    @Test
    fun testAllowAll() {
        val interceptor = CORS.interceptor(CORS.allowAll)

        val response = mockk<HttpServletResponse> {
            every { setHeader(any(), any()) } just runs
        }
        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.OPTIONS
            every { rawResponse } returns response
        }
        every { call setProperty ("status") value any<Int>() } just runs

        interceptor.aroundCall(call) {
            error("Should not be called")
        }

        verify {
            call.status = 200
            response.setHeader("Access-Control-Allow-Origin", "*")
            response.setHeader("Access-Control-Allow-Methods", "*")
            response.setHeader("Access-Control-Allow-Headers", "Accept, Authorization, Accept-Language, Content-Language, Content-Type")
        }
    }

    @Test
    fun testAllowOrigin() {
        val interceptor = CORS.interceptor(CORS.allowOrigin("https://example.com"))

        val response = mockk<HttpServletResponse> {
            every { setHeader(any(), any()) } just runs
        }
        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.OPTIONS
            every { rawResponse } returns response
        }
        every { call setProperty ("status") value any<Int>() } just runs

        interceptor.aroundCall(call) {
            error("Should not be called")
        }

        verify {
            call.status = 200
            response.setHeader("Access-Control-Allow-Origin", "https://example.com")
            response.setHeader("Access-Control-Allow-Methods", "*")
            response.setHeader("Access-Control-Allow-Headers", "Accept, Authorization, Accept-Language, Content-Language, Content-Type")
        }
    }

    @Test
    fun testNotPreflightRequest() {
        val interceptor = CORS.interceptor(CORS.allowAll)

        val response = mockk<HttpServletResponse> {
            every { setHeader(any(), any()) } just runs
        }
        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.GET
            every { rawResponse } returns response
        }
        every { call setProperty ("status") value any<Int>() } just runs

        interceptor.aroundCall(call) {
            // set status to 201 for checking
            call.status = 201
        }

        // headers should not be set
        verify(exactly = 0) {
            response.setHeader("Access-Control-Allow-Origin", any())
            response.setHeader("Access-Control-Allow-Methods", any())
            response.setHeader("Access-Control-Allow-Headers", any())
        }

        verify {
            call.status = 201
        }
    }
}