package kotlet.selector

import jakarta.servlet.http.HttpServletRequest

internal class StaticSegmentSelector(val staticSegment: String) : Selector {

    override fun evaluate(
        request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int
    ): EvaluationResult {
        return if (staticSegment == pathSegments.getOrNull(segmentIndex)) {
            EvaluationResult.SuccessWithOneIncrement
        } else {
            EvaluationResult.Failure
        }
    }
}
