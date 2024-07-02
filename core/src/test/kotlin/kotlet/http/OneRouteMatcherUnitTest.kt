package kotlet.http

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.http.RouteHelpers.RouteMatchResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class OneRouteMatcherUnitTest {

    @Test
    fun testStaticMatch() {
        val route = Route("/first/second", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        val matcher = OneRouteMatcher(route, selectors)
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"

        every { request.requestURI } returns "/first/second"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertTrue(it.parameters.isEmpty())
        }

        every { request.requestURI } returns "/first"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))

        every { request.requestURI } returns "/first/second/third"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))
    }

    @Test
    fun testRouteParamMatch() {
        val route = Route("/first/{userId}/second/{fileId}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        val matcher = OneRouteMatcher(route, selectors)
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"

        every { request.requestURI } returns "/first/USER/second/FILE"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertEquals(mapOf("userId" to "USER", "fileId" to "FILE"), it.parameters)
        }

        every { request.requestURI } returns "/first"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))

        every { request.requestURI } returns "/first/USER/second"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))
    }

    @Test
    fun testOptionalRouteParamMatch() {
        val route = Route("/first/{userId}/second/{fileId?}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        val matcher = OneRouteMatcher(route, selectors)
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"

        // Optional param exists
        every { request.requestURI } returns "/first/USER/second/FILE"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertEquals(mapOf("userId" to "USER", "fileId" to "FILE"), it.parameters)
        }

        // Optional param doesn't exist
        every { request.requestURI } returns "/first/USER/second"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertEquals(mapOf("userId" to "USER"), it.parameters)
        }

        every { request.requestURI } returns "/first"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))
    }

    @Test
    fun testWildcardMatch_MiddlePosition() {
        val route = Route("/first/*/second", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        val matcher = OneRouteMatcher(route, selectors)
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"

        every { request.requestURI } returns "/first/bugaga/second"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertTrue(it.parameters.isEmpty())
        }

        every { request.requestURI } returns "/first/second"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))

        every { request.requestURI } returns "/first/second/third"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))
    }

    @Test
    fun testWildcardMatch_TailPosition() {
        val route = Route("/first/*", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        val matcher = OneRouteMatcher(route, selectors)
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"

        every { request.requestURI } returns "/first/bugaga"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertTrue(it.parameters.isEmpty())
        }

        every { request.requestURI } returns "/first"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))

        every { request.requestURI } returns "/first/bugaga/bugaga"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))
    }

    @Test
    fun testTailMatch() {
        val route = Route("/first/{...}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        val matcher = OneRouteMatcher(route, selectors)
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"

        every { request.requestURI } returns "/first"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertTrue(it.parameters.isEmpty())
        }

        every { request.requestURI } returns "/first/bugaga"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertTrue(it.parameters.isEmpty())
        }

        every { request.requestURI } returns "/first/bugaga/bugaga"
        matcher.match(request).also {
            assertIs<RouteMatchResult.Match>(it)
            assertTrue(it.parameters.isEmpty())
        }

        every { request.requestURI } returns "/"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))

        every { request.requestURI } returns "/second"
        assertIs<RouteMatchResult.Failure>(matcher.match(request))
    }

}
