package kotlet.selector

import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.RouteHelpers
import kotlin.test.Test
import kotlin.test.assertIs

internal class OptionalParamSegmentSelectorUnitTest {

    @Test
    fun evaluateSuccess_IfSegmentExists() {
        val selector = OptionalParamSegmentSelector("userId")

        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/123123"), 1).also {
            assertIs<EvaluationResult.Success>(it)
            kotlin.test.assertEquals(1, it.segmentIncrement)
            kotlin.test.assertEquals(mapOf("userId" to "123123"), it.parameters)
        }
    }

    @Test
    fun evaluateSuccess_IfNoSegment() {
        val selector = OptionalParamSegmentSelector("userId")

        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first"), 1).also {
            assertIs<EvaluationResult.Success>(it)
            kotlin.test.assertEquals(0, it.segmentIncrement)
            kotlin.test.assertEquals(emptyMap(), it.parameters)
        }
    }

    @Test
    fun evaluateFailure() {
        val selector = OptionalParamSegmentSelector("userId")

        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/123123"), 3).also {
            assertIs<EvaluationResult.Failure>(it)
        }
    }

}
