package kotlet.http.selector

import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.http.RouteHelpers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class TailSegmentSelectorUnitTest {

    @Test
    fun evaluateSuccess_IfSegmentsExist() {
        val selector = TailSegmentSelector

        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/second/third"), 1).also {
            assertIs<EvaluationResult.Success>(it)
            assertEquals(2, it.segmentIncrement)
            assertTrue(it.parameters.isEmpty())
        }
    }

    @Test
    fun evaluateSuccess_IfNoSegments() {
        val selector = TailSegmentSelector

        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/second/third"), 3).also {
            assertIs<EvaluationResult.Success>(it)
            assertEquals(0, it.segmentIncrement)
            assertTrue(it.parameters.isEmpty())
        }
    }


    @Test
    fun evaluateFailure() {
        val selector = TailSegmentSelector

        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/second/third"), 4).also {
            assertIs<EvaluationResult.Failure>(it)
        }
    }
}
