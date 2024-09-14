package kotlet.cors

import kotlet.HttpCall

/**
 * Always return the provided response.
 */
class ConstantCorsRules(private val response: CorsResponse) : CorsRules {
    override fun getResponse(call: HttpCall): CorsResponse {
        return response
    }
}
