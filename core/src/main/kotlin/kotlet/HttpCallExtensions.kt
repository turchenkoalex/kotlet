package kotlet

import java.io.InputStream
import java.nio.charset.Charset

/**
 * Default maximum request body size (10 MB).
 */
private const val DEFAULT_MAX_BODY_SIZE = 10 * 1024 * 1024L

/**
 * Reads the request body as a string with a size limit to prevent DoS attacks.
 *
 * @param maxSize Maximum allowed size in bytes. Defaults to 10 MB.
 * @param charset Character set to use for decoding. Defaults to UTF-8.
 * @return The request body as a string.
 * @throws RequestBodyTooLargeException if the request body exceeds the maximum size.
 *
 * Example:
 * ```kotlin
 * route("/api/upload") {
 *     post {
 *         try {
 *             val body = receiveText(maxSize = 1024 * 1024) // 1 MB limit
 *             respondText("Received: $body")
 *         } catch (e: RequestBodyTooLargeException) {
 *             status = 413
 *             respondText("Request body too large")
 *         }
 *     }
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
 * @param maxSize Maximum allowed size in bytes. Defaults to 10 MB.
 * @return The request body as a byte array.
 * @throws RequestBodyTooLargeException if the request body exceeds the maximum size.
 *
 * Example:
 * ```kotlin
 * route("/api/upload") {
 *     post {
 *         try {
 *             val data = receiveBytes(maxSize = 5 * 1024 * 1024) // 5 MB limit
 *             // process data
 *             respondText("Received ${data.size} bytes")
 *         } catch (e: RequestBodyTooLargeException) {
 *             status = 413
 *             respondText("Request body too large")
 *         }
 *     }
 * }
 * ```
 */
fun HttpCall.receiveBytes(maxSize: Long = DEFAULT_MAX_BODY_SIZE): ByteArray {
    val inputStream = rawRequest.inputStream
    return inputStream.readBytesWithLimit(maxSize)
}

/**
 * Creates a size-limited input stream that throws an exception if more than [maxSize] bytes are read.
 *
 * @param maxSize Maximum allowed size in bytes.
 * @return A wrapped input stream that enforces the size limit.
 * @throws RequestBodyTooLargeException if the stream attempts to read more than the maximum size.
 *
 * Example:
 * ```kotlin
 * route("/api/file") {
 *     post {
 *         try {
 *             receiveLimitedStream(maxSize = 10 * 1024 * 1024).use { stream ->
 *                 // Process stream safely
 *                 stream.copyTo(outputStream)
 *             }
 *         } catch (e: RequestBodyTooLargeException) {
 *             status = 413
 *             respondText("File too large")
 *         }
 *     }
 * }
 * ```
 */
fun HttpCall.receiveLimitedStream(maxSize: Long = DEFAULT_MAX_BODY_SIZE): InputStream {
    return SizeLimitedInputStream(rawRequest.inputStream, maxSize)
}

/**
 * Exception thrown when request body exceeds the configured size limit.
 *
 * @property maxSize The maximum allowed size in bytes.
 * @property bytesRead The number of bytes that were attempted to be read.
 */
class RequestBodyTooLargeException(
    val maxSize: Long,
    val bytesRead: Long
) : Exception("Request body size ($bytesRead bytes) exceeds maximum allowed size ($maxSize bytes)")

/**
 * Internal input stream that enforces a maximum size limit.
 */
private class SizeLimitedInputStream(
    private val delegate: InputStream,
    private val maxSize: Long
) : InputStream() {
    private var bytesRead = 0L

    override fun read(): Int {
        if (bytesRead >= maxSize) {
            throw RequestBodyTooLargeException(maxSize, bytesRead + 1)
        }
        val byte = delegate.read()
        if (byte != -1) {
            bytesRead++
        }
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (bytesRead >= maxSize) {
            throw RequestBodyTooLargeException(maxSize, bytesRead + 1)
        }

        // Read up to the limit
        val maxToRead = minOf(len.toLong(), maxSize - bytesRead).toInt()
        val read = delegate.read(b, off, maxToRead)
        if (read > 0) {
            bytesRead += read
        }
        return read
    }

    override fun close() {
        delegate.close()
    }
}

/**
 * Reads all bytes from the input stream with a size limit.
 */
private fun InputStream.readBytesWithLimit(maxSize: Long): ByteArray {
    val buffer = ByteArray(8192)
    var totalRead = 0
    val chunks = mutableListOf<ByteArray>()

    while (true) {
        val remaining = (maxSize - totalRead).toInt()
        if (remaining <= 0) {
            throw RequestBodyTooLargeException(maxSize, totalRead.toLong())
        }

        val toRead = minOf(buffer.size, remaining)
        val read = read(buffer, 0, toRead)

        if (read == -1) break

        totalRead += read

        // Store the chunk
        chunks.add(buffer.copyOf(read))
    }

    // Combine all chunks
    return chunks.fold(ByteArray(0)) { acc, chunk -> acc + chunk }
}
