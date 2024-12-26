package kotlet.metrics

import io.prometheus.metrics.config.PrometheusProperties
import io.prometheus.metrics.model.registry.PrometheusRegistry
import kotlet.Routing
import kotlet.prometheus.PrometheusScrapeEndpointHandler

/**
 * Install a scrape endpoint for Prometheus metrics.
 */
fun Routing.installMetricsScrape(configure: MetricsScrapeConfigBuilder.() -> Unit = {}) {
    val builder = MetricsScrapeConfigBuilder()
    builder.configure()
    val config = builder.build()

    get(config.path, PrometheusScrapeEndpointHandler(config.config, config.registry))
}

/**
 * Configuration for the scrape endpoint.
 */
internal data class MetricsScrapeConfig(
    val path: String,
    val registry: PrometheusRegistry,
    val config: PrometheusProperties
)

/**
 * Builder for [MetricsScrapeConfig].
 */
class MetricsScrapeConfigBuilder internal constructor() {
    /**
     * Path to the scrape endpoint
     * Default: /metrics
     */
    var path: String = "/metrics"

    /**
     * Prometheus registry to scrape metrics from
     * Default: [PrometheusRegistry.defaultRegistry]
     */
    var registry: PrometheusRegistry = PrometheusRegistry.defaultRegistry

    /**
     * Prometheus configuration
     * Default: [PrometheusProperties.get]
     */
    var config: PrometheusProperties = PrometheusProperties.get()

    internal fun build(): MetricsScrapeConfig {
        return MetricsScrapeConfig(
            path = path,
            registry = registry,
            config = config,
        )
    }
}
