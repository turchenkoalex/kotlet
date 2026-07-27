# Error Handling Guide

This guide explains how to properly handle errors in Kotlet applications.

## Table of Contents

1. [Default Error Handling](#default-error-handling)
2. [Custom Error Handlers](#custom-error-handlers)
3. [Route-Level Error Handling](#route-level-error-handling)
4. [Error Logging](#error-logging)
5. [Common Patterns](#common-patterns)
6. [Best Practices](#best-practices)

## Default Error Handling

Kotlet provides default error handling for common HTTP error scenarios:

### 404 Not Found

When no route matches the request:

```kotlin
routing {
    route("/api/users") {
        get {
            respondText("List users")
        }
    }
}

// GET /api/unknown -> 404 "Not found"
```

### 405 Method Not Allowed

When a route exists but the HTTP method is not configured:

```kotlin
routing {
    route("/api/users") {
        get {
            respondText("List users")
        }
    }
}

// POST /api/users -> 405 "Method not allowed"
```

### 500 Internal Server Error

When an unhandled exception occurs:

```kotlin
routing {
    route("/api/data") {
        get {
            throw IllegalStateException("Something went wrong")
        }
    }
}

// GET /api/data -> 500 "Internal server error"
// Exception is logged with full stack trace
```

## Custom Error Handlers

Implement the `ErrorsHandler` interface to customize error responses:

### Basic Custom Handler

```kotlin
class JsonErrorsHandler : ErrorsHandler {
    private val log = Logger.getLogger(JsonErrorsHandler::class.java.name)

    override fun routeNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 404
        response.contentType = "application/json"
        response.writer.write("""
            {
                "error": "Not Found",
                "message": "Route ${request.requestURI} not found",
                "status": 404
            }
        """.trimIndent())
    }

    override fun methodNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 405
        response.contentType = "application/json"
        response.writer.write("""
            {
                "error": "Method Not Allowed",
                "message": "${request.method} method not allowed for ${request.requestURI}",
                "status": 405
            }
        """.trimIndent())
    }

    override fun internalServerError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: Exception
    ) {
        // Log the exception (with stack trace)
        log.log(Level.SEVERE, "Internal server error", e)

        // Return generic error to client (don't leak internals)
        response.status = 500
        response.contentType = "application/json"
        response.writer.write("""
            {
                "error": "Internal Server Error",
                "message": "An unexpected error occurred",
                "status": 500
            }
        """.trimIndent())
    }
}

// Install the custom handler
Kotlet.routing {
    errorsHandler = JsonErrorsHandler()

    // Your routes...
}
```

### Development vs Production Handler

Different error details for development and production:

```kotlin
class EnvironmentAwareErrorHandler(
    private val isDevelopment: Boolean
) : ErrorsHandler {
    private val log = Logger.getLogger(javaClass.name)

    override fun internalServerError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: Exception
    ) {
        log.log(Level.SEVERE, "Internal server error", e)

        response.status = 500
        response.contentType = "application/json"

        if (isDevelopment) {
            // Include details in development
            response.writer.write("""
                {
                    "error": "Internal Server Error",
                    "message": "${e.message}",
                    "type": "${e.javaClass.simpleName}",
                    "stackTrace": ${e.stackTrace.take(10).map { it.toString() }.toJsonArray()}
                }
            """.trimIndent())
        } else {
            // Generic message in production
            response.writer.write("""
                {
                    "error": "Internal Server Error",
                    "message": "An unexpected error occurred"
                }
            """.trimIndent())
        }
    }
}

// Usage
val isDev = System.getenv("ENVIRONMENT") != "production"

Kotlet.routing {
    errorsHandler = EnvironmentAwareErrorHandler(isDev)
}
```

## Route-Level Error Handling

Handle errors within specific routes using try-catch:

### Basic Try-Catch

```kotlin
route("/api/users/{id}") {
    get {
        try {
            val userId = parameters["id"]?.toLong() ?: run {
                status = 400
                respondText("Invalid user ID")
                return@get
            }

            val user = userService.findById(userId)
            respondJson(user)
        } catch (e: UserNotFoundException) {
            status = 404
            respondText("User not found")
        } catch (e: DatabaseException) {
            status = 503
            respondText("Service temporarily unavailable")
        }
    }
}
```

### Reusable Error Handling

Create helper functions for consistent error handling:

```kotlin
// Helper function
fun HttpCall.handleError(e: Exception) {
    when (e) {
        is ValidationException -> {
            status = 400
            respondJson(mapOf(
                "error" to "Validation Error",
                "message" to e.message,
                "fields" to e.fields
            ))
        }
        is NotFoundException -> {
            status = 404
            respondJson(mapOf(
                "error" to "Not Found",
                "message" to e.message
            ))
        }
        is UnauthorizedException -> {
            status = 401
            respondJson(mapOf(
                "error" to "Unauthorized",
                "message" to e.message
            ))
        }
        is ForbiddenException -> {
            status = 403
            respondJson(mapOf(
                "error" to "Forbidden",
                "message" to e.message
            ))
        }
        else -> {
            status = 500
            respondJson(mapOf(
                "error" to "Internal Server Error",
                "message" to "An unexpected error occurred"
            ))
        }
    }
}

// Usage
route("/api/users/{id}") {
    get {
        try {
            val userId = parameters["id"]?.toLong() ?: throw ValidationException("Invalid ID")
            val user = userService.findById(userId)
            respondJson(user)
        } catch (e: Exception) {
            handleError(e)
        }
    }
}
```

### Error Handling Interceptor

Centralize error handling with an interceptor:

```kotlin
class ErrorHandlingInterceptor : Interceptor {
    private val log = Logger.getLogger(javaClass.name)

    override fun aroundCall(call: HttpCall, next: Handler) {
        try {
            next(call)
        } catch (e: ValidationException) {
            call.status = 400
            call.respondJson(mapOf(
                "error" to "Validation Error",
                "message" to e.message
            ))
        } catch (e: NotFoundException) {
            call.status = 404
            call.respondJson(mapOf(
                "error" to "Not Found",
                "message" to e.message
            ))
        } catch (e: UnauthorizedException) {
            call.status = 401
            call.respondJson(mapOf(
                "error" to "Unauthorized",
                "message" to e.message
            ))
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Unexpected error", e)
            call.status = 500
            call.respondJson(mapOf(
                "error" to "Internal Server Error",
                "message" to "An unexpected error occurred"
            ))
        }
    }
}

// Install globally
routing {
    install(ErrorHandlingInterceptor())

    route("/api/users/{id}") {
        get {
            // Just throw exceptions, interceptor handles them
            val userId = parameters["id"]?.toLong() ?: throw ValidationException("Invalid ID")
            val user = userService.findById(userId) // throws NotFoundException
            respondJson(user)
        }
    }
}
```

## Error Logging

### Default Logging

Kotlet automatically logs internal server errors using `java.util.logging`:

```
SEVERE: Internal server error during request processing: Something went wrong
java.lang.IllegalStateException: Something went wrong
    at com.example.MyHandler.handle(MyHandler.kt:42)
    ...
```

### Custom Logging Framework

Integrate with your preferred logging framework:

```kotlin
class Slf4jErrorsHandler : ErrorsHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(Slf4jErrorsHandler::class.java)
    }

    override fun internalServerError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: Exception
    ) {
        // Log with SLF4J
        logger.error(
            "Internal error processing {} {}",
            request.method,
            request.requestURI,
            e
        )

        response.sendError(500, "Internal server error")
    }
}
```

### Structured Logging

Add context to error logs:

```kotlin
class StructuredLoggingErrorHandler : ErrorsHandler {
    private val log = Logger.getLogger(javaClass.name)

    override fun internalServerError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: Exception
    ) {
        val logData = mapOf(
            "timestamp" to Instant.now(),
            "method" to request.method,
            "uri" to request.requestURI,
            "query" to request.queryString,
            "remoteAddr" to request.remoteAddr,
            "userAgent" to request.getHeader("User-Agent"),
            "errorType" to e.javaClass.name,
            "errorMessage" to e.message
        )

        log.log(
            Level.SEVERE,
            "Internal server error: ${logData.toJsonString()}",
            e
        )

        response.sendError(500, "Internal server error")
    }
}
```

### Request ID Tracking

Add request IDs for distributed tracing:

```kotlin
class RequestIdInterceptor : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        val requestId = UUID.randomUUID().toString()
        call.rawRequest.setAttribute("requestId", requestId)
        call.rawResponse.setHeader("X-Request-ID", requestId)
        return call
    }
}

class RequestIdAwareErrorHandler : ErrorsHandler {
    private val log = Logger.getLogger(javaClass.name)

    override fun internalServerError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: Exception
    ) {
        val requestId = request.getAttribute("requestId") ?: "unknown"

        log.log(
            Level.SEVERE,
            "Internal error [requestId=$requestId] ${request.method} ${request.requestURI}",
            e
        )

        response.status = 500
        response.contentType = "application/json"
        response.writer.write("""
            {
                "error": "Internal Server Error",
                "requestId": "$requestId"
            }
        """.trimIndent())
    }
}

routing {
    install(RequestIdInterceptor())
    errorsHandler = RequestIdAwareErrorHandler()
}
```

## Common Patterns

### Validation Errors

```kotlin
data class ValidationError(
    val field: String,
    val message: String
)

class ValidationException(
    val errors: List<ValidationError>
) : Exception()

route("/api/users") {
    post {
        try {
            val body = receiveText()
            val user = parseUser(body)

            val errors = mutableListOf<ValidationError>()

            if (user.email.isBlank()) {
                errors.add(ValidationError("email", "Email is required"))
            }
            if (!user.email.contains("@")) {
                errors.add(ValidationError("email", "Invalid email format"))
            }
            if (user.age < 18) {
                errors.add(ValidationError("age", "Must be 18 or older"))
            }

            if (errors.isNotEmpty()) {
                throw ValidationException(errors)
            }

            // Save user
            userService.save(user)
            status = 201
            respondJson(user)
        } catch (e: ValidationException) {
            status = 400
            respondJson(mapOf(
                "error" to "Validation Failed",
                "errors" to e.errors
            ))
        }
    }
}
```

### Business Logic Errors

```kotlin
// Domain exceptions
class InsufficientFundsException(
    val balance: Double,
    val requested: Double
) : Exception("Insufficient funds: balance=$balance, requested=$requested")

class AccountLockedException(
    val accountId: String
) : Exception("Account $accountId is locked")

// Handler
route("/api/withdraw") {
    post {
        try {
            val amount = receiveText().toDouble()
            accountService.withdraw(amount) // may throw exceptions

            status = 200
            respondText("Withdrawal successful")
        } catch (e: InsufficientFundsException) {
            status = 400
            respondJson(mapOf(
                "error" to "Insufficient Funds",
                "balance" to e.balance,
                "requested" to e.requested
            ))
        } catch (e: AccountLockedException) {
            status = 403
            respondJson(mapOf(
                "error" to "Account Locked",
                "accountId" to e.accountId
            ))
        }
    }
}
```

## Best Practices

### 1. Always Log Errors

Ensure errors are logged for debugging:

```kotlin
override fun internalServerError(..., e: Exception) {
    log.log(Level.SEVERE, "Error details", e) // Always log
    response.sendError(500, "Generic message") // Generic to user
}
```

### 2. Don't Leak Internal Details

Never expose stack traces, database errors, or system paths to users:

```kotlin
// Good
catch (e: SQLException) {
    log.error("Database error", e)
    status = 500
    respondText("Service temporarily unavailable")
}

// Bad
catch (e: SQLException) {
    status = 500
    respondText("Error: ${e.message}") // Leaks database info
}
```

### 3. Use Appropriate HTTP Status Codes

| Status | Use Case |
|--------|----------|
| 400    | Bad Request - Invalid input |
| 401    | Unauthorized - Missing/invalid authentication |
| 403    | Forbidden - Authenticated but not authorized |
| 404    | Not Found - Resource doesn't exist |
| 405    | Method Not Allowed - HTTP method not supported |
| 409    | Conflict - Resource conflict (duplicate, etc.) |
| 413    | Payload Too Large - Request body exceeds limit |
| 422    | Unprocessable Entity - Validation failed |
| 429    | Too Many Requests - Rate limit exceeded |
| 500    | Internal Server Error - Unexpected error |
| 503    | Service Unavailable - Temporary outage |

### 4. Consistent Error Response Format

Use a consistent JSON structure:

```kotlin
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val path: String? = null,
    val requestId: String? = null
)

fun HttpCall.respondError(status: Int, error: String, message: String) {
    this.status = status
    respondJson(ErrorResponse(
        error = error,
        message = message,
        path = rawRequest.requestURI,
        requestId = rawRequest.getAttribute("requestId") as? String
    ))
}
```

### 5. Fail Fast

Validate input early and fail fast:

```kotlin
route("/api/users/{id}") {
    get {
        // Validate immediately
        val id = parameters["id"]?.toLongOrNull() ?: run {
            status = 400
            respondText("Invalid ID")
            return@get // Fail fast
        }

        // Continue with valid data
        val user = userService.findById(id)
        respondJson(user)
    }
}
```

### 6. Monitor Error Rates

Track error rates for alerting:

```kotlin
class MonitoredErrorHandler(
    private val metrics: MetricsService
) : ErrorsHandler {
    override fun internalServerError(..., e: Exception) {
        metrics.increment("errors.internal_server_error")
        metrics.increment("errors.by_type.${e.javaClass.simpleName}")

        // ... handle error
    }
}
```

## See Also

- [SECURITY.md](SECURITY.md) - Security best practices
- [INTERCEPTORS.md](INTERCEPTORS.md) - Interceptor patterns
- [Jakarta Servlet Error Handling](https://jakarta.ee/specifications/servlet/6.0/)
