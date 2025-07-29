# Swagger UI

This library provides a simple way to integrate Swagger UI with your Kotlet application. [Swagger UI](https://swagger.io/tools/swagger-ui/) is a popular tool for
API documentation that allows you to visualize and interact with the API's resources.

Library contains all necessary resources to run Swagger UI, so you don't need to download them manually.

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-swagger-ui:0.47.0")
```

## Configuration

```kotlin
Kotlet.routing {
    installSwaggerUIEndpoint {
        path = "/swagger/ui"
        openAPIPath = "/openapi.json"
    }

    get("/hello") { call ->
        call.respondText("Hello, World!")
    }
}
```

The `installSwaggerUIEndpoint` method adds a route to your application that serves the Swagger UI interface. The method
takes a configuration, which contains the following fields:
* `path` - the path where the Swagger UI will be available. Default value is `/swagger/ui`.
* `openAPIPath` - the path to the OpenAPI specification file. Default value is `/openapi.json`.

When open the `/swagger/ui` path in your browser, you will see the Swagger UI interface with the API documentation.
