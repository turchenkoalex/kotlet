package kotlet

import jakarta.servlet.http.HttpServletRequest
import kotlet.selector.EvaluationResult
import kotlet.selector.OptionalParamSegmentSelector
import kotlet.selector.ParamSegmentSelector
import kotlet.selector.Selector
import kotlet.selector.StaticSegmentSelector
import kotlet.selector.TailSegmentSelector
import kotlet.selector.WildcardSegmentSelector

internal object RouteHelpers {

    internal const val ROOT_ROUTE_PATH = "/"

    private const val ROUTE_SEGMENT_PREFIX = "{"
    private const val ROUTE_SEGMENT_SUFFIX = "}"
    private const val OPTIONAL_ROUTE_SEGMENT_SUFFIX = "?$ROUTE_SEGMENT_SUFFIX"
    private const val WILDCARD_SEGMENT = "*"
    private const val TAIL_SEGMENT = "{...}"

    fun parsePathToSegments(path: String): List<String> {
        val segments = mutableListOf<String>()
        var start = 0
        val length = path.length

        for (i in 0 until length) {
            if (path[i] == '/') {
                if (start < i) {
                    segments.add(path.substring(start, i))
                }
                start = i + 1
            }
        }

        // add last segment if it is not empty
        if (start < length) {
            segments.add(path.substring(start))
        }

        return segments
    }

    fun prepareSelectorsList(route: Route): List<Selector> {
        return prepareSelectorsList(route.path)
    }

    private fun prepareSelectorsList(
        routePath: String
    ): MutableList<Selector> {
        val selectors = mutableListOf<Selector>()

        // Now let's create selectors for each path segment
        parsePathToSegments(routePath).forEach { pathSegment ->
            val selector = if (pathSegment == WILDCARD_SEGMENT) {
                WildcardSegmentSelector
            } else if (pathSegment == TAIL_SEGMENT) {
                TailSegmentSelector
            } else if (pathSegment.surroundedBy(ROUTE_SEGMENT_PREFIX, OPTIONAL_ROUTE_SEGMENT_SUFFIX)) {
                val paramName = pathSegment.removeSurrounding(ROUTE_SEGMENT_PREFIX, OPTIONAL_ROUTE_SEGMENT_SUFFIX)
                OptionalParamSegmentSelector(paramName)
            } else if (pathSegment.surroundedBy(ROUTE_SEGMENT_PREFIX, ROUTE_SEGMENT_SUFFIX)) {
                val paramName = pathSegment.removeSurrounding(ROUTE_SEGMENT_PREFIX, ROUTE_SEGMENT_SUFFIX)
                ParamSegmentSelector(paramName)
            } else {
                StaticSegmentSelector(pathSegment)
            }

            selectors += selector
        }

        return selectors
    }

    fun checkSelectorsList(
        routePath: String, selectors: List<Selector>
    ) {
        // Some obvious checks for routing settings

        // Tail selector can be only one
        val tailsCount = selectors.count { it is TailSegmentSelector }
        if (tailsCount > 1) {
            throw IllegalArgumentException(
                "$tailsCount tails have been found. " +
                    "You can't have more than 1 tail selector: $routePath"
            )
        }

        // OptionalParam selector can be only one
        val optionalParamsCount = selectors.count { it is OptionalParamSegmentSelector }
        if (optionalParamsCount > 1) {
            throw IllegalArgumentException(
                "$optionalParamsCount optionals have been found. " +
                    "You can't have more than 1 optional selector: $routePath"
            )
        }

        // If there is a Tail selector, it must be the last one
        val lastSelectorIsTail = (selectors.lastOrNull() is TailSegmentSelector)
        if (tailsCount == 1 && !lastSelectorIsTail) {
            throw IllegalArgumentException("Tail selector must be in the last position: $routePath")
        }

        // If there is an OptionalParamSegmentSelector, it must be the last one
        val lastSelectorIsOptionalParam = (selectors.lastOrNull() is OptionalParamSegmentSelector)
        if (optionalParamsCount == 1 && !lastSelectorIsOptionalParam) {
            throw IllegalArgumentException("Optional selector must be in the last position: $routePath")
        }

        // All parameters must have unique names
        val allParamsNames = selectors.filterIsInstance<ParamSegmentSelector>().map { it.parameterName }.toSet()
        allParamsNames.forEach { paramName ->
            val count = selectors.filterIsInstance<ParamSegmentSelector>().count { it.parameterName == paramName }
            if (count > 1) {
                throw IllegalArgumentException("Parameter $paramName is duplicated")
            }
        }
    }

