package kotlet

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

}
