package kotlet.cors

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class CORSUnitTest {
    @Test
    fun testAllowAll() {
        val response = CORS.allowAll.getResponse(mockk())
        assert(response is CorsResponse.Headers)
        response as CorsResponse.Headers
        assertEquals("*", response.allowOrigin)
        assertEquals("*", response.allowMethods)
        assertEquals("Accept, Authorization, Accept-Language, Content-Language, Content-Type", response.allowHeaders)
    }

    @Test
    fun testAllowOrigin() {
        val response = CORS.allowOrigin("https://example.com").getResponse(mockk())
        assert(response is CorsResponse.Headers)
        response as CorsResponse.Headers
        assertEquals("https://example.com", response.allowOrigin)
        assertEquals("*", response.allowMethods)
        assertEquals("Accept, Authorization, Accept-Language, Content-Language, Content-Type", response.allowHeaders)
    }

    @Test
    fun testHeadersResponse() {
        val interceptor = CORS.interceptor(object : CorsRules {
            override fun getResponse(call: HttpCall): CorsResponse {
                return CorsResponse.headers(
                    allowOrigin = "https://example.com",
                    allowMethods = listOf("*"),
                    allowHeaders = listOf("Content-Type", "Authorization")
                )
            }
        })

        val response = mockk<HttpServletResponse> {
            every { setHeader(any(), any()) } just Runs
        }
        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.OPTIONS
            every { rawResponse } returns response
        }
        every { call setProperty ("status") value any<Int>() } just Runs

        interceptor.aroundCall(call) {
            error("Should not be called")
        }

        verify {
            call.status = 200
            response.setHeader("Access-Control-Allow-Origin", "https://example.com")
            response.setHeader("Access-Control-Allow-Methods", "*")
            response.setHeader(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
            )
        }
    }

    @Test
    fun testErrorResponse() {
        val interceptor = CORS.interceptor(object : CorsRules {
            override fun getResponse(call: HttpCall): CorsResponse {
                return CorsResponse.error(403, "Forbidden")
            }
        })

        val response = mockk<HttpServletResponse> {
            every { setHeader(any(), any()) } just Runs
        }
        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.OPTIONS
            every { rawResponse } returns response
            every { respondText(any()) } just Runs
        }
        every { call setProperty ("status") value any<Int>() } just Runs

        interceptor.aroundCall(call) {
            error("Should not be called")
        }

        // headers should not be set
        verify(exactly = 0) {
            response.setHeader("Access-Control-Allow-Origin", any())
            response.setHeader("Access-Control-Allow-Methods", any())
            response.setHeader("Access-Control-Allow-Headers", any())
        }

        verify {
            call.status = 403
            call.respondText("Forbidden")
        }
    }

    @Test
    fun testNotPreflightRequest() {
        val interceptor = CORS.interceptor(CORS.allowAll)

        val response = mockk<HttpServletResponse> {
            every { setHeader(any(), any()) } just Runs
        }
        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.GET
            every { rawResponse } returns response
        }
        every { call setProperty ("status") value any<Int>() } just Runs

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
