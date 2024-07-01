package kotlet.prometheus

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Summary
import io.prometheus.metrics.model.registry.PrometheusRegistry
import jakarta.servlet.AsyncEvent
import jakarta.servlet.AsyncListener
import kotlet.HttpCall
import kotlet.metrics.MetricsCollector

private const val START_TIME_ATTRIBUTE = "kotlet.prometheus.started_at"

class PrometheusMetricsCollector(
    registry: PrometheusRegistry
) : MetricsCollector {
    override fun startRequest(call: HttpCall) {
        call.rawRequest.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime())
    }

    override fun endRequest(call: HttpCall) {
        if (call.rawRequest.isAsyncStarted) {
            call.rawRequest.asyncContext.addListener(RequestCounterAsyncListener(call, counter, summary))
        } else {
            measureRequest(call, counter, summary)
        }
    }

    private val counter = Counter.builder()
        .name("kotlet_http_requests_total")
        .help("Total number of HTTP requests")
        .labelNames("method", "path", "status")
        .register(registry)

    private val summary = Summary.builder()
        .name("kotlet_http_requests_duration_seconds")
        .help("Duration of HTTP requests in seconds")
        .unit(io.prometheus.metrics.model.snapshots.Unit.SECONDS)
        .quantile(0.5, 0.05)
        .quantile(0.9, 0.01)
        .quantile(0.95, 0.005)
        .quantile(0.99, 0.001)
        .labelNames("method", "path", "status")
        .register(registry)
}

private data class RequestCounterAsyncListener(
    private val call: HttpCall,
    private val counter: Counter,
    private val summary: Summary,
) : AsyncListener {
    override fun onComplete(event: AsyncEvent) {
        measureRequest(call, counter, summary)
    }

    override fun onTimeout(event: AsyncEvent) {
        measureRequest(call, counter, summary)
    }

    override fun onError(event: AsyncEvent) {
        measureRequest(call, counter, summary)
    }

    override fun onStartAsync(event: AsyncEvent) {
    }
}

private fun measureRequest(call: HttpCall, counter: Counter, summary: Summary) {
    val statusCode = call.statusCode()
    counter.labelValues(call.httpMethod.name, call.routePath, statusCode).inc()
    val startTime = call.rawRequest.getAttribute(START_TIME_ATTRIBUTE) as? Long
    if (startTime != null) {
        val duration = (System.nanoTime() - startTime) / 1_000_000_000.0
        summary.labelValues(call.httpMethod.name, call.routePath, statusCode).observe(duration)
        call.rawRequest.removeAttribute(START_TIME_ATTRIBUTE)
    }
}

private fun HttpCall.statusCode(): String {
    return when (status) {
        in 100..599 -> status.toString()
        else -> "unknown"
    }
}
