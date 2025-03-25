# Cross-Origin Resource Sharing (CORS)

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-cors:0.7.0")
```

## Configuration

Configure Cross-Origin Resource Sharing (CORS) for your application using the `cors` method.

```kotlin
Kotlet.routing {
    // allow all for development purposes
    installCORS(CORS.allowAll) 

    // allow only from one origin https://example.com
    installCORS(CORS.allowOrigin("https://example.com"))

    // or implement your own logic
    val myCustomCorsRules = object : CorsRules {
        override fun getResponse(call: HttpCall): CorsResponse {
            return CorsResponse.headers(
                allowOrigin = "https://example.com",
                allowMethods = listOf("GET", "POST"),
                allowHeaders = listOf("Content-Type", "Authorization")
            )
        }
    }
    installCORS(myCustomCorsRules)
    
    // or respond with error
    val myCustomCorsRulesWithError = object : CorsRules {
        override fun getResponse(call: HttpCall): CorsResponse {
            return CorsResponse.error(HttpStatusCode.Forbidden)
        }
    }
    installCORS(myCustomCorsRulesWithError)
}
```
