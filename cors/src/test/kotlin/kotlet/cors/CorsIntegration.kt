package kotlet.cors

import io.mockk.confirmVerified
import io.mockk.verify
import kotlet.HttpCall
import kotlet.Kotlet
import kotlet.mocks.Mocks
import kotlin.test.Test

class CorsIntegration {
    @Test
    fun testOptionsMethod() {
        val routing = Kotlet.routing {
            installCORS(CORS.allowAll)

            get("/test") {
                it.status = 200
            }
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(
            method = kotlet.HttpMethod.OPTIONS,
            routePath = "/test",
            headers = mapOf(
                "Origin" to "https://example.com",
                "Access-Control-Request-Method" to "GET"
            )
        )

        handler.service(call.rawRequest, call.rawResponse)

        verify {
            call.status = 200
            call.rawResponse.setHeader("Access-Control-Allow-Origin", "*")
            call.rawResponse.setHeader("Access-Control-Allow-Methods", "*")
            call.rawResponse.setHeader(
                "Access-Control-Allow-Headers",
                "Accept, Authorization, Accept-Language, Content-Language, Content-Type"
            )
            call.rawResponse.setHeader("Access-Control-Max-Age", "600")
        }

        confirmVerified(call.rawResponse)
    }

    @Test
    fun testGetMethod() {
        val routing = Kotlet.routing {
            installCORS(CORS.allowAll)

            get("/test") {
                it.status = 202
            }
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(
            method = kotlet.HttpMethod.GET,
            routePath = "/test",
            headers = mapOf(
                "Origin" to "https://example.com",
                "Access-Control-Request-Method" to "GET"
            )
        )

        handler.service(call.rawRequest, call.rawResponse)

        verify {
            call.status = 202
            call.rawResponse.setHeader("Access-Control-Allow-Origin", "*")
        }

        confirmVerified(call.rawResponse)
    }

    @Test
    fun testOptionsNotAllowedMethod() {
        val routing = Kotlet.routing {
            get("/test") {
                it.status = 200
            }
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(
            method = kotlet.HttpMethod.OPTIONS,
            routePath = "/test",
            headers = mapOf(
                "Origin" to "https://example.com",
                "Access-Control-Request-Method" to "GET"
            )
        )

        handler.service(call.rawRequest, call.rawResponse)

        verify {
            call.rawResponse.sendError(405, "Method not allowed")
        }

        confirmVerified(call.rawResponse)
    }

    @Test
    fun testCorsErrorOnGetMethod() {
        val routing = Kotlet.routing {
            installCORS(
                object : CorsRules {
                    override fun getResponse(call: HttpCall): CorsResponse {
                        return CorsResponse.error(403, "Cors Error")
                    }

                }
            )

            get("/test") {
                it.status = 202
            }
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(
            method = kotlet.HttpMethod.GET,
            routePath = "/test",
            headers = mapOf(
                "Origin" to "https://example.com",
                "Access-Control-Request-Method" to "GET"
            )
        )

        handler.service(call.rawRequest, call.rawResponse)

        verify {
            call.respondError(403, "Cors Error")
        }

        verify(exactly = 0) {
            call.rawResponse.setHeader("Access-Control-Allow-Origin", any())
        }

        confirmVerified(call.rawResponse)
    }
}
