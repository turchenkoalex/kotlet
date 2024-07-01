package kotlet.selector

import jakarta.servlet.http.HttpServletRequest

interface Selector {

    fun evaluate(request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int): EvaluationResult

}

sealed class EvaluationResult {

    data class Success(
        val segmentIncrement: Int, val parameters: Map<String, String> = emptyMap()
    ) : EvaluationResult()

    data object Failure : EvaluationResult()

    companion object {
        // Useful constants to avoid excessive allocation
        val SuccessWithZeroIncrement = Success(segmentIncrement = 0)
        val SuccessWithOneIncrement = Success(segmentIncrement = 1)
    }

}
