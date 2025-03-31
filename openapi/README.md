# OpenAPI documentation generation

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-openapi:0.8.0")
```

## Configuration

Before configuration OpenAPI routing, you need to configure application routing as usual and provide it to `documentedRoutings` property.

Example:

```kotlin
    data class HelloResponse(val message: String)

    val applicationRouter = Kotlet.routing {
        get("/hello", { call ->
            call.respondText("Hello, World!")
        }) {
            openapi {
                summary("Hello world")
                responses {
                    jsonResponse<HelloResponse>("Simple response")
                }
            }
        }
    }

    val auxRouter = Kotlet.routing {
        installOpenAPI {
            path = "/swagger/openapi.json"
            documentedRoutings = listOf(applicationRouter)
            prettyPrint = true
            openAPI {
                info {
                    title = "Sample API"
                    version = "1.0"
                }
            }
        }
    }

    val kotlet = Kotlet.servlet(
        routings = listOf(applicationRouter, auxRouter)
    )
```

This feature works better with the [`kotlet-swagger-ui`](../swagger-ui/README.md) library. You can use it to visualize the OpenAPI documentation.
