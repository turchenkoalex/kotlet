# Error Handling

This guide explains how errors are handled in Kotlet and how to customize it.

## Table of Contents

1. [Default Error Handling](#default-error-handling)
2. [Custom Errors Handler](#custom-errors-handler)
3. [Handler-Level Error Handling](#handler-level-error-handling)
4. [Error Handling Interceptor](#error-handling-interceptor)
5. [Logging](#logging)
6. [Best Practices](#best-practices)

## Default Error Handling

All errors are delegated to the `kotlet.ErrorsHandler`. The default implementation responds with
`HttpServletResponse.sendError`:

| Situation                                                    | Method                | Default response                 |
|--------------------------------------------------------------|-----------------------|----------------------------------|
| No route matches the request path                            | `routeNotFound`       | `404 Not found`                  |
| The route matches, but has no handler for the HTTP method    | `methodNotFound`      | `405 Method not allowed`         |
| Handler throws `RequestBodyTooLargeException`                | `requestBodyTooLarge` | `413 Request body too large`     |
| Handler (or interceptor) throws any other `Exception`        | `internalServerError` | `500 Internal server error`      |

```kotlin
val routing = Kotlet.routing {
    get("/api/users") { call ->
        call.respondText("List users")
    }

    get("/api/fail") { call ->
        error("Something went wrong")
    }

    post("/api/upload") { call ->
        val body = call.receiveText(maxSize = 1024)
        call.respondText("Received ${body.length} chars")
    }
}

// GET  /api/unknown                  -> 404 "Not found"
// POST /api/users                    -> 405 "Method not allowed"
// GET  /api/fail                     -> 500 "Internal server error" (exception is logged)
// POST /api/upload (body > 1 KB)     -> 413 "Request body too large"
```

> [!NOTE]
> If a root route `/` is defined, it catches all unmatched paths, so `routeNotFound` is never called.
> See [ROUTES.md](ROUTES.md#root-route).

Only `Exception` subclasses are handled. `Error`s (`OutOfMemoryError`, `StackOverflowError`, ...) are propagated
to the servlet container.

## Custom Errors Handler

Implement `kotlet.ErrorsHandler` and override only the methods you need. All methods have default implementations.

```kotlin
object JsonErrorsHandler : ErrorsHandler {
    private val log = Logger.getLogger(JsonErrorsHandler::class.java.name)

    override fun routeNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.writeJsonError(HttpServletResponse.SC_NOT_FOUND, "Not found")
    }

    override fun methodNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.writeJsonError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed")
    }

    override fun requestBodyTooLarge(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: RequestBodyTooLargeException,
    ) {
        response.writeJsonError(
            HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
            "Request body exceeds ${e.maxSize} bytes"
        )
    }

    override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
        // The default implementation logs the exception, an overridden one has to do it by itself
        log.log(Level.SEVERE, "Internal server error: ${request.method} ${request.requestURI}", e)

        // Don't leak exception details to the client
        response.writeJsonError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error")
    }

    private fun HttpServletResponse.writeJsonError(status: Int, message: String) {
        this.status = status
        contentType = "application/json"
        writer.write("""{"status":$status,"error":"$message"}""")
    }
}
```

The errors handler is passed when the servlet (or handler) is created:

```kotlin
val servlet = Kotlet.servlet(routing, errorsHandler = JsonErrorsHandler)

// or, if you use Kotlet as a routing library inside your own servlet
val handler = Kotlet.handler(routing, errorsHandler = JsonErrorsHandler)

// or with the kotlet-jetty extension
val server = jetty(routing) {
    errorsHandler = JsonErrorsHandler
}
```

> [!WARNING]
> If the response is already committed (the handler has already flushed some output), `sendError` throws
> `IllegalStateException` and the status can't be changed anymore. Validate input before you start writing the response.

## Handler-Level Error Handling

Handle expected errors directly in the handler and respond with a proper status:

```kotlin
get("/api/users/{id}") { call ->
    val userId = call.parameters["id"]?.toLongOrNull()
    if (userId == null) {
        call.respondError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID")
        return@get
    }

    val user = userService.findById(userId)
    if (user == null) {
        call.respondError(HttpServletResponse.SC_NOT_FOUND, "User not found")
        return@get
    }

    call.respondJson(user)
}
```

`respondJson` and `receiveBody` come from the [JSON](../json/README.md) extension and work with
`@Serializable` classes (kotlinx.serialization), so use data classes for error bodies instead of `Map<String, Any>`:

```kotlin
@Serializable
data class ErrorResponse(val error: String, val message: String)
```

## Error Handling Interceptor

Domain exceptions can be mapped to HTTP responses in one place with an interceptor:

```kotlin
class NotFoundException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)

object DomainErrorsInterceptor : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        try {
            next(call)
        } catch (e: ValidationException) {
            call.status = HttpServletResponse.SC_BAD_REQUEST
            call.respondJson(ErrorResponse("Validation error", e.message.orEmpty()))
        } catch (e: NotFoundException) {
            call.status = HttpServletResponse.SC_NOT_FOUND
            call.respondJson(ErrorResponse("Not found", e.message.orEmpty()))
        }
        // other exceptions go to ErrorsHandler.internalServerError
    }
}

val routing = Kotlet.routing {
    install(DomainErrorsInterceptor)

    get("/api/users/{id}") { call ->
        val id = call.parameters["id"]?.toLongOrNull() ?: throw ValidationException("Invalid ID")
        val user = userService.findById(id) ?: throw NotFoundException("User $id not found")
        call.respondJson(user)
    }
}
```

> [!IMPORTANT]
> Catch only your own exception types in interceptors. Global interceptors wrap the method dispatch, so a
> `catch (e: Exception)` would also swallow the internal signal for `405 Method not allowed` and
> `RequestBodyTooLargeException`, turning them into whatever your interceptor responds with.

`afterCall` of every interceptor is always called (even when an exception is thrown), so it is a good place for
cleanup. See [INTERCEPTORS.md](INTERCEPTORS.md).

## Logging

Kotlet uses `java.util.logging` and has no logging dependencies.

* The default `internalServerError` logs the exception with the stack trace at `SEVERE` level
  (logger `kotlet.ErrorsHandler`):

  ```
  SEVERE: Internal server error during request processing: Something went wrong
  java.lang.IllegalStateException: Something went wrong
      at ...
  ```

  If you override `internalServerError`, log the exception yourself.
* The JWT extension logs failed token verifications at `FINE` level
  (logger `kotlet.jwt.JWTAuthenticationInterceptor`).

To route these logs to SLF4J/Logback, use the
[jul-to-slf4j](https://www.slf4j.org/legacy.html#jul-to-slf4j) bridge, or log with your framework in a custom handler:

```kotlin
object Slf4jErrorsHandler : ErrorsHandler {
    private val logger = LoggerFactory.getLogger(Slf4jErrorsHandler::class.java)

    override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
        logger.error("Internal error processing {} {}", request.method, request.requestURI, e)
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error")
    }
}
```

### Request ID

A request id set by an interceptor is available in the errors handler through request attributes:

```kotlin
object RequestIdInterceptor : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        val requestId = call.rawRequest.getHeader("X-Request-ID") ?: UUID.randomUUID().toString()
        call.rawRequest.setAttribute("requestId", requestId)
        call.rawResponse.setHeader("X-Request-ID", requestId)
        return call
    }
}

object RequestIdErrorsHandler : ErrorsHandler {
    private val log = Logger.getLogger(RequestIdErrorsHandler::class.java.name)

    override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Exception) {
        val requestId = request.getAttribute("requestId") ?: "unknown"
        log.log(Level.SEVERE, "Internal error [requestId=$requestId] ${request.method} ${request.requestURI}", e)
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error")
    }
}

val routing = Kotlet.routing {
    install(RequestIdInterceptor, order = InstallOrder.FIRST)
    // routes...
}
val servlet = Kotlet.servlet(routing, errorsHandler = RequestIdErrorsHandler)
```

> [!NOTE]
> `routeNotFound` is called before any interceptor runs (there is no route to take interceptors from), so request
> attributes set by interceptors are not available there.

## Best Practices

1. **Log unexpected errors, respond generically.** Never send `e.message` or stack traces to the client: they can
   leak SQL, file paths or internal hostnames.
2. **Use proper status codes.** `400` for invalid input, `401` for missing/invalid credentials, `403` for missing
   permissions, `404` for missing resources, `409` for conflicts, `413` for too large bodies, `422` for semantically
   invalid input, `429` for rate limiting, `503` for temporary unavailability.
3. **Validate early.** Check parameters and the body before writing anything to the response.
4. **Keep one error format.** Use one `ErrorResponse` class for interceptors and the errors handler.
5. **Monitor error rates.** The [Metrics](../metrics/README.md) extension records the response status of every
   request, so 4xx/5xx rates can be alerted on in Prometheus.

## See Also

- [SECURITY.md](SECURITY.md) - Security best practices
- [INTERCEPTORS.md](INTERCEPTORS.md) - Interceptors
- [ROUTES.md](ROUTES.md) - Routing
