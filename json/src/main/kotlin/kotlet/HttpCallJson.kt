package kotlet

import kotlet.json.Serializer

/**
 * Use Kotlin Serialization to parse JSON in request body and create object T
 */
inline fun <reified T> HttpCall.receiveBody(): T {
    return Serializer.deserialize(rawRequest.inputStream, T::class.java)
}

/**
 * Use Kotlin Serialization to create JSON from object and send it to the response
 */
fun HttpCall.respondJson(obj: Any) {
    rawResponse.contentType = "application/json"
    Serializer.serializeToStream(obj, rawResponse.outputStream)
}
