## Routing methods

Kotlet provides a simple and intuitive syntax for defining routes in your application. The following methods are
available for creating routes:

* `get(path: String, handler: (HttpCall) -> Unit)`
* `post(path: String, handler: (HttpCall) -> Unit)`
* `put(path: String, handler: (HttpCall) -> Unit)`
* `delete(path: String, handler: (HttpCall) -> Unit)`
* `patch(path: String, handler: (HttpCall) -> Unit)`
* `options(path: String, handler: (HttpCall) -> Unit)`
* `head(path: String, handler: (HttpCall) -> Unit)`
* `trace(path: String, handler: (HttpCall) -> Unit)`

### Path Syntax

#### Static Paths

Define a route with a static path:

```kotlin
get("/hello/world") { call ->
    call.respondText("Hello, World!")
}
get("/hello/world/method") { call ->
    call.respondText("Hello, Method!")
}
```

#### Static Path with Parameters

Define a route with parameters in the path:

```kotlin
get("/first/{userId}/second/{fileId}") { call ->
    // Access via /first/123/second/456
    val userId = call.parameters["userId"] // 123
    val fileId = call.parameters["fileId"] // 456
    call.respondText("User: $userId, File: $fileId")
}
```

#### Optional Parameters

Define a route with optional parameters:

```kotlin
get("/first/{userId?}") { call ->
    // Access via /first/123 or /first
    val userId = call.parameters["userId"] // 123 or null
    call.respondText("User: $userId")
}
```

#### Wildcard

Define a route with wildcard parameters:

```kotlin
get("/first/*/second/*") { call ->
    // Access via /first/123/second/456
    call.respondText("Hello from wildcard route")
}
```

#### Tail Path

Define a route with a tail path that captures the rest of the path:

```kotlin
get("/first/{...}") { call ->
    // Access via /first/second/third
    call.respondText("Hello from tail path route")
}
```

#### Complex Paths

Define a route with a combination of static, parameter, wildcard, and tail path elements:

```kotlin
get("/first/{userId}/second/*/third/{...}") { call ->
    // Access via /first/123/second/456/third/789/10
    val userId = call.parameters["userId"] // 123
    call.respondText("User: $userId")
}
```

Kotlet's routing methods provide a flexible way to define HTTP routes with various types of path parameters. Whether
using static paths, dynamic parameters, optional segments, wildcards, or complex combinations, Kotlet makes it easy to
handle different routing scenarios in your application.
