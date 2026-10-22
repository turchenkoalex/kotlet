# Performance

This guide explains what affects performance in Kotlet applications and how to tune it.

## Table of Contents

1. [How Routing Works](#how-routing-works)
2. [Interceptors](#interceptors)
3. [Servlet Container](#servlet-container)
4. [Request and Response Bodies](#request-and-response-bodies)
5. [Monitoring](#monitoring)
6. [Benchmarking](#benchmarking)
7. [Checklist](#checklist)

## How Routing Works

All work that can be done once is done when the servlet (or handler) is created:

* Routes are split into segment selectors, validated, and sorted: routes with more segments and more specific segments
  go first. A static segment is more specific than a parameter, a parameter more than an optional parameter, an optional
  parameter more than a wildcard, and routes ending with a tail `{...}` go last.
* Interceptor chains are built for every route and method.

On each request Kotlet goes through the sorted routes and takes the first one that matches the path, then picks the
handler by the HTTP method.

What follows from this:

* **Declaration order doesn't matter.** `/users/me` always wins over `/users/{id}` for the path `/users/me`, wherever
  it is declared.
* **Methods of one path share one route.** Declaring `get("/users/{id}")` and `delete("/users/{id}")` in different
  blocks is the same as declaring them together, there is no extra cost.
* **The matching cost grows linearly with the number of routes.** For typical services (tens to hundreds of routes)
  it is negligible compared to I/O. If you have thousands of routes, measure it with the `RoutingBenchmark`
  (see [Benchmarking](#benchmarking)).
* **Configuration errors fail fast.** Duplicate path and method pairs and invalid paths throw
  `RoutingConfigurationException` on startup, not on the request.

See [ROUTES.md](ROUTES.md#route-matching-order) for the path syntax and matching order.

## Interceptors

Every interceptor adds a `beforeCall` / `aroundCall` / `afterCall` round to the request, so its cost is the cost of the
code inside it.

* **Scope interceptors to where they are needed.** `install` applies an interceptor to every route of the routing,
  `use { }` and `configure { withInterceptor(...) }` apply it only to specific routes:

  ```kotlin
  val routing = Kotlet.routing {
      installMetrics(metricsCollector) // needed everywhere

      get("/health") { call -> call.respondText("OK") }

      useJWTAuthentication(verifier, ::buildIdentity) { // only for the API
          get("/api/profile") { call -> /* ... */ }
      }
  }
  ```

* **Order global interceptors deliberately.** Use `order = InstallOrder.FIRST` for interceptors that can stop a request
  early (rate limiting, cheap header checks), so expensive ones don't run for rejected requests:

  ```kotlin
  install(RateLimitInterceptor, order = InstallOrder.FIRST)
  installJWTAuthentication(verifier) // InstallOrder.LAST by default
  ```

* **Don't do per-request work that can be done once.** Load configuration, compile regexes, and create clients in the
  interceptor constructor, not in `beforeCall`.

See [INTERCEPTORS.md](INTERCEPTORS.md) for details.

## Servlet Container

Kotlet adds very little on top of the servlet container, so the container configuration usually matters more.

### kotlet-jetty Defaults

The [kotlet-jetty](../jetty/README.md) extension creates a Jetty server with:

| Setting       | Default                                                            | Property             |
|---------------|--------------------------------------------------------------------|----------------------|
| Port          | `80`                                                               | `port`               |
| Thread pool   | `VirtualThreadPool` (a virtual thread per request)                 | `threadPool`         |
| Compression   | gzip for `GET`, `POST`, `PUT`, `DELETE` and `OPTIONS` responses    | `compressionEnabled` |
| Protocols     | HTTP/1.1 and HTTP/2 cleartext (h2c) on the same port               | -                    |

```kotlin
val server = jetty(routing) {
    port = 8080
    // a platform thread pool instead of virtual threads, if you need it
    threadPool = QueuedThreadPool(200)
}
server.start()
```

With virtual threads, blocking calls in handlers (JDBC, HTTP clients, file I/O) are fine: a blocked virtual thread
doesn't hold a platform thread. Limit concurrency to downstream resources (a database connection pool) instead of
limiting request threads.

> [!WARNING]
> Don't hand the response to another thread (`executor.submit { call.respondJson(...) }`) and return from the handler:
> the container completes the response as soon as the handler returns. Do blocking work in the handler itself.

### Other Containers

If you use `Kotlet.servlet(...)` with your own container, configure the thread pool, compression and HTTP/2 there.
Compression usually helps JSON APIs a lot, and should be enabled either in the container or in a reverse proxy.

## Request and Response Bodies

* **Read bodies with a limit.** `receiveText`, `receiveBytes` and `receiveLimitedStream` stop reading after `maxSize`
  bytes and reject requests with a larger `Content-Length` without reading them. See [SECURITY.md](SECURITY.md).
* **Stream large bodies.** `receiveLimitedStream` gives you an `InputStream` to process the upload without buffering it.
  `respondBytes(inputStream)` copies the stream to the response without loading it into memory:

  ```kotlin
  get("/files/{name}") { call ->
      val file = storage.resolve(call.parameters["name"].orEmpty())
      call.rawResponse.setContentLengthLong(Files.size(file))
      Files.newInputStream(file).use { input ->
          call.respondBytes(input)
      }
  }
  ```

  Remember to close the streams you open, `respondBytes` doesn't close the input stream.
* **Use the JSON extension instead of building JSON by hand.** `respondJson` serializes `@Serializable` classes
  directly to the response stream with kotlinx.serialization and caches the serializer for every class.
* **Use HTTP caching for rarely changing resources** (`Cache-Control`, `ETag` with `If-None-Match` and `304 Not Modified`).

## Monitoring

Measure before tuning. Kotlet has extensions for both metrics and traces:

```kotlin
val routing = Kotlet.routing {
    // kotlet_http_requests_total and kotlet_http_requests_duration_seconds
    installMetrics(PrometheusMetricsCollector(PrometheusRegistry.defaultRegistry))
    installMetricsScrape() // GET /metrics

    // a server span for every request
    installTracing(GlobalOpenTelemetry.get())
}
```

See [Metrics](../metrics/README.md) and [Tracing](../tracing/README.md) for the required dependencies.

## Benchmarking

The `benchmarks` module contains JMH benchmarks for the routing (`RoutingBenchmark`, 1000 routes) and the interceptor
chain (`InterceptorBenchmark`):

```bash
./gradlew :benchmarks:jmh
```

Results are written to `benchmarks/build/reports/jmh/results.json`. Numbers depend on hardware and JVM, so compare
results on the same machine before and after a change.

For end-to-end numbers, load test the running application with a tool like [wrk](https://github.com/wg/wrk),
[k6](https://k6.io/) or [Gatling](https://gatling.io/):

```bash
wrk -t4 -c100 -d30s http://localhost:8080/api/users/123
```

## Checklist

- [ ] Metrics and tracing are enabled, and you know your latency percentiles
- [ ] Interceptors are scoped to the routes that need them, cheap rejecting interceptors go first
- [ ] Request bodies are read with a size limit, large bodies are streamed
- [ ] Opened streams are closed
- [ ] Response compression is enabled (kotlet-jetty, container or reverse proxy)
- [ ] Blocking work runs in the handler, concurrency is limited at downstream resources
- [ ] Changes in hot paths are checked with benchmarks or load tests
