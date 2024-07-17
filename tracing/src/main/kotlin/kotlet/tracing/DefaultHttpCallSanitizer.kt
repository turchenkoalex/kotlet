package kotlet.tracing

import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall

/**
 * Default implementation of [HttpCallSanitizer] that extracts information from [HttpCall] using default methods
 * without any modification.
 */
object DefaultHttpCallSanitizer : HttpCallSanitizer {
    override fun getHttpRequestHeader(call: HttpCall, name: String): MutableList<String> {
        return call.rawRequest.getHeaders(name)
            .toList()
            .toMutableList()
    }

    override fun getHttpResponseHeader(response: HttpServletResponse, name: String): MutableList<String> {
        return response.getHeaders(name)
            .toList()
            .toMutableList()
    }

    override fun getUrlPath(call: HttpCall): String? {
        return call.rawRequest.requestURI
    }

    override fun getUrlQuery(call: HttpCall): String? {
        return call.rawRequest.queryString
    }
}
