package kotlet.client

/**
 * Exception thrown when deserialization fails.
 */
class DeserializationException(
    val statusCode: Int,
    message: String,
    cause: Throwable
) : RuntimeException(message, cause)
