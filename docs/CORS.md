# Cross-Origin Resource Sharing (CORS)

## Installation

```kotlin
implementation("com.ecwid:kotlet-cors:1.0.0")
```

## Configuration

Configure Cross-Origin Resource Sharing (CORS) for your application using the `cors` method.

```kotlin
Kotlet.routing {
    installCORS(CORS.allowAll) // for development

    // allow only from one origin https://example.com
    installCORS(CORS.allowOrigin("https://example.com"))

    // or implement your own logic
    val myCustomCorsRules = object : CorsRules {
        override fun CorsRules(call: HttpCall): CorsHeaders {
            return CorsHeaders(
                allowOrigin = "https://example.com",
                allowMethods = listOf(HttpMethod.GET, HttpMethod.POST),
                allowHeaders = listOf("Content-Type")
            )
        }
    }
    installCORS(myCustomCorsRules)
}
```
