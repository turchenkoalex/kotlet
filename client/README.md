# Kotlet Client

A lightweight, type-safe HTTP client for Kotlin built on top of Java's native `HttpClient`. The client provides a clean API for making HTTP requests with support for automatic serialization/deserialization, GZIP compression, and custom headers.

## Features

- Type-safe HTTP requests with reified generics
- Automatic JSON serialization/deserialization using Kotlinx Serialization
- GZIP compression support for both requests and responses
- Configurable User-Agent and custom headers
- Support for all standard HTTP methods (GET, POST, PUT, DELETE)
- Extensible serializer interface
- Built on Java's native `HttpClient` (no external HTTP dependencies)

## Dependencies

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$version")
```

## Core Components

### Client

The main HTTP client class that handles request execution and response processing.

**Location**: `kotlet.client.Client`

Key features:
- Automatic request/response serialization based on type parameters
- Configurable default headers
- GZIP compression handling
- Closeable resource management

### Request

Generic HTTP request representation that encapsulates HTTP method, URI, headers, and body.

**Location**: `kotlet.client.Request`

### Response

Generic HTTP response wrapper containing status code, headers, and deserialized body.

**Location**: `kotlet.client.Response`

```kotlin
data class Response<TRes>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: TRes?
)
```

### ClientOptions

Configuration options for customizing client behavior.

**Location**: `kotlet.client.ClientOptions`

Available options:
- `userAgent`: Custom User-Agent header value
- `allowGzipRequests`: Enable GZIP compression for request bodies
- `allowGzipResponses`: Enable GZIP decompression for response bodies (default: true)
- `additionalHeaders`: Map of additional headers to include in every request

### ClientSerializer

Interface for pluggable serialization strategies.

**Location**: `kotlet.client.ClientSerializer`

Default implementation: `KotlinxClientSerializer` (uses Kotlinx Serialization with JSON format)

## Quick Start

### Basic Usage

```kotlin
import kotlet.client.Client
import java.net.URI

// Create a client with default options
val client = Client.newClient()

// Make a GET request
data class User(val id: Int, val name: String)
val user: User? = client.get(URI("https://api.example.com/users/1"))

// Make a POST request
data class CreateUserRequest(val name: String, val email: String)
data class CreateUserResponse(val id: Int, val status: String)

val response: CreateUserResponse? = client.post(
    uri = URI("https://api.example.com/users"),
    req = CreateUserRequest(name = "John Doe", email = "john@example.com")
)

// Always close the client when done
client.close()
```

### Using with try-with-resources

```kotlin
Client.newClient().use { client ->
    val user: User? = client.get(URI("https://api.example.com/users/1"))
    // Client is automatically closed
}
```

### Custom Configuration

```kotlin
import kotlet.client.ClientOptions
import java.net.http.HttpClient

val options = ClientOptions(
    userAgent = "MyApp/1.0.0",
    allowGzipRequests = true,
    allowGzipResponses = true,
    additionalHeaders = mapOf(
        "X-API-Key" to "secret-key",
        "X-Custom-Header" to "value"
    )
)

val httpBuilder = HttpClient.newBuilder()
    .connectTimeout(java.time.Duration.ofSeconds(30))

val client = Client.newClient(
    httpBuilder = httpBuilder,
    options = options
)
```

## HTTP Methods

### GET Request

```kotlin
inline fun <reified TRes : Any> get(
    uri: URI,
    headers: Map<String, String> = emptyMap()
): TRes?
```

Example:
```kotlin
val user: User? = client.get(
    uri = URI("https://api.example.com/users/1"),
    headers = mapOf("Authorization" to "Bearer token")
)
```

### POST Request

```kotlin
inline fun <reified TReq : Any, reified TRes : Any> post(
    uri: URI,
    req: TReq? = null,
    headers: Map<String, String> = emptyMap()
): TRes?
```

Example:
```kotlin
val response: CreateUserResponse? = client.post(
    uri = URI("https://api.example.com/users"),
    req = CreateUserRequest(name = "Jane", email = "jane@example.com")
)
```

### PUT Request

```kotlin
inline fun <reified TReq : Any, reified TRes : Any> put(
    uri: URI,
    req: TReq? = null,
    headers: Map<String, String> = emptyMap()
): TRes?
```

Example:
```kotlin
val updated: User? = client.put(
    uri = URI("https://api.example.com/users/1"),
    req = UpdateUserRequest(name = "Jane Smith")
)
```

### DELETE Request

```kotlin
inline fun <reified TRes : Any> delete(
    uri: URI,
    headers: Map<String, String> = emptyMap()
): TRes?
```

Example:
```kotlin
val result: DeleteResponse? = client.delete(
    uri = URI("https://api.example.com/users/1")
)
```

## Advanced Usage

### Custom Serializer

Implement the `ClientSerializer` interface to use a different serialization format:

```kotlin
class CustomSerializer : ClientSerializer {
    override val acceptContentType: String = "application/xml"

