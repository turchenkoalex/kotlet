package kotlet.prometheus

import io.prometheus.metrics.model.registry.PrometheusRegistry
import kotlet.Routing
import kotlet.metrics.installMetrics

/**
 * Install Prometheus metrics collection as global routing interceptor
 */
fun Routing.installPrometheus(registry: PrometheusRegistry) {
    val metricsCollector = PrometheusMetricsCollector(registry)
    installMetrics(metricsCollector)
}