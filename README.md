# Kotlet: A playful blend of Kotlin and Servlet with simple routing

Welcome to Kotlet! This library enables you to create routing for web applications using Java Servlets and the Kotlin
programming language. Kotlet is designed for developers who appreciate simplicity and efficiency.

With Kotlet, you can forget about complex configurations and verbose code required for setting up routes. We offer an
intuitive and powerful API that makes the process of creating routes easy and fast.

## Key Features

* Simple Syntax: Use concise and understandable methods to define routes.
* Powerful Functionality: Support for all HTTP methods (GET, POST, PUT, DELETE, and more).
* Integration with Java Servlets: Full compatibility with existing Servlet infrastructure.
* Kotlin Support: Fully utilize Kotlin's features for cleaner and safer code.
* Extensibility: Easily extend functionality with Interceptors and other tools.

# Example Usage

```kotlin
val routing = Kotlet.routing {
    get("/hello") { call ->
        call.responseText("Hello, World!")
    }

    get("/json/{name}") { call ->
        val name = call.parameters["name"]
        call.respondJson(User(name))
    }
}

// create HttpServlet with our routing
val kotlet = Kotlet.servlet(listOf(routing))

// add servlet to your server for example Jetty
server.addServlet(ServletHolder(kotlet), "/*")
```

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
    call.responseText("Hello, World!")
}
get("/hello/world/method") { call ->
    call.responseText("Hello, Method!")
}
```

#### Static Path with Parameters

Define a route with parameters in the path:

```kotlin
get("/first/{userId}/second/{fileId}") { call ->
    // Access via /first/123/second/456
    val userId = call.parameters["userId"] // 123
    val fileId = call.parameters["fileId"] // 456
    call.responseText("User: $userId, File: $fileId")
}
```

#### Optional Parameters

Define a route with optional parameters:

```kotlin
get("/first/{userId?}") { call ->
    // Access via /first/123 or /first
    val userId = call.parameters["userId"] // 123 or null
    call.responseText("User: $userId")
}
```

#### Wildcard

Define a route with wildcard parameters:

```kotlin
get("/first/*/second/*") { call ->
    // Access via /first/123/second/456
    call.responseText("Hello from wildcard route")
}
```

#### Tail Path

Define a route with a tail path that captures the rest of the path:

```kotlin
get("/first/{...}") { call ->
    // Access via /first/second/third
    call.responseText("Hello from tail path route")
}
```

#### Complex Paths

Define a route with a combination of static, parameter, wildcard, and tail path elements:

```kotlin
get("/first/{userId}/second/*/third/{...}") { call ->
    // Access via /first/123/second/456/third/789/10
    val userId = call.parameters["userId"] // 123
    call.responseText("User: $userId")
}
```

Kotlet's routing methods provide a flexible way to define HTTP routes with various types of path parameters. Whether
using static paths, dynamic parameters, optional segments, wildcards, or complex combinations, Kotlet makes it easy to
handle different routing scenarios in your application.

## Installation

Add the following dependency to your build.gradle.kts file:

```kotlin
dependencies {
    implementation("com.ecwid:kotlet-core:1.0.0")
}
```

Or to pom.xml if you use Maven:

```xml

<dependency>
    <groupId>com.ecwid</groupId>
    <artifactId>kotlet-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Define the Kotlet routing in your application, and you're ready to go!

```kotlin
val routing = Kotlet.routing {
    get("/hello") { call ->
        call.responseText("Hello, World!")
    }
}
```

Create HttpServlet with the routing and add it to your server

```kotlin
val kotlet = Kotlet.servlet(listOf(routing))
// add servlet to your server for example Jetty
server.addServlet(ServletHolder(kotlet), "/*")
```

Now your server is ready to handle Kotlet requests!

## Extending Functionality

### Extension libraries

Kotlet provides a set of extensions that can be used to enhance the functionality of your application. Kotlet extensions
are designed to be lightweight and easy to use. You can add them to your project as needed to extend.

The following extensions are available:
- [CORS](docs/CORS.md)
- [Json](docs/JSON.md)
- [JWT](docs/JWT.md)
- [Metrics](docs/METRICS.md)
- [Tracing](docs/TRACING.md)
- [Type Safe](docs/TYPESAFE.md)

### Interceptors

Kotlet provides an interceptor functionality that allows you to intercept requests and responses. You can use
interceptors to add custom logic before or after handling a request, such as logging, authentication, or error handling.
For more information, see the [Interceptors](docs/INTERCEPTORS.md) documentation.

### Custom error handling

Kotlet support error handling by providing a way to define error handlers for few types of errors:

* `routeNotFound`: Invoked when no route matches the request path.
* `methodNotFound`: Invoked when the route matches the request path but does not support the request method.
* `internalServerError`: Invoked when an exception occurs during request processing.

For overriding default error handling you can implement `kotlet.ErrorsHandler` interface and provide it to
`Kotlet.servlet` method.

## Contributing

We welcome your contributions and suggestions! Open an issue or create a pull request in our repository.
