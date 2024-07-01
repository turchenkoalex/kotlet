package kotlet.cors

import kotlet.HttpCall

/**
 * Rules for CORS.
 *
 * Implementations of this interface define the CORS response.
 */
interface CorsRules {
    fun getResponse(call: HttpCall): CorsResponse
}
