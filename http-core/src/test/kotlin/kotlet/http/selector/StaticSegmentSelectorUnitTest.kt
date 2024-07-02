package kotlet.http.selector

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.http.RouteHelpers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue


internal class StaticSegmentSelectorUnitTest {

    @Test
    fun evaluateSuccess() {
        val selector = StaticSegmentSelector("first")

        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns "/first/second"

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/second"), 0).also {
            assertIs<EvaluationResult.Success>(it)
            assertEquals(1, it.segmentIncrement)
            assertTrue(it.parameters.isEmpty())
        }
    }

    @Test
    fun evaluateFailure() {
        val selector = StaticSegmentSelector("first")

        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns "/first/second"

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/second"), 1).also {
            assertIs<EvaluationResult.Failure>(it)
        }
    }

}
