package kotlet.metrics

import kotlet.http.Interceptor
import kotlet.http.Routing

object Metrics {
    fun interceptor(collector: MetricsCollector): Interceptor {
        return MetricsInterceptor(collector)
    }
}

/**
 * Install metrics collection as global routing interceptor
 */
fun Routing.installMetrics(metricsCollector: MetricsCollector) {
    val interceptor = Metrics.interceptor(metricsCollector)
    install(interceptor)
}