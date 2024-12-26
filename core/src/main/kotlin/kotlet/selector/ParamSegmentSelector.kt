package kotlet.selector

import jakarta.servlet.http.HttpServletRequest

internal class ParamSegmentSelector(internal val parameterName: String) : Selector {

    override fun evaluate(
        request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int
    ): EvaluationResult {
        val parameterValue = pathSegments.getOrNull(segmentIndex) ?: return EvaluationResult.Failure

        return EvaluationResult.Success(segmentIncrement = 1, parameters = mapOf(parameterName to parameterValue))
    }

}
