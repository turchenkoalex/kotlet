# Security Best Practices

This guide covers security considerations when building applications with Kotlet.

## Table of Contents

1. [Input Validation](#input-validation)
2. [Request Body Size Limits](#request-body-size-limits)
3. [JWT Authentication](#jwt-authentication)
4. [Error Handling](#error-handling)
5. [CORS Configuration](#cors-configuration)
6. [Content Security](#content-security)
7. [Common Vulnerabilities](#common-vulnerabilities)

## Input Validation

Always validate user input at system boundaries. Kotlet provides raw access to request data, so validation is your responsibility.

### Validate Path Parameters

```kotlin
route("/users/{id}") {
    get {
        val id = parameters["id"] ?: run {
            status = 400
            respondText("Missing user ID")
            return@get
        }

        // Validate format
        val userId = id.toLongOrNull() ?: run {
            status = 400
            respondText("Invalid user ID format")
            return@get
        }

        // Validate range
        if (userId <= 0 || userId > Long.MAX_VALUE) {
            status = 400
            respondText("User ID out of range")
            return@get
        }

        // Safe to use
        val user = userRepository.findById(userId)
        // ...
    }
}
```

### Validate Query Parameters

```kotlin
route("/search") {
    get {
        val query = rawRequest.getParameter("q")

        // Check for null/empty
        if (query.isNullOrBlank()) {
            status = 400
            respondText("Search query required")
            return@get
        }

        // Limit length
        if (query.length > 100) {
            status = 400
            respondText("Query too long (max 100 characters)")
            return@get
        }

        // Sanitize for your use case
        val sanitized = query.trim()

        // Safe to use
        val results = searchService.search(sanitized)
        // ...
    }
}
```

## Request Body Size Limits

**Critical:** Always limit request body size to prevent denial-of-service attacks.

### Using Built-in Size-Limited Helpers

Kotlet provides extension functions for safe body reading:

```kotlin
route("/api/upload") {
    post {
        try {
            // Read text with 1 MB limit
            val body = receiveText(maxSize = 1024 * 1024)

            // Process body
            val data = parseJson(body)
            respondText("Success")
        } catch (e: RequestBodyTooLargeException) {
            status = 413 // Payload Too Large
            respondText("Request body exceeds 1 MB limit")
        }
    }
}
```

### Reading Binary Data

```kotlin
route("/api/file") {
    post {
        try {
            // Read binary data with 5 MB limit
            val fileData = receiveBytes(maxSize = 5 * 1024 * 1024)

            // Save file
            saveFile(fileData)
            respondText("File uploaded successfully")
        } catch (e: RequestBodyTooLargeException) {
            status = 413
            respondText("File size exceeds 5 MB limit")
        }
    }
}
```

### Streaming Large Files

For streaming, use the size-limited input stream:

```kotlin
route("/api/upload-stream") {
    post {
        try {
            receiveLimitedStream(maxSize = 100 * 1024 * 1024).use { stream ->
                // Stream directly to storage
                fileStorage.save(stream, filename)
                respondText("Upload complete")
            }
        } catch (e: RequestBodyTooLargeException) {
            status = 413
            respondText("File exceeds 100 MB limit")
        }
    }
}
```

### Setting Servlet Container Limits

Also configure limits in your servlet container (Jetty, Tomcat, etc.):

```kotlin
// Jetty example
val server = Server(8080)
val connector = ServerConnector(server)
connector.setMaxRequestHeaderSize(32 * 1024) // 32 KB
connector.setRequestBufferSize(64 * 1024) // 64 KB
server.addConnector(connector)
```

## JWT Authentication

### Secure JWT Configuration

```kotlin
val verifier = JWT.require(Algorithm.HMAC256(secretKey))
    .withIssuer("your-app")
    .withAudience("your-api")
    .build()

routing {
    install(
        jwtAuthentication(verifier) { decodedJWT ->
            UserIdentity(
                userId = decodedJWT.getClaim("userId").asLong(),
                roles = decodedJWT.getClaim("roles").asList(String::class.java)
            )
        }
    )
}
```

### Monitor JWT Failures

JWT verification failures are automatically logged at WARNING level. Monitor these logs for suspicious activity:

```
WARNING: JWT verification failed: The Token has expired on 2026-01-25T10:30:00Z.
```

Excessive JWT failures may indicate:
- Replay attacks
- Token theft attempts
- Clock synchronization issues
- Legitimate token expiration

### Best Practices

1. **Use Strong Secrets**: Use cryptographically secure random keys (at least 256 bits for HMAC)
2. **Set Short Expiration**: Use short-lived tokens (15-60 minutes) with refresh tokens
3. **Validate All Claims**: Check issuer, audience, and custom claims
4. **Use HTTPS Only**: Never transmit JWTs over unencrypted connections
5. **Don't Store Sensitive Data**: JWTs are base64-encoded, not encrypted

```kotlin
// Good: Short-lived token
val token = JWT.create()
    .withIssuer("your-app")
    .withAudience("your-api")
    .withClaim("userId", userId)
    .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
    .sign(algorithm)

// Bad: Long-lived token with sensitive data
val token = JWT.create()
    .withClaim("password", userPassword) // NEVER DO THIS
    .withExpiresAt(Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)) // 1 year
    .sign(algorithm)
```

## Error Handling

### Don't Leak Internal Details

Never expose internal error details, stack traces, or system information to users.

```kotlin
// Good: Generic error message
route("/api/data") {
    get {
        try {
            val data = database.query()
            respondJson(data)
        } catch (e: Exception) {
            // Exception is logged automatically by ErrorsHandler
            status = 500
            respondText("An error occurred")
        }
    }
}

// Bad: Exposes internals
route("/api/data") {
    get {
        try {
            val data = database.query()
            respondJson(data)
        } catch (e: Exception) {
            status = 500
            respondText("Error: ${e.message}\n${e.stackTraceToString()}") // NEVER DO THIS
        }
    }
}
```

### Custom Error Handler

Implement a custom error handler for better control:

```kotlin
class SecureErrorsHandler : ErrorsHandler {
    override fun internalServerError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: Exception
    ) {
        // Logging is handled by default implementation
        log.error("Internal error processing ${request.method} ${request.requestURI}", e)

        // Send generic error to client
        response.status = 500
        response.contentType = "application/json"
        response.writer.write("""{"error":"Internal server error"}""")
    }
}

Kotlet.routing {
    errorsHandler = SecureErrorsHandler()
}
```

## CORS Configuration

Configure CORS carefully to prevent unauthorized access:

```kotlin
routing {
    // Restrictive CORS
    install(
        cors {
            allowOrigin("https://your-app.com") // Specific origin only
            allowMethods(HttpMethod.GET, HttpMethod.POST)
            allowHeaders("Content-Type", "Authorization")
            maxAge(3600)
            // allowCredentials = false by default
        }
    )
}

// For development only
routing {
    install(
        cors {
            allowOrigin("*") // WARNING: Only use in development!
            allowMethods(*HttpMethod.values())
            allowHeaders("*")
        }
    )
}
```

## Content Security

### Set Security Headers

Always set appropriate security headers:

```kotlin
routing {
    install(object : Interceptor {
        override fun afterCall(call: HttpCall): HttpCall {
            call.rawResponse.apply {
                setHeader("X-Content-Type-Options", "nosniff")
                setHeader("X-Frame-Options", "DENY")
                setHeader("X-XSS-Protection", "1; mode=block")
                setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                setHeader("Content-Security-Policy", "default-src 'self'")
            }
            return call
        }
    })
}
```

### Content Type Validation

Validate content types for endpoints:

```kotlin
route("/api/data") {
    post {
        val contentType = rawRequest.contentType

        if (contentType == null || !contentType.startsWith("application/json")) {
            status = 415 // Unsupported Media Type
            respondText("Expected application/json")
            return@post
        }

        // Safe to process JSON
        val body = receiveText()
        // ...
    }
}
```

## Common Vulnerabilities

### SQL Injection Prevention

Always use parameterized queries:

```kotlin
// Good: Parameterized query
val userId = parameters["id"]
val user = database.execute(
    "SELECT * FROM users WHERE id = ?",
    userId
)

// Bad: String concatenation
val userId = parameters["id"]
val user = database.execute(
    "SELECT * FROM users WHERE id = $userId" // VULNERABLE!
)
```

### XSS Prevention

Escape HTML output and set Content-Type correctly:

```kotlin
// Good: JSON response (auto-escaped by serialization)
route("/api/user/{id}") {
    get {
        val user = findUser(parameters["id"])
        respondJson(user) // Safe: JSON serialization escapes
    }
}

// If returning HTML, escape user input
route("/profile") {
    get {
        val name = getUserName()
        val escaped = name.replace("<", "&lt;").replace(">", "&gt;")
        rawResponse.contentType = "text/html"
        respondText("<h1>Welcome $escaped</h1>")
    }
}
```

### Path Traversal Prevention

Validate file paths:

```kotlin
route("/files/{filename}") {
    get {
        val filename = parameters["filename"] ?: run {
            status = 400
            respondText("Filename required")
            return@get
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            status = 400
            respondText("Invalid filename")
            return@get
        }

        // Safe to use
        val file = File("/safe/directory/$filename")
        if (file.exists() && file.isFile) {
            respondBytes(file.inputStream())
        } else {
            status = 404
            respondText("File not found")
        }
    }
}
```

### Command Injection Prevention

Never pass user input directly to shell commands:

```kotlin
// Bad: Command injection vulnerability
route("/convert") {
    post {
        val filename = parameters["file"]
        Runtime.getRuntime().exec("convert $filename output.pdf") // VULNERABLE!
    }
}

// Good: Validate and sanitize, or avoid shell execution
route("/convert") {
    post {
        val filename = parameters["file"] ?: run {
            status = 400
            respondText("Filename required")
            return@post
        }

        // Validate filename
        if (!filename.matches(Regex("^[a-zA-Z0-9_.-]+$"))) {
            status = 400
            respondText("Invalid filename")
            return@post
        }

        // Use ProcessBuilder with arguments (safer)
        val process = ProcessBuilder("convert", filename, "output.pdf").start()
        process.waitFor()
    }
}
```

## Security Checklist

- [ ] Input validation on all user-provided data
- [ ] Request body size limits configured
- [ ] JWT tokens use strong secrets and short expiration
- [ ] Error messages don't leak internal details
- [ ] CORS configured with specific origins (not `*` in production)
- [ ] Security headers set (CSP, X-Frame-Options, etc.)
- [ ] HTTPS enforced in production
- [ ] SQL queries use parameterized statements
- [ ] HTML output is escaped
- [ ] File paths validated against traversal
- [ ] No shell command injection vulnerabilities
- [ ] Sensitive data not logged
- [ ] Authentication required on protected endpoints
- [ ] Rate limiting implemented for public endpoints

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
- [Servlet Security](https://jakarta.ee/specifications/servlet/6.0/)
