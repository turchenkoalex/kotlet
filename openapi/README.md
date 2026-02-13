# OpenAPI documentation generation

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-openapi:0.93.0")
```

## Configuration

Before configuration OpenAPI routing, you need to configure application routing as usual and provide it to
`documentedRoutings` property.

Example:

```kotlin
    @Serializable
    @OpenApiDescription("Simple response")
    data class HelloResponse(
        val message: String
    )

    val router = Kotlet.routing {
        openAPI {
            path = "/swagger/openapi.json"
            prettyPrint = true
            describe {
                info {
                    title = "Sample API"
                    version = "1.0"
                }
            }
        }
        
        get("/hello") { call ->
            call.respondText("Hello, World!")
        } describe {
            summary = "Hello world"
            jsonResponse<HelloResponse>(200, "Simple response")
        }
    }

    val kotlet = Kotlet.servlet(
        routings = listOf(router)
    )
```

This feature works better with the [`kotlet-swagger-ui`](../swagger-ui/README.md) library. You can use it to visualize
the OpenAPI documentation.
