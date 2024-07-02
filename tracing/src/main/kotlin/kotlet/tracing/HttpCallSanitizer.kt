package kotlet.tracing

import kotlet.HttpCall

/**
 * An interface to sanitize the HTTP call for tracing.
 */
interface HttpCallSanitizer {
    /**
     * Get the HTTP request header with the given name.
     */
    fun getHttpRequestHeader(call: HttpCall, name: String): MutableList<String>

    /**
     * Get the URL path of the HTTP call.
     */
    fun getUrlPath(call: HttpCall): String?

    /**
     * Get the URL query of the HTTP call.
     */
    fun getUrlQuery(call: HttpCall): String?
}