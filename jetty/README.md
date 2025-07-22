# Jetty support for Kotlet

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-jetty:0.46.0")
```

## Configuration

This library provides a convenient way to run your Kotlet application using the Jetty server. You can use the `jetty`
method to create a Jetty server with your Kotlet routing.

Example:

```kotlin
val applicationRouter = Kotlet.routing {
    get("/hello") { call ->
        call.respondText("Hello, World!")
    }
}

val auxRouter = Kotlet.routing {
    get("/health") { call ->
        call.respondText("OK")
    }
}

val jettyServer = jetty(applicationRouter, auxRouter) {
    port = 8080
}

jettyServer.start()
```
