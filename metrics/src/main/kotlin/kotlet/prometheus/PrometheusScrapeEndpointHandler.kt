package kotlet.prometheus

import io.prometheus.metrics.config.PrometheusProperties
import io.prometheus.metrics.exporter.common.PrometheusScrapeHandler
import io.prometheus.metrics.exporter.servlet.jakarta.HttpExchangeAdapter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import kotlet.Handler
import kotlet.HttpCall

/**
 * Handler for Prometheus scrape endpoint.
 */
internal class PrometheusScrapeEndpointHandler(
    config: PrometheusProperties,
    registry: PrometheusRegistry
) : Handler {
    private val scrapeHandler = PrometheusScrapeHandler(config, registry)

    override fun invoke(call: HttpCall) {
        scrapeHandler.handleRequest(HttpExchangeAdapter(call.rawRequest, call.rawResponse))
    }
}
