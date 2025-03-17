package kotlet.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalSerializationApi::class)
object Serializer {
    private val kotlinSerializersCache = ConcurrentHashMap<Class<*>, KSerializer<*>>()

    fun serializeToStream(obj: Any, outputStream: OutputStream) {
        val serializer = findSerializer(obj.javaClass)
        return json.encodeToStream(serializer, obj, outputStream)
    }

    fun <T> deserialize(inputStream: InputStream, clazz: Class<T>): T {
        val serializer = findSerializer(clazz)
        return json.decodeFromStream(serializer, inputStream)
    }

    private fun <T> findSerializer(clazz: Class<T>): KSerializer<T> {
        @Suppress("UNCHECKED_CAST")
        return kotlinSerializersCache.computeIfAbsent(clazz) {
            serializer(clazz)
        } as KSerializer<T>
    }

    // Custom JSON instance used for routing serialization/deserialization
    private val json = Json {
        ignoreUnknownKeys = true
    }

}
