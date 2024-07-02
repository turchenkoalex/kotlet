package kotlet.metrics

import kotlet.http.HttpCall
import kotlet.http.Interceptor

internal class MetricsInterceptor(
    private val metricsCollector: MetricsCollector
) : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        metricsCollector.startRequest(call)
        return call
    }

    override fun afterCall(call: HttpCall): HttpCall {
        metricsCollector.endRequest(call)
        return call
    }
}
