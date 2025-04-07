# JSON Responses

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-json:0.16.0")
```

## Configuration

Library provides a convenient way to return JSON responses from your routes. You can use the `respondJson` method to
serialize an object to JSON and send it as the response.

```kotlin
@Serializable
data class Post(val title: String, val content: String)

@Serializable
data class DocumentPatch(val title: String?, val content: String?)

patch("/document") { call ->
    val documentPatch = call.receiveBody<DocumentPatch>()
    val document = DocumentService.updateDocument(documentPatch)
    call.respondJson(document)
}
```
