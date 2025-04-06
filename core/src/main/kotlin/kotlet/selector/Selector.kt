package kotlet.selector

import jakarta.servlet.http.HttpServletRequest

/**
 * Selector is an interface that defines a method for evaluating a request against a specific path segment.
 */
internal interface Selector {

    fun evaluate(request: HttpServletRequest, pathSegments: List<String>, segmentIndex: Int): EvaluationResult

}

/**
 * EvaluationResult is a sealed class that represents the result of evaluating a request against a path segment.
 * It can be either a success or a failure.
 */
internal sealed class EvaluationResult {

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
