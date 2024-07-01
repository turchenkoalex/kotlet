package kotlet.selector

import jakarta.servlet.http.HttpServletRequest

internal object WildcardSegmentSelector : Selector {

    override fun evaluate(
        request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int
    ): EvaluationResult {
        // Wildcard selector matches any string
        // Only check here – boundaries check
        return if (segmentIndex < pathSegments.size) {
            EvaluationResult.SuccessWithOneIncrement
        } else {
            EvaluationResult.Failure
        }
    }

}
