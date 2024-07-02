package kotlet.http.selector

import jakarta.servlet.http.HttpServletRequest

internal object TailSegmentSelector : Selector {

    override fun evaluate(
        request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int
    ): EvaluationResult {
        // Tail selector matches all segments. Two variants are possible:
        // 1) Pattern is /a/{...} and path is /a/b. In this case segmentIndex has to be less than pathSegments.size
        // 2) Pattern is /a/{...} and path is /a. In this case segmentIndex has to be equal to pathSegments.size
        return if (segmentIndex <= pathSegments.size) {
            // need to "consume" all available segments
            EvaluationResult.Success(segmentIncrement = pathSegments.size - segmentIndex)
        } else {
            EvaluationResult.Failure
        }
    }

}
