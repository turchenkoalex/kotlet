package kotlet

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlet.mocks.Mocks
import kotlin.test.Test
import kotlin.test.assertEquals

class KotletTest {
    @Test
    fun handlerTest() {
        val routing = Kotlet.routing {
            get("/test") {
                it.status = 201
            }
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(201, call.status)
    }

    @Test
    fun handlerListOfRoutingTest() {
        val routing = Kotlet.routing {
            get("/test") {
                it.status = 201
            }
        }

        val handler = Kotlet.handler(listOf(routing))

        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(201, call.status)
    }

    @Test
    fun servletTest() {
        val routing = Kotlet.routing {
            get("/test") {
                it.status = 201
            }
        }

        val servlet = Kotlet.servlet(routing)

        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        servlet.service(call.rawRequest, call.rawResponse)

        assertEquals(201, call.status)
    }

    @Test
    fun servletListOfRoutingTest() {
        val routing = Kotlet.routing {
            get("/test") {
                it.status = 201
            }
        }

        val servlet = Kotlet.servlet(listOf(routing))

        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        servlet.service(call.rawRequest, call.rawResponse)

        assertEquals(201, call.status)
    }

    @Test
    fun handlerNotFoundTest() {
        val routing = Kotlet.routing {
            get("/test") {}
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(HttpMethod.GET, "/not_exists")

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(404, call.status)
        assertEquals("Not found", call.responseDataAsString)
    }

    @Test
    fun handlerNotAcceptedTest() {
        val routing = Kotlet.routing {
            get("/test") {}
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(HttpMethod.POST, "/test")

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(405, call.status)
        assertEquals("Method not allowed", call.responseDataAsString)
    }

    @Test
    fun handlerErrorTest() {
        val routing = Kotlet.routing {
            get("/test") {
                error("Test error")
            }
        }

        val handler = Kotlet.handler(routing)

        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(500, call.status)
        assertEquals("Internal server error", call.responseDataAsString)
    }

    @Test
    fun handlerCustomErrorTest() {
        val routing = Kotlet.routing {
            get("/test") {
                error("Test error")
            }
        }

        val errorHandler = object : ErrorsHandler {
            override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
                response.status = 501
                response.writer.write("Custom error")
                response.writer.flush()
            }
        }
        val handler = Kotlet.handler(listOf(routing), errorHandler)

        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        handler.service(call.rawRequest, call.rawResponse)

        assertEquals(501, call.status)
        assertEquals("Custom error", call.responseDataAsString)
    }

}
