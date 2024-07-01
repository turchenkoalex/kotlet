package kotlet

import kotlet.selector.OptionalParamSegmentSelector
import kotlet.selector.ParamSegmentSelector
import kotlet.selector.StaticSegmentSelector
import kotlet.selector.TailSegmentSelector
import kotlet.selector.WildcardSegmentSelector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class RouteHelpersUnitTest {

    @Test
    fun testStaticPath() {
        val route = Route("/first/second", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(2, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("second", it.staticSegment)
        }
    }

    @Test
    fun testWildcardPath() {
        val route = Route("/first/*/second/*", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(4, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<WildcardSegmentSelector>(it)
        }

        selectors[2].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("second", it.staticSegment)
        }

        selectors[3].also {
            assertIs<WildcardSegmentSelector>(it)
        }
    }

    @Test
    fun testParametrizedPath() {
        val route = Route("/first/{userId}/second", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(3, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<ParamSegmentSelector>(it)
            assertEquals("userId", it.parameterName)
        }

        selectors[2].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("second", it.staticSegment)
        }
    }

    @Test
    fun testOptionalParametrizedPath() {
        val route = Route("/first/second/{userId?}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(3, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("second", it.staticSegment)
        }

        selectors[2].also {
            assertIs<OptionalParamSegmentSelector>(it)
            assertEquals("userId", it.parameterName)
        }
    }

    @Test
    fun testTailPath() {
        val route = Route("/first/{...}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(2, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<TailSegmentSelector>(it)
        }
    }

    @Test
    fun testComplexPathWithTail() {
        val route = Route("/first/{id}/second/*/{...}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(5, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<ParamSegmentSelector>(it)
            assertEquals("id", it.parameterName)
        }

        selectors[2].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("second", it.staticSegment)
        }

        selectors[3].also {
            assertIs<WildcardSegmentSelector>(it)
        }

        selectors[4].also {
            assertIs<TailSegmentSelector>(it)
        }
    }

    @Test
    fun testComplexPathWithOptional() {
        val route = Route("/first/{id}/second/*/{otherId?}", emptyList(), emptyMap())
        val selectors = RouteHelpers.prepareSelectorsList(route)

        assertEquals(5, selectors.size)

        selectors[0].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("first", it.staticSegment)
        }

        selectors[1].also {
            assertIs<ParamSegmentSelector>(it)
            assertEquals("id", it.parameterName)
        }

        selectors[2].also {
            assertIs<StaticSegmentSelector>(it)
            assertEquals("second", it.staticSegment)
        }

        selectors[3].also {
            assertIs<WildcardSegmentSelector>(it)
        }

        selectors[4].also {
            assertIs<OptionalParamSegmentSelector>(it)
            assertEquals("otherId", it.parameterName)
        }
    }

    @Test
    fun testCheckSelectors() {
        // check double Tails
        assertFailsWith(IllegalArgumentException::class) {
            val route = Route("/first/{...}/{...}", emptyList(), emptyMap())
            val selectors = RouteHelpers.prepareSelectorsList(route)
            RouteHelpers.checkSelectorsList(route.path, selectors)
        }

        // check double optionals
        assertFailsWith(IllegalArgumentException::class) {
            val route = Route("/first/{a?}/{b?}", emptyList(), emptyMap())
            val selectors = RouteHelpers.prepareSelectorsList(route)
            RouteHelpers.checkSelectorsList(route.path, selectors)
        }

        // check Tail in the last position
        assertFailsWith(IllegalArgumentException::class) {
            val route = Route("/first/{...}/second", emptyList(), emptyMap())
            val selectors = RouteHelpers.prepareSelectorsList(route)
            RouteHelpers.checkSelectorsList(route.path, selectors)
        }

        // check optional in the last position
        assertFailsWith(IllegalArgumentException::class) {
            val route = Route("/first/{a?}/second", emptyList(), emptyMap())
            val selectors = RouteHelpers.prepareSelectorsList(route)
            RouteHelpers.checkSelectorsList(route.path, selectors)
        }

        // check repeated param names
        assertFailsWith(IllegalArgumentException::class) {
            val route = Route("/first/{id}/second/{id}", emptyList(), emptyMap())
            val selectors = RouteHelpers.prepareSelectorsList(route)
            RouteHelpers.checkSelectorsList(route.path, selectors)
        }
    }

}
