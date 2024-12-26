package kotlet

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlet.mocks.Mocks
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutingServletTest {
    @Test
    fun `servlet should delegate service method`() {
        val handler = mockk<RoutingHandler> {
            every { service(any(), any()) } returns Unit
        }
        val servlet = RoutingServlet(handler)
        val call = Mocks.httpCall(HttpMethod.GET, "/test")

        servlet.service(call.rawRequest, call.rawResponse)

        verify {
            handler.service(call.rawRequest, call.rawResponse)
        }
    }

    @Test
    fun `servlet should delegate toString method`() {
        val handler = mockk<RoutingHandler>()
        every { handler.toString() } returns "MockedRoutingHandler"

        val servlet = RoutingServlet(handler)

        assertEquals("RoutingServlet(routingHandler=MockedRoutingHandler)", servlet.toString())
    }

}
