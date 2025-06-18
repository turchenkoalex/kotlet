package kotlet

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class RoutingUnitTest {
    @Test
    fun `registered routes contains info about all interceptors`() {
        val routing = Kotlet.routing {
            install(GlobalInterceptor)
            get("/only_global") {}
            use(LocalInterceptor) {
                get("/global_and_local") {}
            }
        }

        val routes = routing.registeredRoutes

        val globalRoute = routes.first { it.path == "/only_global" }
        val globalAndLocalRoute = routes.first { it.path == "/global_and_local" }

        assertEquals(globalRoute.interceptors, listOf(GlobalInterceptor))
        assertEquals(globalAndLocalRoute.interceptors, listOf(GlobalInterceptor, LocalInterceptor))
    }

}

private data object GlobalInterceptor : Interceptor

private data object LocalInterceptor : Interceptor
