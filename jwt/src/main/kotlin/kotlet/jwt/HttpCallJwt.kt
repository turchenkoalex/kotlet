package kotlet.jwt

import kotlet.HttpCall

fun <T> HttpCall.identity(): T? {
    @Suppress("UNCHECKED_CAST")
    return this.rawRequest.getAttribute(IDENTITY_PARAMETER_NAME) as? T
}
