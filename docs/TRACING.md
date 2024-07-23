# Tracing

```kotlin
implementation("com.ecwid:kotlet-tracing:1.0.0")
```

Use the `installTracing` method to add tracing to your routes. The method takes a `OpenTelemetry` object as a parameter.

```kotlin
Kotlet.routing {
    installTracing(GlobalOpenTelemetry.get())
    get("/hello") { call ->
        call.responseText("Hello, World!")
    }
}
```

After that the extension will automatically create spans for each request and send them to the library.

> [!IMPORTANT]  
> You must configure OpenTelemetry before using this extension.
