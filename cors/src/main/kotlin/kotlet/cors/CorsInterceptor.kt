package kotlet.cors

import jakarta.servlet.http.HttpServletResponse
import kotlet.Handler
import kotlet.HttpCall
import kotlet.HttpMethod
import kotlet.Interceptor

/**
 * Interceptor that handles CORS requests.
 * Aborts the call chain if the request is a preflight request and sets the appropriate headers from the [CorsRules].
 */
internal data class CorsInterceptor(
    private val rules: CorsRules
) : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        if (call.httpMethod != HttpMethod.OPTIONS) {
            return next(call)
        }

        val headers = rules.getHeaders(call)
        call.rawResponse.setHeader("Access-Control-Allow-Origin", headers.allowOrigin)
        call.rawResponse.setHeader("Access-Control-Allow-Methods", headers.allowMethods)
        call.rawResponse.setHeader("Access-Control-Allow-Headers", headers.allowHeaders)
        call.status = HttpServletResponse.SC_OK

        // This is a preflight request, so we don't need to continue
        // Abort the call chain
    }
}
