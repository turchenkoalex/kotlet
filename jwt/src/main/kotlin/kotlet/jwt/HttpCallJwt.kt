package kotlet.jwt

import kotlet.HttpCall

/**
 * Returns JWT identity retrieved from [IdentityBuilder]
 */
fun <T> HttpCall.identity(): T? {
    @Suppress("UNCHECKED_CAST")
    return this.rawRequest.getAttribute(IDENTITY_PARAMETER_NAME) as? T
}
