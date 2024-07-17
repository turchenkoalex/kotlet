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
import kotlet.Kotlet
import kotlet.metrics.installMetrics
import kotlin.test.Test

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

    data class MetricMatch(val name: String) : Matcher<Metric> {
        override fun match(arg: Metric?): Boolean {
            if (arg == null) {
                return false
            }

            return arg.prometheusName == name
        }
    }
}
