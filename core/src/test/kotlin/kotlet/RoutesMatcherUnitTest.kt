package kotlet

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.Kotlet.routing
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

internal class RoutesMatcherUnitTest {

    @Test
    fun findRoute_Static() {
        // simple test – longer static path should always win
        val routes = routing {
            get("/a/b/c") {}
            get("/a/b") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/a/b", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_StaticAndWildcard() {
        // static path should always win if match is ideal
        val routes = routing {
            get("/a/*/c") {}
            get("/a/b/c") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/e/c")) { route, params, allRoutes ->
            assertEquals("/a/*/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_StaticAndParam() {
        // static path should always win if match is ideal
        val routes = routing {
            get("/a/{id}/c") {}
            get("/a/b/c") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/e/c")) { route, params, allRoutes ->
            assertEquals("/a/{id}/c", route.path, "All routes: $allRoutes")
            assertEquals(1, params.size, "All routes: $allRoutes")
            assertEquals(mapOf("id" to "e"), params, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_StaticAndParamAndWildcard() {
        // static path should always win if match is ideal, next – parametrized, next – wildcard
        val routes = routing {
            get("/a/*/c") {}
            get("/a/{id}/c") {}
            get("/a/b/c") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/e/c")) { route, params, allRoutes ->
            assertEquals("/a/{id}/c", route.path, "All routes: $allRoutes")
            assertEquals(1, params.size, "All routes: $allRoutes")
            assertEquals(mapOf("id" to "e"), params, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_StaticAndTail() {
        // static path should always win if match is ideal
        val routes = routing {
            get("/a/b/{...}") {}
            get("/a/b/c") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c/d")) { route, params, allRoutes ->
            assertEquals("/a/b/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/a/b/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_WildcardAndTail() {
        // wildcard should win if there are only 3 segments
        val routes = routing {
            get("/a/b/{...}") {}
            get("/a/b/*") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/*", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/a/b/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c/d")) { route, params, allRoutes ->
            assertEquals("/a/b/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_ParamAndOptionalParam() {
        // required param should win if there are only 3 segments
        val routes = routing {
            get("/a/b/{id}") {}
            get("/a/b/{id?}") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/{id}", route.path, "All routes: $allRoutes")
            assertEquals(1, params.size, "All routes: $allRoutes")
            assertEquals(mapOf("id" to "c"), params, "All routes: $allRoutes")
        }
        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/a/b/{id?}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }


    @Test
    fun findRoute_TwoTails() {
        // longer tail should win
        val routes = routing {
            get("/a/b/{...}") {}
            get("/a/b/c/{...}") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c/d")) { route, params, allRoutes ->
            assertEquals("/a/b/c/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/a/b/{...}", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_RootRouteAsDefault_RootInstalled() {
        // longer tail should win
        val routes = routing {
            get {}
            get("/a") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a")) { route, params, allRoutes ->
            assertEquals("/a", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/")) { route, params, allRoutes ->
            assertEquals("/", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/b")) { route, params, allRoutes ->
            assertEquals("/", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun findRoute_RootRouteAsDefault_RootNotInstalled() {
        // longer tail should win
        val routes = routing {
            get("/a") {}
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a")) { route, params, allRoutes ->
            assertEquals("/a", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        assertFails {
            routes.findMatchForShuffledRoutes(mockRequest("/")) { _, _, allRoutes ->
                fail("No root route: $allRoutes")
            }
        }

        assertFails {
            routes.findMatchForShuffledRoutes(mockRequest("/b")) { route, params, allRoutes ->
                assertEquals("/", route.path, "All routes: $allRoutes")
                assertEquals(0, params.size, "All routes: $allRoutes")
            }
        }

        assertFails {
            routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
                assertEquals("/", route.path, "All routes: $allRoutes")
                assertEquals(0, params.size, "All routes: $allRoutes")
            }
        }
    }

    @Test
    fun findRoute_WithRouteBlock() {
        // simple test – longer static path should always win
        val routes = routing {
            route("/a") {
                route("/b") {
                    get {}
                    put("/c") {}
                }

                route("/e") {
                    get {}
                    post("/h") {}
                }
            }

            route("/c/d") {
                patch {}
                head("e") {}
            }

            route("/f") {
                trace {}
                route("g") {
                    delete("h") {}
                }
            }
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b")) { route, params, allRoutes ->
            assertEquals("/a/b", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/b/c")) { route, params, allRoutes ->
            assertEquals("/a/b/c", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/c/d")) { route, params, allRoutes ->
            assertEquals("/c/d", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/c/d/e")) { route, params, allRoutes ->
            assertEquals("/c/d/e", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/f")) { route, params, allRoutes ->
            assertEquals("/f", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/f/g/h")) { route, params, allRoutes ->
            assertEquals("/f/g/h", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/e")) { route, params, allRoutes ->
            assertEquals("/a/e", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }

        routes.findMatchForShuffledRoutes(mockRequest("/a/e/h")) { route, params, allRoutes ->
            assertEquals("/a/e/h", route.path, "All routes: $allRoutes")
            assertEquals(0, params.size, "All routes: $allRoutes")
        }
    }

    @Test
    fun `toString test`() {
        val routes = routing {
            route("/a/b") {
                get {}
                post {}
            }
            get("/a/b/c") {}
        }

        val matcher = AllRoutesMatcher(routes.getAllRoutes())

        val expected = "RoutesMatcher(routes=[[GET] => /a/b/c, [GET, POST] => /a/b])"
        assertEquals(expected, matcher.toString())
    }

    @Test
    fun `fails when duplicates found in different routing`() {
        val routing1 = routing {
            get("/foo") { }
        }

        val routing2 = routing {
            get("/foo") { }
        }

        val error = assertThrows(RoutingConfigurationException::class.java) {
            AllRoutesMatcher(routing1.getAllRoutes() + routing2.getAllRoutes())
        }

        assertEquals("Route /foo has more than one handler for the same HTTP method: [GET]", error.message)
    }

    @Test
    fun `only one root route allowed`() {
        val routing1 = routing {
            get { }
        }

        val routing2 = routing {
            post { }
        }

        val error = assertThrows(RoutingConfigurationException::class.java) {
            AllRoutesMatcher(routing1.getAllRoutes() + routing2.getAllRoutes())
        }

        assertContains(error.message!!, "There are more than one root router defined")
    }

    @Test
    fun `fails when duplicates found in one route`() {
        val error = assertThrows(RoutingConfigurationException::class.java) {
            routing {
                get("/foo") { }
                get("/foo", routeOptions { }) { }
            }
        }

        assertEquals("Route /foo has more than one handler for the same HTTP method: [GET]", error.message)
    }

    private fun Routing.findMatchForShuffledRoutes(
        request: HttpServletRequest,
        testCodeBlock: (route: Route, params: Map<String, String>, allRoutes: List<Route>) -> Unit
    ) {
        // Ideally, findRoute() method mustn't depend on routes order, however, it is better to test this.
        // This is why this method try to shuffle routes list a few times (15) and try to find the route
        // for the same request in this shuffled routes.
        //
        // P.S. There is no magic behind 15. The number should be big enough, but not bigger. So – 15 :)
        (1..15).forEach { _ ->
            val shuffledRoutes = getAllRoutes().shuffled()
            val allRoutesMatcher = AllRoutesMatcher(shuffledRoutes)
            val (route, params) = allRoutesMatcher.findRoute(request)
                ?: fail("Route for '${request.requestURI}' not found. Known routes: $shuffledRoutes")

            testCodeBlock(route, params, shuffledRoutes)
        }
    }

    private fun mockRequest(path: String): HttpServletRequest {
        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns path
        return request
    }
}
