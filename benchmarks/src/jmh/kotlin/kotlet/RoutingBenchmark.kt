package kotlet

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import kotlet.Kotlet.routing
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class RoutingBenchmark {
    private val routing = routing {
        repeat(1000) {
            get("/test/$it") {
                // do nothing
            }
        }
    }

    private val matcher = RoutesMatcher.create(listOf(routing))

    private val request = mockk<HttpServletRequest>(relaxed = true) {
        every { pathInfo } returns "/test/XXXXXX"
    }

    @Benchmark
    fun testManyRoutes(): Pair<Route, Map<String, String>>? {
        val found = matcher.findRoute(request)
        return found
    }

}
