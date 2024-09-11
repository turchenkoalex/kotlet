# Kotlet: A blend of Kotlin and Servlet with simple routing

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
        call.respondText("Hello, World!")
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
* and more...

For more information, see the [Routing Methods](docs/ROUTES.md) documentation.

## Installation

Add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.ecwid:kotlet-core:1.0.0")
}
```

Or to `pom.xml` if you use Maven:

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
        call.respondText("Hello, World!")
    }
    
    // API section
    route("/api") {
        route("/v1") {
            get("/users") { call ->
                call.respondText("List of users at /api/v1/users")
            }
        }
    }
    
    // admin/api section
    route("/admin/api") {
        // users nested routes section
        route("/users") {
            get("/") { call ->
                call.respondText("List of users at /admin/api/users")
            }

            post("/") { call ->
                call.respondText("Create user")
            }
        }
    }
}
```

Create `HttpServlet` with the routing and add it to your server

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
- [JSON](docs/JSON.md)
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
