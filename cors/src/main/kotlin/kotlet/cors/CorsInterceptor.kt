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
        if (call.httpMethod == HttpMethod.OPTIONS) {
            processPreflightRequest(call)

            // This is a preflight request, so we don't need to continue
            // Abort the call chain
            return
        }

        if (isCorsRequest(call)) {
            when (val response = rules.getResponse(call)) {
                is CorsResponse.Headers -> {
                    writeAllowOriginHeader(call, response)
                }

                is CorsResponse.Error -> {
                    call.respondError(response.statusCode, response.message)

                    // Abort the call chain on CORS error
                    return
                }
            }
        }

        // Not a preflight request, continue the call chain
        next(call)
    }

    /**
     * Determines if the request is a CORS request by checking for the presence of the "Origin" header.
     */
    private fun isCorsRequest(call: HttpCall): Boolean {
        val hasOrigin = !call.rawRequest.getHeader("Origin").isNullOrEmpty()
        return hasOrigin
    }

    /**
     * Processes a CORS preflight request.
     */
    private fun processPreflightRequest(call: HttpCall) {
        when (val response = rules.getResponse(call)) {
            is CorsResponse.Headers -> {
                call.status = HttpServletResponse.SC_OK
                writeAllowOriginHeader(call, response)
                with(call.rawResponse) {
                    setHeader("Access-Control-Allow-Methods", response.allowMethods)
                    setHeader("Access-Control-Allow-Headers", response.allowHeaders)
                    if (response.maxAgeSeconds.isNotEmpty()) {
                        setHeader("Access-Control-Max-Age", response.maxAgeSeconds)
                    }
                }
            }

            is CorsResponse.Error -> {
                call.respondError(response.statusCode, response.message)
            }
        }
    }

    private fun writeAllowOriginHeader(call: HttpCall, corsResponse: CorsResponse.Headers) {
        with(call.rawResponse) {
            val allowOrigin = corsResponse.allowOrigin
            setHeader("Access-Control-Allow-Origin", allowOrigin)
            if (allowOrigin != CORS.ALL_ORIGINS) {
                setHeader("Vary", "Origin")
            }
        }
    }
}
