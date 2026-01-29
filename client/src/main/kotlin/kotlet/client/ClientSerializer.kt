package kotlet.client

import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer interface for serializing and deserializing objects to and from streams.
 */
interface ClientSerializer {
    /**
     * The content type used by this serializer (e.g., "application/json").
     */
    val contentType: String

    /**
     * Serializes the given object and writes it to the provided output stream.
     *
     * @param obj The object to serialize.
     * @param clazz The class of the object to serialize.
     * @param outputStream The output stream to write the serialized data to.
     */
    fun <T> serializeToStream(obj: T, clazz: Class<T>, outputStream: OutputStream)

    /**
     * Deserializes an object of the specified class from the provided input stream.
     *
     * @param inputStream The input stream to read the serialized data from.
     * @param clazz The class of the object to deserialize.
     * @return The deserialized object.
     */
    fun <T> deserializeFromStream(inputStream: InputStream, clazz: Class<T>): T
}
