package kotlet.prometheus

import io.mockk.Matcher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.prometheus.metrics.core.metrics.Metric
import io.prometheus.metrics.model.registry.Collector
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot
import io.prometheus.metrics.model.snapshots.Quantile
import io.prometheus.metrics.model.snapshots.SummarySnapshot.SummaryDataPointSnapshot
import kotlet.HttpMethod
import kotlet.Kotlet
import kotlet.metrics.installMetrics
import kotlet.mocks.Mocks
import java.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class PrometheusMetricsCollectorUnitTest {
    @Test
    fun `prometheus register expected metrics`() {
        val registry = mockk<PrometheusRegistry> {
            every { register(any<Collector>()) } just Runs
        }

        val prometheusMetricsCollector = PrometheusMetricsCollector(registry)

        Kotlet.routing {
            installMetrics(prometheusMetricsCollector)
        }

        Kotlet.routing {
            installMetrics(prometheusMetricsCollector)
        }

        verify(exactly = 1) {
            registry.register(match<Metric>(MetricMatch("kotlet_http_requests")))
            registry.register(match<Metric>(MetricMatch("kotlet_http_requests_duration_seconds")))
        }
    }

    @Test
    fun `test kotlet_http_requests metric values`() {
        val registry = PrometheusRegistry()
        val prometheusMetricsCollector = PrometheusMetricsCollector(registry)

        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        prometheusMetricsCollector.startRequest(call)
        prometheusMetricsCollector.endRequest(call)

        val httpCallCounter = registry.scrape { name -> name == "kotlet_http_requests" }.single()
        val point = httpCallCounter.dataPoints.single() as CounterDataPointSnapshot

        assertEquals(1.0, point.value)
        assertEquals(3, point.labels.size())
        assertEquals("GET", point.labels.get("method"))
        assertEquals("/", point.labels.get("path"))
        assertEquals("200", point.labels.get("status"))
    }

    @Test
    fun `test kotlet_http_requests_duration_seconds metric values`() {
        val registry = PrometheusRegistry()
        val clock = mockk<Clock>()
        val prometheusMetricsCollector = PrometheusMetricsCollector(registry, clock)

        val call = Mocks.httpCall(
            method = HttpMethod.POST,
            routePath = "/test"
        )

        val now = System.currentTimeMillis()
        every { clock.millis() } returns now andThen (now + 5000L)

        prometheusMetricsCollector.startRequest(call)
        call.status = 400
        prometheusMetricsCollector.endRequest(call)

        val httpCallCounter = registry.scrape { name -> name == "kotlet_http_requests_duration_seconds" }.single()
        val point = httpCallCounter.dataPoints.single() as SummaryDataPointSnapshot

        assertEquals(1, point.count)
        assertEquals(3, point.labels.size())
        assertEquals(5.0, point.sum)
        assertEquals(0.5, point.quantiles.get(0).quantile)
        assertEquals(5.0, point.quantiles.get(0).value)
        assertEquals(0.9, point.quantiles.get(1).quantile)
        assertEquals(5.0, point.quantiles.get(1).value)
        assertEquals(0.95, point.quantiles.get(2).quantile)
        assertEquals(5.0, point.quantiles.get(2).value)
        assertEquals(0.99, point.quantiles.get(3).quantile)
        assertEquals(5.0, point.quantiles.get(3).value)
        assertEquals("POST", point.labels.get("method"))
        assertEquals("/test", point.labels.get("path"))
        assertEquals("400", point.labels.get("status"))
    }

    data class MetricMatch(val name: String) : Matcher<Metric> {
        override fun match(arg: Metric?): Boolean {
            if (arg == null) {
                return false
            }

            return arg.prometheusName == name
        }
    }
}
