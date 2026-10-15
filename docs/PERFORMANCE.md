# Performance Tuning Guide

This guide covers performance optimization techniques for Kotlet applications.

## Table of Contents

1. [Benchmarking](#benchmarking)
2. [Servlet Container Tuning](#servlet-container-tuning)
3. [Route Configuration](#route-configuration)
4. [Interceptor Performance](#interceptor-performance)
5. [Request Processing](#request-processing)
6. [Response Optimization](#response-optimization)
7. [Monitoring](#monitoring)
8. [Common Issues](#common-issues)

## Benchmarking

### Using JMH

Kotlet includes JMH benchmarks in the `benchmarks` module:

```bash
# Run all benchmarks
./gradlew :benchmarks:jmh

# Run specific benchmark
./gradlew :benchmarks:jmh -Pjmh.includes=RoutingBenchmark
```

### Creating Custom Benchmarks

```kotlin
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class CustomBenchmark {

    private lateinit var routing: Routing

    @Setup
    fun setup() {
        routing = Kotlet.routing {
            route("/api/users/{id}") {
                get {
                    respondText("User ${parameters["id"]}")
                }
            }
        }
    }

    @Benchmark
    fun benchmarkRouteMatching(blackhole: Blackhole) {
        val request = MockHttpServletRequest(
            method = "GET",
            requestURI = "/api/users/123"
        )
        val response = MockHttpServletResponse()

        routing.service(request, response)
        blackhole.consume(response)
    }
}
```

### Load Testing

Use tools like Apache Bench, wrk, or Gatling:

```bash
# Apache Bench
ab -n 10000 -c 100 http://localhost:8080/api/users/123

# wrk
wrk -t 4 -c 100 -d 30s http://localhost:8080/api/users/123

# Gatling
mvn gatling:test -Dgatling.simulationClass=com.example.UserSimulation
```

## Servlet Container Tuning

### Jetty Configuration

```kotlin
val server = Server()

// Thread pool configuration
val threadPool = QueuedThreadPool().apply {
    minThreads = 10
    maxThreads = 200
    idleTimeout = 60000
}
server.threadPool = threadPool

// Connector configuration
val connector = ServerConnector(server).apply {
    port = 8080
    acceptQueueSize = 100
    idleTimeout = 30000
    soLingerTime = -1
}
server.addConnector(connector)

// Handler configuration
val handler = ServletContextHandler().apply {
    contextPath = "/"
    addServlet(ServletHolder(RoutingServlet(routing)), "/*")
}
server.handler = handler

server.start()
```

### Connection Pool Sizing

Calculate thread pool size based on the formula:
```
threads = number_of_cores * (1 + wait_time / service_time)
```

For I/O-bound applications:
```kotlin
val cores = Runtime.getRuntime().availableProcessors()
val threadPool = QueuedThreadPool().apply {
    minThreads = cores
    maxThreads = cores * 4  // For I/O-bound with 3:1 wait:service ratio
}
```

For CPU-bound applications:
```kotlin
val cores = Runtime.getRuntime().availableProcessors()
val threadPool = QueuedThreadPool().apply {
    minThreads = cores
    maxThreads = cores + 1  // CPU-bound: cores + 1
}
```

### HTTP/2 Support

Enable HTTP/2 for better performance:

```kotlin
val http2 = HTTP2ServerConnectionFactory(HttpConfiguration())
val connector = ServerConnector(server, http2).apply {
    port = 8080
}
```

## Route Configuration

### Route Ordering

Routes are matched in order. Put specific routes before generic ones:

```kotlin
routing {
    // Good: Specific routes first
    route("/api/users/me") {
        get { /* ... */ }
    }

    route("/api/users/{id}") {
        get { /* ... */ }
    }

    // Bad: Generic route first (would match /api/users/me)
    route("/api/users/{id}") {
        get { /* ... */ }
    }

    route("/api/users/me") {
        get { /* ... */ }  // Never reached!
    }
}
```

### Route Complexity

Simpler routes match faster. Route matching performance (fastest to slowest):

1. Static segments: `/api/users` ⚡
2. Parameters: `/api/users/{id}` ⚡
3. Optional parameters: `/api/users/{id?}` 🔸
4. Wildcards: `/api/*/users` 🔸
5. Tail selectors: `/files/{path...}` 🐌

### Minimize Route Count

Consolidate routes when possible:

```kotlin
// Good: Single route with method branching
route("/api/users/{id}") {
    get {
        // Fetch user
    }
    put {
        // Update user
    }
    delete {
        // Delete user
    }
}

// Less optimal: Multiple route definitions (still works, but more overhead)
route("/api/users/{id}") { get { /* ... */ } }
route("/api/users/{id}") { put { /* ... */ } }
route("/api/users/{id}") { delete { /* ... */ } }
```

## Interceptor Performance

### Interceptor Order Matters

Lightweight interceptors should run first:

```kotlin
routing {
    // Fast: Simple header check
    install(SecurityHeadersInterceptor(), order = InstallOrder.FIRST)

    // Medium: JWT parsing
    install(jwtAuthentication(...), order = InstallOrder.NORMAL)

    // Slow: Database lookup
    install(UserProfileInterceptor(), order = InstallOrder.LAST)
}
```

### Avoid Heavy Operations

Don't perform expensive operations in interceptors unless necessary:

```kotlin
// Bad: Database call in interceptor for every request
class HeavyInterceptor : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        val settings = database.loadSettings() // Expensive!
        call.rawRequest.setAttribute("settings", settings)
        return call
    }
}

// Good: Cache or lazy load
class OptimizedInterceptor : Interceptor {
    private val settings by lazy { database.loadSettings() }

    override fun beforeCall(call: HttpCall): HttpCall {
        call.rawRequest.setAttribute("settings", settings)
        return call
    }
}
```

### Conditional Interceptors

Skip interceptors when not needed:

```kotlin
class ConditionalAuthInterceptor(
    private val verifier: JWTVerifier
) : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        // Skip auth for public endpoints
        if (call.routePath.startsWith("/public/")) {
            return call
        }

        // Only verify JWT if Authorization header present
        val authHeader = call.rawRequest.getHeader("Authorization")
            ?: return call

        // ... verify JWT
        return call
    }
}
```

## Request Processing

### Lazy Parameter Extraction

Extract parameters only when needed:

```kotlin
route("/api/users/{id}") {
    get {
        // Bad: Extract even if not needed
        val id = parameters["id"]?.toLong()
        val format = rawRequest.getParameter("format")
        val fields = rawRequest.getParameter("fields")

        if (checkCache()) {
            respondCached() // Didn't need those params!
            return@get
        }

        // ... use params
    }
}

// Good: Extract when needed
route("/api/users/{id}") {
    get {
        if (checkCache()) {
            respondCached()
            return@get
        }

        // Only extract when cache miss
        val id = parameters["id"]?.toLong()
        // ... use params
    }
}
```

### Stream Large Responses

Use streaming for large responses instead of buffering:

```kotlin
route("/api/download/{file}") {
    get {
        val file = File("/data/${parameters["file"]}")

        rawResponse.contentType = "application/octet-stream"
        rawResponse.setContentLengthLong(file.length())

        // Stream directly (no buffering)
        file.inputStream().use { input ->
            rawResponse.outputStream.use { output ->
                input.copyTo(output, bufferSize = 8192)
            }
        }
    }
}
```

### Avoid Unnecessary String Operations

```kotlin
// Bad: Multiple string operations
route("/api/data") {
    get {
        val json = """
            {
                "status": "ok",
                "data": [${items.joinToString { toJson(it) }}]
            }
        """.trimIndent()
        respondText(json)
    }
}

// Good: Use proper JSON serialization
route("/api/data") {
    get {
        respondJson(mapOf(
            "status" to "ok",
            "data" to items
        ))
    }
}
```

## Response Optimization

### Enable Compression

Configure compression in your servlet container:

```kotlin
// Jetty example
val gzipHandler = GzipHandler().apply {
    includedMimeTypes = setOf(
        "text/html",
        "text/css",
        "text/javascript",
        "application/json",
        "application/javascript"
    )
    minGzipSize = 1024 // Only compress responses > 1KB
}
gzipHandler.handler = servletHandler
server.handler = gzipHandler
```

### Set Appropriate Cache Headers

```kotlin
route("/api/static/{resource}") {
    get {
        rawResponse.setHeader("Cache-Control", "public, max-age=3600")
        rawResponse.setHeader("ETag", generateETag(resource))

        // Check If-None-Match
        val ifNoneMatch = rawRequest.getHeader("If-None-Match")
        if (ifNoneMatch == etag) {
            status = 304 // Not Modified
            return@get
        }

        // Return resource
        respondBytes(loadResource(resource))
    }
}
```

### Response Size

Minimize response payload:

```kotlin
// Bad: Return entire object
route("/api/users/{id}") {
    get {
        val user = userRepository.findById(id)
        respondJson(user) // Includes internal fields
    }
}

// Good: Return only needed fields
route("/api/users/{id}") {
    get {
        val user = userRepository.findById(id)
        respondJson(UserDTO(
            id = user.id,
            name = user.name,
            email = user.email
            // Exclude internal fields
        ))
    }
}

// Better: Support field selection
route("/api/users/{id}") {
    get {
        val user = userRepository.findById(id)
        val fields = rawRequest.getParameter("fields")?.split(",") ?: defaultFields

        respondJson(user.toDTO(fields))
    }
}
```

## Monitoring

### OpenTelemetry Integration

```kotlin
dependencies {
    implementation("kotlet:tracing")
}

routing {
    install(
        tracing(
            serviceName = "my-service",
            endpoint = "http://localhost:4318/v1/traces"
        )
    )
}
```

### Prometheus Metrics

```kotlin
dependencies {
    implementation("kotlet:metrics")
}

routing {
    install(metrics())

    // Expose metrics endpoint
    route("/metrics") {
        get {
            rawResponse.contentType = "text/plain; version=0.0.4"
            respondText(metricsRegistry.scrape())
        }
    }
}
```

### Custom Metrics

```kotlin
class MetricsInterceptor(
    private val registry: MetricsRegistry
) : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        val start = System.nanoTime()

        try {
            next(call)
        } finally {
            val duration = System.nanoTime() - start
            registry.recordRequestDuration(
                path = call.routePath,
                method = call.httpMethod.name,
                status = call.status,
                durationNanos = duration
            )
        }
    }
}
```

### Health Checks

```kotlin
route("/health") {
    get {
        val checks = mapOf(
            "database" to checkDatabase(),
            "redis" to checkRedis(),
            "disk" to checkDiskSpace()
        )

        if (checks.values.all { it }) {
            status = 200
            respondJson(mapOf("status" to "UP", "checks" to checks))
        } else {
            status = 503
            respondJson(mapOf("status" to "DOWN", "checks" to checks))
        }
    }
}

route("/health/ready") {
    get {
        // Kubernetes readiness probe
        if (isReady()) {
            status = 200
            respondText("OK")
        } else {
            status = 503
            respondText("Not Ready")
        }
    }
}

route("/health/live") {
    get {
        // Kubernetes liveness probe
        status = 200
        respondText("OK")
    }
}
```

## Common Issues

### Issue: High Latency on First Request

**Cause:** JVM warmup and class loading

**Solution:**
- Use GraalVM native image for instant startup
- Implement warmup requests in tests
- Use tiered compilation: `-XX:TieredStopAtLevel=1` for faster startup

```kotlin
// Warmup utility
fun warmupRouting(routing: Routing) {
    val warmupPaths = listOf(
        "/api/users/1",
        "/api/data",
        "/health"
    )

    warmupPaths.forEach { path ->
        repeat(100) {
            val request = MockHttpServletRequest(method = "GET", requestURI = path)
            val response = MockHttpServletResponse()
            routing.service(request, response)
        }
    }
}
```

### Issue: Memory Leaks

**Cause:** Unclosed resources, growing caches

**Solution:**
- Always use `.use {}` for resources
- Implement bounded caches
- Monitor heap usage

```kotlin
// Bad: Resource leak
route("/api/file") {
    get {
        val stream = FileInputStream(file)
        respondBytes(stream) // Stream not closed!
    }
}

// Good: Automatic cleanup
route("/api/file") {
    get {
        FileInputStream(file).use { stream ->
            respondBytes(stream)
        }
    }
}
```

### Issue: Thread Pool Exhaustion

**Cause:** Blocking I/O on all threads

**Solution:**
- Use async I/O or separate thread pool for blocking operations
- Increase thread pool size
- Use virtual threads (Java 21+)

```kotlin
// Java 21 Virtual Threads
val executor = Executors.newVirtualThreadPerTaskExecutor()

route("/api/blocking") {
    get {
        executor.submit {
            val result = blockingDatabaseCall()
            respondJson(result)
        }
    }
}
```

### Issue: Slow JSON Serialization

**Cause:** Reflection-based serialization

**Solution:**
- Use kotlinx.serialization (compile-time)
- Configure Jackson properly
- Cache serialized responses

```kotlin
dependencies {
    implementation("kotlet:json") // Uses kotlinx.serialization
}

// kotlinx.serialization is faster than reflection-based JSON libraries
@Serializable
data class User(val id: Long, val name: String)

route("/api/users/{id}") {
    get {
        val user = findUser(id)
        respondJson(user) // Fast compile-time serialization
    }
}
```

## Performance Checklist

- [ ] JMH benchmarks created for critical paths
- [ ] Thread pool sized appropriately
- [ ] HTTP/2 enabled
- [ ] Routes ordered from specific to generic
- [ ] Lightweight interceptors run first
- [ ] Heavy operations cached or lazy-loaded
- [ ] Large responses streamed, not buffered
- [ ] Response compression enabled
- [ ] Cache headers set appropriately
- [ ] Field selection supported for large objects
- [ ] Metrics and monitoring in place
- [ ] Health check endpoints implemented
- [ ] Resources properly closed
- [ ] Warmup performed in production
- [ ] Load testing completed

## Benchmarking Results

Typical Kotlet performance (varies by hardware):

| Operation             | Throughput  | Latency (p99) |
|-----------------------|-------------|---------------|
| Static route match    | ~1M req/s   | ~1ms          |
| Parameter extraction  | ~800K req/s | ~1.5ms        |
| JSON serialization    | ~500K req/s | ~2ms          |
| JWT verification      | ~200K req/s | ~5ms          |
| With interceptors (3) | ~400K req/s | ~3ms          |

Run your own benchmarks for accurate numbers on your hardware and workload.
