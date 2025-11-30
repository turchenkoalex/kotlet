package kotlet.swagger.ui

import kotlet.HttpMethod
import kotlet.Kotlet
import kotlet.RegisteredRoute
import kotlin.test.Test
import kotlin.test.assertTrue

class SwaggerUIUnitTest {
    @Test
    fun `swagger resources test`() {
        val router = Kotlet.routing {
            serveSwaggerUI {
                path = "/myswagger"
                openAPIPath = "/openapi"
            }
        }

        val routes = router.registeredRoutes
        assertHasRoute(routes, HttpMethod.GET, "/myswagger/index.html")
        assertHasRoute(routes, HttpMethod.GET, "/myswagger/swagger-initializer.js")
    }

    private fun assertHasRoute(routes: List<RegisteredRoute>, method: HttpMethod, path: String) {
        assertTrue(routes.any { it.method == method && it.path == path })
    }
}