    internal fun checkAllRoutes(allRoutes: List<OneRouteMatcher>) {
        val allRootRoutes = allRoutes.filter { it.route.path == ROOT_ROUTE_PATH }
        if (allRootRoutes.size > 1) {
            throw RoutingConfigurationException("There are more than one root router defined: $allRootRoutes")
        }

        // Check for duplicated routes with the same HTTP method
        allRoutes
            .groupBy { it.route.path } // group by it path
            .filter { it.value.size > 1 } // and check only those with more than one
            .forEach {
                val path = it.key
                val matchers = it.value
                val alreadyRegisteredMethods = mutableSetOf<HttpMethod>()

                matchers.forEach { matcher ->
                    val duplicatedMethods = matcher.route.allowedHttpMethods intersect alreadyRegisteredMethods
                    if (duplicatedMethods.isNotEmpty()) {
                        throw RoutingConfigurationException(
                            "Route $path has more than one handler for the same HTTP method: $duplicatedMethods"
                        )
                    }
                    alreadyRegisteredMethods.addAll(matcher.route.allowedHttpMethods)
                }
            }
    }

    internal fun matchRequestRouteSelectors(
        request: HttpServletRequest, selectors: List<Selector>
    ): RouteMatchResult {
        val pathSegments = parsePathToSegments(request.requestURI)

        // almost all match() calls will fail, it is better not to create map in advance
        var parameters: MutableMap<String, String>? = null
        var segmentIndex = 0

        // try each selector one by one and evaluate
        // Every selector has to match with appropriate segment from path
        // If one or more selectors don't match - all matching will fail
        //
        // For example, we have static route /a/b/c. All selectors of this route are static selectors:
        // 		[Static('a'), Static('b'), Static('c')]
        // Ok, now we try to match this url: /a/b/d.
        // 		First selector ('a') – matches, second ('b') - matches, third ('c') – fails.
        for (selectorIndex in selectors.indices) {
            val selector = selectors[selectorIndex]
            val matchResult = selector.evaluate(request, pathSegments, segmentIndex)

            // Selector doesn't match – stop any further evaluation
            if (matchResult is EvaluationResult.Failure) {
                return RouteMatchResult.Failure
            }

            if (matchResult is EvaluationResult.Success) {
                // Selector may consume 0, 1 or more segments from url
                segmentIndex += matchResult.segmentIncrement
                // Collect named params, if any
                if (matchResult.parameters.isNotEmpty()) {
                    if (parameters == null) {
                        parameters = matchResult.parameters.toMutableMap()
                    } else {
                        parameters += matchResult.parameters
                    }
                }
            }
        }

        // Consumed segments aren't equal to all segments
        if (segmentIndex != pathSegments.size) {
            return RouteMatchResult.Failure
        }

        // All segments were consumed
        return if (parameters == null) {
            RouteMatchResult.MATCH_WITH_EMPTY_PARAMETERS
        } else {
            RouteMatchResult.Match(parameters)
        }
    }

    sealed class RouteMatchResult {
        data class Match(val parameters: Map<String, String>) : RouteMatchResult()
        data object Failure : RouteMatchResult()

        companion object {
            val MATCH_WITH_EMPTY_PARAMETERS = Match(emptyMap())
        }
    }
}

private fun String.surroundedBy(prefix: String, suffix: String): Boolean {
    return this.startsWith(prefix) && this.endsWith(suffix)
}
