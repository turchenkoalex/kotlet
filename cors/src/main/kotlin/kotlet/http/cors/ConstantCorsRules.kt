package kotlet.http.cors

import kotlet.http.HttpCall

/**
 * Always return the provided headers.
 */
class ConstantCorsRules(private val headers: CorsHeaders) : CorsRules {
    override fun getHeaders(call: HttpCall): CorsHeaders {
        return headers
    }
}