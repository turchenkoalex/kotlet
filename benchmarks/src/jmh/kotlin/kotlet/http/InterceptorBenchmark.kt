package kotlet.http

import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

private val EMPTY_CALL = object : HttpCall {
    override val httpMethod = HttpMethod.GET
    override val routePath: String = "/"
    override val rawRequest: HttpServletRequest = mockk()
    override val rawResponse: HttpServletResponse = mockk()
    override val parameters: Map<String, String> = emptyMap()
}

object SimpleInterceptor : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        return call
    }

    override fun aroundCall(call: HttpCall, next: Handler) {
        next(call)
    }

    override fun afterCall(call: HttpCall): HttpCall {
        return call
    }
}

@State(Scope.Benchmark)
open class InterceptorBenchmark {
    private val listOfInterceptors =
        listOf(SimpleInterceptor, SimpleInterceptor, SimpleInterceptor, SimpleInterceptor, SimpleInterceptor)
    private val interceptedHandler = createChainHandler(listOfInterceptors, ::handler)

    @Benchmark
    fun testRecursive() {
        return recursiveApplyInterceptors(listOfInterceptors, EMPTY_CALL, ::handler)
    }

    @Benchmark
    fun testInterceptedHandler() {
        return interceptedHandler(EMPTY_CALL)
    }

    private fun handler(call: HttpCall) {
        // do nothing
    }
}

private tailrec fun recursiveApplyInterceptors(interceptors: List<Interceptor>, request: HttpCall, handler: Handler) {
    if (interceptors.isEmpty()) {
        handler(request)
    } else {
        val topInterceptors = interceptors.subList(0, interceptors.size - 1)
        val deepestInterceptor = interceptors.last()
        return recursiveApplyInterceptors(topInterceptors, request) { call ->
            val interceptedCall = deepestInterceptor.beforeCall(call)
            deepestInterceptor.aroundCall(interceptedCall, handler)
            deepestInterceptor.afterCall(interceptedCall)
        }
    }
}

private fun createChainHandler(interceptors: List<Interceptor>, handler: Handler): Handler {
    return interceptors.asReversed().fold(handler) { next: Handler, interceptor: Interceptor ->
        { call ->
            val interceptedCall = interceptor.beforeCall(call)
            interceptor.aroundCall(interceptedCall, next)
            interceptor.afterCall(interceptedCall)
        }
    }
}