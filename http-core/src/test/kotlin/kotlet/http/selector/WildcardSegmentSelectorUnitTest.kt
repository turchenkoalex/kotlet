package kotlet.http.selector

import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class WildcardSegmentSelectorUnitTest {

    @Test
    fun evaluateSuccess() {
        val selector = WildcardSegmentSelector
        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, listOf("a", "b", "c"), 1).also {
            assertIs<EvaluationResult.Success>(it)
            assertEquals(1, it.segmentIncrement)
            assertTrue(it.parameters.isEmpty())
        }
    }

    @Test
    fun evaluateFailure() {
        val selector = WildcardSegmentSelector
        val request = mockk<HttpServletRequest>()

        selector.evaluate(request, listOf("a", "b", "c"), 3).also {
            assertIs<EvaluationResult.Failure>(it)
        }
    }
}
