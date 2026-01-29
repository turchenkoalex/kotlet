package kotlet.client

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
object KotlinxClientSerializer : ClientSerializer {
    private val kotlinSerializersCache = ConcurrentHashMap<Class<*>, KSerializer<*>>()

    override val acceptContentType: String = "application/json"

    override fun <T> serializeToStream(obj: T, clazz: Class<T>, outputStream: OutputStream) {
        if (obj == null) {
            return
        }

        if (obj is Unit) {
            return
        }

        if (obj is ByteArray) {
            outputStream.write(obj)
            return
        }

        val serializer = findSerializer(clazz)
        json.encodeToStream(serializer, obj, outputStream)
    }

    override fun <T> deserializeFromStream(inputStream: InputStream, clazz: Class<T>): T {
        if (clazz == Unit::class.java) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }

        if (clazz == ByteArray::class.java) {
            @Suppress("UNCHECKED_CAST")
            return inputStream.readAllBytes() as T
        }

        if (clazz == String::class.java) {
            @Suppress("UNCHECKED_CAST")
            return inputStream.bufferedReader().readText() as T
        }

        val serializer = findSerializer(clazz)
        return json.decodeFromStream(serializer, inputStream)
    }

    private fun <T> findSerializer(clazz: Class<T>): KSerializer<T> {
        @Suppress("UNCHECKED_CAST")
        return kotlinSerializersCache.computeIfAbsent(clazz, ::serializer) as KSerializer<T>
    }

    // Custom JSON instance used for routing serialization/deserialization
    private val json = Json {
        ignoreUnknownKeys = true
    }

}
