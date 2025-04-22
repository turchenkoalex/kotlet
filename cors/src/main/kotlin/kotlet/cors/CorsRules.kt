package kotlet.cors

import kotlet.HttpCall

/**
 * Rules for CORS.
 *
 * Implementations of this interface define the CORS response.
 */
interface CorsRules {
    /**
     * Get the CORS response for the given HTTP call.
     */
    fun getResponse(call: HttpCall): CorsResponse
}
