package kotlet.metrics

import kotlet.InstallOrder
import kotlet.Interceptor
import kotlet.Routing

/**
 * Kotlet metrics builder
 */
object Metrics {
    /**
     * Create a new [Interceptor] that collects metrics using the provided [MetricsCollector].
     */
    fun interceptor(collector: MetricsCollector): Interceptor {
        return MetricsInterceptor(collector)
    }
}

/**
 * Install metrics collection as global routing interceptor
 */
fun Routing.installMetrics(
    /**
     * Implementation of [MetricsCollector] to collect metrics
     */
    metricsCollector: MetricsCollector,

    /**
     * Order of the interceptor in the chain
     */
    order: Int = InstallOrder.LAST,
) {
    val interceptor = Metrics.interceptor(metricsCollector)
    install(interceptor, order = order)
}
