# Security Best Practices

This guide covers security considerations when building applications with Kotlet.

## Table of Contents

1. [Input Validation](#input-validation)
2. [Request Body Size Limits](#request-body-size-limits)
3. [Authentication with JWT](#authentication-with-jwt)
4. [Error Responses](#error-responses)
5. [CORS](#cors)
6. [Security Headers](#security-headers)
7. [Common Vulnerabilities](#common-vulnerabilities)
8. [Checklist](#checklist)

## Input Validation

Kotlet gives you raw access to the request, so validation is your responsibility. Validate everything that comes from
the client: path parameters, query parameters, headers and the body.

### Path Parameters

Path parameters are always strings. Parse them with the `*OrNull` functions:

```kotlin
get("/users/{id}") { call ->
    val userId = call.parameters["id"]?.toLongOrNull()
    if (userId == null || userId <= 0) {
        call.respondError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID")
        return@get
    }

    call.respondJson(userRepository.findById(userId))
}
```

### Query Parameters

Query parameters are available through the raw servlet request:

```kotlin
get("/search") { call ->
    val query = call.rawRequest.getParameter("q")?.trim()
    if (query.isNullOrEmpty() || query.length > 100) {
        call.respondError(HttpServletResponse.SC_BAD_REQUEST, "Query must be 1..100 characters")
        return@get
    }

    call.respondJson(searchService.search(query))
}
```

## Request Body Size Limits

**Always limit the request body size.** Reading an unbounded body into memory lets a single client exhaust the heap.

### Size-Limited Helpers

The core module provides extension functions on `HttpCall` that enforce a limit (10 MB by default):

| Function                                | Returns       |
|-----------------------------------------|---------------|
| `receiveText(maxSize, charset = UTF_8)` | `String`      |
| `receiveBytes(maxSize)`                 | `ByteArray`   |
| `receiveLimitedStream(maxSize)`         | `InputStream` |

When the body is larger than `maxSize`, a `RequestBodyTooLargeException` is thrown. If the request has a
`Content-Length` header larger than the limit, the exception is thrown before anything is read. Uncaught, the exception
is handled by `ErrorsHandler.requestBodyTooLarge`, which responds with `413 Payload Too Large` by default
(see [ERROR-HANDLING.md](ERROR-HANDLING.md)).

```kotlin
post("/api/notes") { call ->
    val text = call.receiveText(maxSize = 64 * 1024) // 64 KB, larger bodies get 413
    notes.save(text)
    call.status = HttpServletResponse.SC_CREATED
}

post("/api/avatar") { call ->
    val image = call.receiveBytes(maxSize = 5 * 1024 * 1024) // 5 MB
    avatars.save(image)
    call.status = HttpServletResponse.SC_NO_CONTENT
}
```

Catch the exception if you want a custom response for a single route:

```kotlin
post("/api/upload") { call ->
    try {
        val data = call.receiveBytes(maxSize = 1024 * 1024)
        storage.save(data)
    } catch (e: RequestBodyTooLargeException) {
        call.respondError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Max upload size is 1 MB")
    }
}
```

### Streaming

For large uploads, don't buffer the body in memory. Stream it with a limit:

```kotlin
post("/api/files") { call ->
    call.receiveLimitedStream(maxSize = 100 * 1024 * 1024).use { stream ->
        fileStorage.save(stream) // throws RequestBodyTooLargeException after 100 MB
    }
    call.status = HttpServletResponse.SC_CREATED
}
```

### JSON Bodies

`receiveBody<T>()` from the [JSON](../json/README.md) extension reads the raw input stream **without a limit**.
To limit JSON bodies, deserialize from a limited stream:

```kotlin
post("/api/users") { call ->
    val request = Serializer.deserialize(call.receiveLimitedStream(maxSize = 64 * 1024), CreateUserRequest::class.java)
    // ...
}
```

### Servlet Container Limits

Configure limits in the servlet container too. They protect parts of the request Kotlet doesn't read, like headers.
For example, in Jetty:

```kotlin
val httpConfiguration = HttpConfiguration().apply {
    requestHeaderSize = 8 * 1024 // 8 KB for the request line and headers
}
```

## Authentication with JWT

The [JWT](../jwt/README.md) extension verifies the `Authorization: Bearer <token>` header and stores the identity in
the request. **It doesn't reject requests by itself**: without a valid token `call.identity<T>()` returns `null`, and it
is up to you to respond with `401`.

```kotlin
data class UserIdentity(val userId: Long, val roles: List<String>)

val verifier = JWT.require(Algorithm.HMAC256(secret))
    .withIssuer("your-app")
    .withAudience("your-api")
    .build()

fun buildIdentity(decodedJWT: DecodedJWT): UserIdentity {
    return UserIdentity(
        userId = decodedJWT.getClaim("userId").asLong(),
        roles = decodedJWT.getClaim("roles").asList(String::class.java).orEmpty(),
    )
}

val routing = Kotlet.routing {
    installJWTAuthentication(verifier, ::buildIdentity)

    get("/api/me") { call ->
        val identity = call.identity<UserIdentity>()
        if (identity == null) {
            call.respondError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            return@get
        }
        call.respondText("Hello, ${identity.userId}")
    }
}
```

To protect a group of routes, add an interceptor that stops the call when there is no identity:

```kotlin
object RequireAuthentication : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        if (call.identity<UserIdentity>() == null) {
            call.respondError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            return // next is not called
        }
        next(call)
    }
}

val routing = Kotlet.routing {
    installJWTAuthentication(verifier, ::buildIdentity) // global, runs before route-specific interceptors

    get("/public") { call -> call.respondText("Hello, anonymous") }

    use(RequireAuthentication) {
        get("/api/profile") { call -> /* identity is guaranteed here */ }
        get("/api/orders") { call -> /* ... */ }
    }
}
```

Failed verifications (expired, malformed or wrongly signed tokens) are logged at `FINE` level by the
`kotlet.jwt.JWTAuthenticationInterceptor` logger. They are not logged at higher levels on purpose: every client can send
an invalid token, and logging each one with a stack trace would let anyone flood your logs.

### JWT Recommendations

1. **Use strong secrets**: at least 256 random bits for HMAC, or prefer asymmetric algorithms (RS256, ES256) so that
   services verifying tokens don't hold the signing key.
2. **Validate claims**: always set the expected issuer and audience in the verifier.
3. **Use short-lived tokens** (minutes, not days) with refresh tokens.
4. **Don't put secrets in tokens**: JWT payloads are only base64-encoded, not encrypted.
5. **Use HTTPS only**: a bearer token is a password for its lifetime.

## Error Responses

Never expose exception messages or stack traces to clients. The default `ErrorsHandler` responds with a generic
`500 Internal server error` and logs the exception on the server. Keep it this way in custom handlers:

```kotlin
// Good: the details go to the log, the client gets a generic message
object SafeErrorsHandler : ErrorsHandler {
    private val log = Logger.getLogger(SafeErrorsHandler::class.java.name)

    override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
        log.log(Level.SEVERE, "Internal error: ${request.method} ${request.requestURI}", e)
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error")
    }
}

// Bad: leaks SQL, file paths, internal hostnames...
call.respondText("Error: ${e.message}\n${e.stackTraceToString()}")
```

The [kotlet-jetty](../jetty/README.md) extension also disables the `Server` and `X-Powered-By` response headers.

## CORS

Allow only the origins you trust with the [CORS](../cors/README.md) extension:

```kotlin
val routing = Kotlet.routing {
    // Production: only your frontend
    installCORS(CORS.allowOrigin("https://your-app.com"))

    // Fine-grained rules
    installCORS(object : CorsRules {
        override fun getResponse(call: HttpCall): CorsResponse {
            return CorsResponse.headers(
                allowOrigin = "https://your-app.com",
                allowMethods = listOf("GET", "POST"),
                allowHeaders = listOf("Content-Type", "Authorization"),
                maxAge = Duration.ofHours(1),
            )
        }
    })
}
```

`CORS.allowAll` (`Access-Control-Allow-Origin: *`) is meant for development only.

## Security Headers

Set security headers in `beforeCall`: by the time `afterCall` runs, the handler may already have committed the
response, and headers set then are silently ignored.

```kotlin
object SecurityHeadersInterceptor : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        call.rawResponse.apply {
            setHeader("X-Content-Type-Options", "nosniff")
            setHeader("X-Frame-Options", "DENY")
            setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            setHeader("Content-Security-Policy", "default-src 'self'")
        }
        return call
    }
}

val routing = Kotlet.routing {
    install(SecurityHeadersInterceptor, order = InstallOrder.FIRST)
}
```

### Content Type Validation

```kotlin
post("/api/data") { call ->
    val contentType = call.rawRequest.contentType
    if (contentType == null || !contentType.startsWith("application/json")) {
        call.respondError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Expected application/json")
        return@post
    }
    // ...
}
```

## Common Vulnerabilities

### SQL Injection

Always use parameterized queries:

```kotlin
// Good
connection.prepareStatement("SELECT * FROM users WHERE id = ?").use { statement ->
    statement.setLong(1, userId)
    // ...
}

// Bad: VULNERABLE
connection.createStatement().executeQuery("SELECT * FROM users WHERE id = ${call.parameters["id"]}")
```

### XSS

Prefer JSON responses. `respondText` writes the string as is and doesn't set a content type, so escape user input
when you return HTML:

```kotlin
get("/profile") { call ->
    val name = getUserName().escapeHtml() // use an HTML escaping library
    call.rawResponse.contentType = "text/html; charset=utf-8"
    call.respondText("<h1>Welcome $name</h1>")
}
```

### Path Traversal

Never build file paths from user input without checking the result:

```kotlin
val baseDir: Path = Path.of("/safe/directory").toRealPath()

get("/files/{filename}") { call ->
    val filename = call.parameters["filename"].orEmpty()
    val file = baseDir.resolve(filename).normalize()

    if (!file.startsWith(baseDir) || !Files.isRegularFile(file)) {
        call.respondError(HttpServletResponse.SC_NOT_FOUND, "File not found")
        return@get
    }

    Files.newInputStream(file).use { input ->
        call.respondBytes(input)
    }
}
```

### Command Injection

Don't run shell commands with user input. If you have to start a process, pass arguments as a list (no shell) and
validate them against a strict allowlist:

```kotlin
// Bad: VULNERABLE
Runtime.getRuntime().exec("convert $filename output.pdf")

// Better
require(filename.matches(Regex("^[a-zA-Z0-9_.-]+$"))) { "Invalid filename" }
ProcessBuilder("convert", filename, "output.pdf").start()
```

## Checklist

- [ ] All client input (path, query, headers, body) is validated
- [ ] Request bodies are read with a size limit (`receiveText`, `receiveBytes`, `receiveLimitedStream`)
- [ ] Protected routes reject requests without identity (`401`)
- [ ] JWT verifier checks issuer and audience, tokens are short-lived
- [ ] Error responses don't leak internal details
- [ ] CORS allows only trusted origins in production
- [ ] Security headers are set in `beforeCall`
- [ ] HTTPS is enforced in production
- [ ] SQL queries are parameterized
- [ ] HTML output is escaped
- [ ] File paths are checked against traversal
- [ ] Sensitive data (tokens, passwords) is not logged
- [ ] Rate limiting is in place for public endpoints

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JSON Web Token Best Current Practices (RFC 8725)](https://datatracker.ietf.org/doc/html/rfc8725)
- [Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
