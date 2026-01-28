package kotlet.client

/**
 * Generic HTTP response wrapper.
 */
data class Response<TRes>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: TRes?
)
