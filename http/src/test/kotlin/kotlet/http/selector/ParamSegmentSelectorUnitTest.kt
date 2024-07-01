package kotlet.http.selector

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.http.RouteHelpers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ParamSegmentSelectorUnitTest {

    @Test
    fun evaluateSuccess() {
        val selector = ParamSegmentSelector("userId")

        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns "/first/{userId}/second/{fileId}"

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/123123/second/AAABBB"), 1).also {
            assertIs<EvaluationResult.Success>(it)
            assertEquals(1, it.segmentIncrement)
            assertEquals(mapOf("userId" to "123123"), it.parameters)
        }
    }

    @Test
    fun evaluateFailure() {
        val selector = ParamSegmentSelector("userId")

        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns "/first/{userId}/second"

        selector.evaluate(request, RouteHelpers.parsePathToSegments("/first/123123/second"), 3).also {
            assertIs<EvaluationResult.Failure>(it)
        }
    }
}
