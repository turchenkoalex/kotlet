import kotlet.Handler
import kotlet.HttpCall
import kotlet.Interceptor

/**
 * Example of simple interceptor that sets a header to the response
 */
data class SetHeaderInterceptor(
    private val headerName: String,
    private val headerValue: String,
) : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        call.rawResponse.setHeader(headerName, headerValue)
        next(call)
    }
}