    override fun <T> serializeToStream(obj: T, clazz: Class<T>, outputStream: OutputStream) {
        // Custom serialization logic
    }

    override fun <T> deserializeFromStream(inputStream: InputStream, clazz: Class<T>): T {
        // Custom deserialization logic
    }
}

val client = Client.newClient(serializer = CustomSerializer())
```

### Low-Level Request Building

```kotlin
val request = client.buildRequest(
    method = "PATCH",
    uri = URI("https://api.example.com/users/1"),
    headers = mapOf("Content-Type" to "application/json"),
    req = UpdateData(field = "value"),
    clazz = UpdateData::class.java
)

val response: Response<User> = client.send(request, User::class.java)
println("Status: ${response.statusCode}")
println("Headers: ${response.headers}")
println("Body: ${response.body}")
```

### Working with Raw Byte Arrays

```kotlin
// Send raw bytes
val rawData = "custom data".toByteArray()
val response: ResponseData? = client.post(
    uri = URI("https://api.example.com/data"),
    req = rawData
)
```

## GZIP Compression

The client automatically handles GZIP compression when configured:

### Response Compression

Enabled by default. The client automatically decompresses GZIP-encoded responses:

```kotlin
val options = ClientOptions(allowGzipResponses = true)
val client = Client.newClient(options = options)
```

The client adds `Accept-Encoding: gzip` header and decompresses responses transparently.

### Request Compression

Disabled by default. Enable to compress request bodies:

```kotlin
val options = ClientOptions(allowGzipRequests = true)
val client = Client.newClient(options = options)
```

The client adds `Content-Encoding: gzip` header and compresses request bodies before sending.

## Error Handling

The client throws standard Java exceptions:

```kotlin
try {
    val user: User? = client.get(URI("https://api.example.com/users/1"))
} catch (e: java.io.IOException) {
    // Network error
} catch (e: InterruptedException) {
    // Request interrupted
} catch (e: IllegalArgumentException) {
    // Serialization error
}
```

## Thread Safety

The `Client` class is thread-safe and can be shared across multiple threads. The underlying `HttpClient` handles concurrent requests efficiently.

## Best Practices

1. **Reuse client instances**: Create one client and reuse it across requests
2. **Use try-with-resources**: Always close the client or use `.use { }` block
3. **Configure timeouts**: Set appropriate timeouts on the `HttpClient.Builder`
4. **Handle errors**: Wrap requests in try-catch blocks
5. **Type safety**: Leverage reified generics for type-safe serialization
6. **Custom headers**: Use `ClientOptions.additionalHeaders` for headers common to all requests
7. **Enable compression**: Use GZIP compression for large payloads to reduce bandwidth

## Implementation Details

### Serialization

- Uses Kotlinx Serialization by default with `ignoreUnknownKeys = true`
- Serializers are cached for performance
- Supports `ByteArray` and `String` as pass-through types
- Returns `null` for non-2xx responses when body is expected

### Headers

Default headers are applied in this order:
1. `Accept` header based on serializer content type
2. `User-Agent` (if configured)
3. `Accept-Encoding: gzip` (if response compression enabled)
4. `Content-Encoding: gzip` (if request compression enabled)
5. Additional headers from `ClientOptions`
6. Per-request headers (override defaults)

### Response Body Handling

- Bodies are only deserialized for 2xx status codes
- `Unit` type parameter skips deserialization
- GZIP responses are automatically detected and decompressed based on `Content-Encoding` header
