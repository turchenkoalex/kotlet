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

## Route Blocks

Kotlet supports route blocks for grouping related routes together. Use the `route` method to define a block of routes
under a common path prefix:

```kotlin
route("/api") {
    get("/users") { call ->
        call.respondText("List of users")
    }
    get("/users/{userId}") { call ->
        val userId = call.parameters["userId"]
        call.respondText("User: $userId")
    }
}
```

Route blocks allow you to organize your routes into logical sections, making it easier to manage and maintain your
application's routing configuration.

### Nested Routes

Kotlet supports nested routes for creating hierarchical route structures. Use the `route` method to define nested
routes within a parent route block:

```kotlin
route("/admin") {
    route("/users") {
        get("/") { call ->
            call.respondText("List of users")
        }
        post("/") { call ->
            call.respondText("Create user")
        }
    }

    route("/roles") {
        get("/") { call ->
            call.respondText("List of roles")
        }
        post("/") { call ->
            call.respondText("Create role")
        }
    }
}
```

Nested routes allow you to create a tree-like structure of routes, making it easy to organize and manage complex routing
configurations in your application.

## Conclusion

Kotlet provides a powerful and flexible routing system that allows you to define HTTP routes with ease. Whether you're
creating simple static routes, dynamic routes with parameters, or complex nested routes, Kotlet's routing methods and
features make it easy to build robust and scalable web applications.
