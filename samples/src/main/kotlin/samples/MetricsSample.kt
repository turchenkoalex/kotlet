package samples

import io.prometheus.metrics.model.registry.PrometheusRegistry
import jetty.startJettyServer
import kotlet.Kotlet
import kotlet.metrics.installMetrics
import kotlet.metrics.installMetricsScrape
import kotlet.prometheus.PrometheusMetricsCollector

fun main() {
    val kotletMetrics = PrometheusMetricsCollector(PrometheusRegistry.defaultRegistry)

    val routing = Kotlet.routing {
        installMetrics(kotletMetrics)
        installMetricsScrape {
            path = "/prometheus"
        }

        get("/", { call ->
            call.rawResponse.contentType = "text/html"
            call.respondText(
                "Open <a href=\"http://localhost:8080/prometheus\">http://localhost:8080/prometheus</a> " +
                    "to see the metrics"
            )
        })
    }

    startJettyServer(
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}
