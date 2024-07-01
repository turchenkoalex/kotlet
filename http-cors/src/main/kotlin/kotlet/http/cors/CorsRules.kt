package kotlet.http.cors

import kotlet.http.HttpCall

/**
 * Rules for CORS.
 *
 * Implementations of this interface define the headers for CORS response.
 */
interface CorsRules {
    fun getHeaders(call: HttpCall): CorsHeaders
}
