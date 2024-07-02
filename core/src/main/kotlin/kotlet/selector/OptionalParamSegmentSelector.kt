package kotlet.selector

import jakarta.servlet.http.HttpServletRequest

internal class OptionalParamSegmentSelector(internal val parameterName: String) : Selector {

    override fun evaluate(
        request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int
    ): EvaluationResult {
        // Optional selector matches optional parameter. Two variants are possible:
        // 1) Pattern is /user/{userId?} and path is /user/123. In this case segmentIndex has to be less than pathSegments.size
        // 2) Pattern is /user/{userId?} and path is /user. In this case segmentIndex has to be equal to pathSegments.size
        return if (segmentIndex < pathSegments.size) {
            // if path is /user/123
            val parameterValue = pathSegments[segmentIndex]
            EvaluationResult.Success(
                segmentIncrement = 1, parameters = mapOf(parameterName to parameterValue)
            )
        } else if (segmentIndex == pathSegments.size) {
            // if path is just /user
            EvaluationResult.SuccessWithZeroIncrement
        } else {
            EvaluationResult.Failure
        }
    }
}
