package kotlet

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
    private val request = DummyRequest("/test/XXXXXX")

    @Benchmark
    fun testManyRoutes(): Pair<Route, Map<String, String>>? {
        return matcher.findRoute(request)
    }

}
