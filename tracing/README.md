# Tracing

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-tracing:0.10.0")
```

## Configuration

Use the `installTracing` method to add tracing to your routes. The method takes a `OpenTelemetry` object as a parameter.

```kotlin
Kotlet.routing {
    installTracing(GlobalOpenTelemetry.get())
    get("/hello") { call ->
        call.respondText("Hello, World!")
    }
}
```

After that the extension will automatically create spans for each request and send them to the library.

> [!IMPORTANT]  
> You must configure OpenTelemetry before using this extension.
