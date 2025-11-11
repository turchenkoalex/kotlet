# Kotlet: A blend of Kotlin and Servlet with simple routing

![GitHub Tag](https://img.shields.io/github/v/tag/turchenkoalex/kotlet?sort=semver&label=version&color=green)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.turchenkoalex/kotlet-core)
[![codecov](https://codecov.io/gh/turchenkoalex/kotlet/graph/badge.svg?token=F21ZD8IPEJ)](https://codecov.io/gh/turchenkoalex/kotlet)

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

Create a simple routing and servlet with Kotlet:

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
val kotlet = Kotlet.servlet(routing)

// add servlet to your server for example Jetty
server.addServlet(ServletHolder(kotlet), "/*")
```

Or you can create your own `HttpServlet` and use Kotlet as a routing library through handler:

```kotlin
class MyServlet: HttpServlet() {
    private val routing = Kotlet.routing {
        get("/hello") { call ->
            call.respondText("Hello, World!")
        }
    }

    private val handler = Kotlet.handler(routing)
    
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        // process request and response with Kotlet library
        handler.service(request, response)
    }
}

// add servlet to your server for example Jetty
server.addServlet(ServletHolder(MyServlet()), "/*")
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
    implementation("io.github.turchenkoalex:kotlet-core:0.74.0")
}
```

Define the Kotlet routing in your application, and you're ready to go!

```kotlin
val routing = Kotlet.routing {
    get("/hello") { call ->
        call.respondText("Hello, World!")
    }
    
    // API section
    route("/api/v1") {
        get("/users") { call ->
            call.respondText("List of users at /api/v1/users")
        }
    }
    
    // admin section
    route("/admin") {
        // users nested routes section
        route("/api/users") {
            get { call ->
                call.respondText("List of users at /admin/api/users")
            }
            post { call ->
                call.respondText("Create user")
            }
        }
    }
}
```

Create `HttpServlet` with the routing and add it to your server

```kotlin
val kotlet = Kotlet.servlet(routing)
// add servlet to your server for example Jetty
server.addServlet(ServletHolder(kotlet), "/*")
```

Now your server is ready to handle Kotlet requests!

## Extending Functionality

### Extension libraries

Kotlet provides a set of extensions that can be used to enhance the functionality of your application. Kotlet extensions
are designed to be lightweight and easy to use. You can add them to your project as needed to extend.

The following extensions are available:
- [CORS](cors/README.md)
- [Jetty](jetty/README.md)
- [JSON](json/README.md)
- [JWT](jwt/README.md)
- [Metrics](metrics/README.md)
- [Tracing](tracing/README.md)
- [Type Safe](typesafe/README.md)
- [OpenAPI](openapi/README.md)
- [Swagger UI](swagger-ui/README.md)

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
