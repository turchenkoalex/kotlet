package kotlet.metrics

import io.prometheus.metrics.model.registry.PrometheusRegistry
import kotlet.HttpMethod
import kotlet.Kotlet
import kotlet.mocks.Mocks
import kotlet.prometheus.PrometheusMetricsCollector
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class MetricsScrapeUnitTest {
    @Test
    fun testEndpoint() {
        val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
        val collector = PrometheusMetricsCollector(PrometheusRegistry.defaultRegistry, clock)

        val route = Kotlet.routing {
            installMetrics(collector)

            get("/", {})

            installMetricsScrape {
                path = "/metrics"
            }
        }

        val servlet = Kotlet.servlet(listOf(route))

        val getCall = Mocks.httpCall(
            method = HttpMethod.GET,
            routePath = "/"
        )
        servlet.service(getCall.rawRequest, getCall.rawResponse)

        val metricsCall = Mocks.httpCall(
            method = HttpMethod.GET,
            routePath = "/metrics"
        )
        servlet.service(metricsCall.rawRequest, metricsCall.rawResponse)

        val body = metricsCall.responseData.toString(Charsets.UTF_8)
        val expected = """
            # HELP kotlet_http_requests_total Total number of HTTP requests
            # TYPE kotlet_http_requests_total counter
            kotlet_http_requests_total{method="GET",path="/",status="200"} 1.0
            # HELP kotlet_http_requests_duration_seconds Duration of HTTP requests in seconds
            # TYPE kotlet_http_requests_duration_seconds summary
            kotlet_http_requests_duration_seconds{method="GET",path="/",status="200",quantile="0.5"} 0.0
            kotlet_http_requests_duration_seconds{method="GET",path="/",status="200",quantile="0.9"} 0.0
            kotlet_http_requests_duration_seconds{method="GET",path="/",status="200",quantile="0.95"} 0.0
            kotlet_http_requests_duration_seconds{method="GET",path="/",status="200",quantile="0.99"} 0.0
            kotlet_http_requests_duration_seconds_count{method="GET",path="/",status="200"} 1
            kotlet_http_requests_duration_seconds_sum{method="GET",path="/",status="200"} 0.0

            """.trimIndent()

        assertEquals(expected, body)
    }

}
