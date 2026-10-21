package kotlet

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

/**
 * Default maximum request body size (10 MB).
 */
private const val DEFAULT_MAX_BODY_SIZE = 10 * 1024 * 1024L

private const val BUFFER_SIZE = 8192

/**
 * Reads the request body as a string with a size limit to prevent DoS attacks.
 *
 * If the body exceeds [maxSize], a [RequestBodyTooLargeException] is thrown. When it is not caught by the handler,
 * the [ErrorsHandler.requestBodyTooLarge] is called (by default it responds with 413 Payload Too Large).
 *
 * @param maxSize Maximum allowed size in bytes. Defaults to 10 MB.
 * @param charset Character set to use for decoding. Defaults to UTF-8.
 * @return The request body as a string.
 * @throws RequestBodyTooLargeException if the request body exceeds the maximum size.
 *
 * Example:
 * ```kotlin
 * post("/api/upload") { call ->
 *     val body = call.receiveText(maxSize = 1024 * 1024) // 1 MB limit
 *     call.respondText("Received: $body")
 * }
 * ```
 */
fun HttpCall.receiveText(
    maxSize: Long = DEFAULT_MAX_BODY_SIZE,
    charset: Charset = Charsets.UTF_8
): String {
    val bytes = receiveBytes(maxSize)
    return String(bytes, charset)
}

/**
 * Reads the request body as bytes with a size limit to prevent DoS attacks.
 *
 * If the `Content-Length` header is present and exceeds [maxSize], the exception is thrown without reading the body.
 *
 * @param maxSize Maximum allowed size in bytes. Defaults to 10 MB.
 * @return The request body as a byte array.
 * @throws RequestBodyTooLargeException if the request body exceeds the maximum size.
 *
 * Example:
 * ```kotlin
 * post("/api/upload") { call ->
 *     val data = call.receiveBytes(maxSize = 5 * 1024 * 1024) // 5 MB limit
 *     call.respondText("Received ${data.size} bytes")
 * }
 * ```
 */
fun HttpCall.receiveBytes(maxSize: Long = DEFAULT_MAX_BODY_SIZE): ByteArray {
    val output = ByteArrayOutputStream(BUFFER_SIZE)
    receiveLimitedStream(maxSize).copyTo(output, BUFFER_SIZE)
    return output.toByteArray()
}

/**
 * Creates a size-limited input stream that throws an exception if more than [maxSize] bytes are read.
 *
 * If the `Content-Length` header is present and exceeds [maxSize], the exception is thrown immediately.
 *
 * @param maxSize Maximum allowed size in bytes.
 * @return A wrapped input stream that enforces the size limit.
 * @throws RequestBodyTooLargeException if the stream attempts to read more than the maximum size.
 *
 * Example:
 * ```kotlin
 * post("/api/file") { call ->
 *     call.receiveLimitedStream(maxSize = 100 * 1024 * 1024).use { stream ->
 *         storage.save(stream)
 *     }
 *     call.status = HttpServletResponse.SC_CREATED
 * }
 * ```
 */
fun HttpCall.receiveLimitedStream(maxSize: Long = DEFAULT_MAX_BODY_SIZE): InputStream {
    require(maxSize >= 0) { "maxSize must be non-negative, but was $maxSize" }

    val contentLength = rawRequest.contentLengthLong
    if (contentLength > maxSize) {
        throw RequestBodyTooLargeException(maxSize, contentLength)
    }

    return SizeLimitedInputStream(rawRequest.inputStream, maxSize)
}

/**
 * Exception thrown when request body exceeds the configured size limit.
 *
 * @property maxSize The maximum allowed size in bytes.
 * @property bytesRead The number of bytes that were attempted to be read (or the declared `Content-Length`).
 */
class RequestBodyTooLargeException(
    val maxSize: Long,
    val bytesRead: Long
) : Exception("Request body size ($bytesRead bytes) exceeds maximum allowed size ($maxSize bytes)")

/**
 * Internal input stream that enforces a maximum size limit.
 * Reading exactly [maxSize] bytes is allowed, the exception is thrown only when there is more data.
 */
private class SizeLimitedInputStream(
    private val delegate: InputStream,
    private val maxSize: Long
) : InputStream() {
    private var bytesRead = 0L

    override fun read(): Int {
        if (bytesRead >= maxSize) {
            return checkEndOfStream()
        }
        val byte = delegate.read()
        if (byte != -1) {
            bytesRead++
        }
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) {
            return 0
        }
        if (bytesRead >= maxSize) {
            return checkEndOfStream()
        }

        // Read up to the limit
        val maxToRead = minOf(len.toLong(), maxSize - bytesRead).toInt()
        val read = delegate.read(b, off, maxToRead)
        if (read > 0) {
            bytesRead += read
        }
        return read
    }

    override fun available(): Int {
        return minOf(delegate.available().toLong(), maxSize - bytesRead).toInt()
    }

    override fun close() {
        delegate.close()
    }

    /**
     * The limit is reached: returns -1 if the delegate stream is exhausted, otherwise throws.
     */
    private fun checkEndOfStream(): Int {
        if (delegate.read() == -1) {
            return -1
        }
        throw RequestBodyTooLargeException(maxSize, bytesRead + 1)
    }
}
