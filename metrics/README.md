# Metrics

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-metrics:0.79.0")
```

> [!IMPORTANT]
> For using prometheus metrics you must add to your dependencies
> [io.prometheus:prometheus-metrics-core](https://mvnrepository.com/artifact/io.prometheus/prometheus-metrics-core)
> library.

## Configuration

You can use the `installMetrics` method to add metrics to your routes. The method takes a `MetricsCollector` object
as a parameter.

```kotlin
val kotletMetrics =
    PrometheusMetricsCollector(PrometheusRegistry.defaultRegistry) // this collector can be reused for multiple routes

Kotlet.routing {
    installMetrics(kotletMetrics) // Now all requests of this routing will be measured
    get("/hello") { call ->
        call.respondText("Hello, World!")
    }
}
```

## Available metrics:

| Metric Name                             | Description                                            |
|-----------------------------------------|--------------------------------------------------------|
| `kotlet_http_requests_total`            | Total number of HTTP requests.                         |
| `kotlet_http_requests_duration_seconds` | Duration of HTTP requests in seconds with percentiles. |

## Custom metrics

Also, you can define your own metrics collector by implementing the `kotlet.metrics.MetricsCollector` interface, like in
the example below:

```kotlin
class MyMetricsCollector : MetricsCollector {
    override fun startRequest(call: HttpCall) {
        // start timers
    }
    override fun endRequest(call: HttpCall) {
        // measure request and cleanup
    }
}

Kotlet.routing {
    installMetrics(MyMetricsCollector())
    // your routes
}
```

## Metrics scrape endpoint

> [!IMPORTANT]
> For using scrape metrics endpoint you must add to your dependencies
> [prometheus-metrics-exporter-servlet-jakarta](https://mvnrepository.com/artifact/io.prometheus/prometheus-metrics-exporter-servlet-jakarta)
> library.


To expose metrics for Prometheus, you can use the `installMetricsScrape` method. Add it to your kotlet router like this:

```kotlin
val appRouting = Kotlet.routing {
    installMetrics(kotletMetrics) // Now all requests of this routing will be measured
    get("/hello") { call ->
        call.respondText("Hello, World!")
    }
}

val auxRouting = Kotlet.routing {
    installMetricsScrape {
        path = "/metrics"
    }
}

// Combine routings
Kotlet.servlet(
    routings = listOf(appRouting, auxRouting)
)
```

After it metrics will be available at `/metrics` endpoint in OpenMetrics format.
