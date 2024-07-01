package kotlet.http

inline fun <reified T : Any> HttpCall.receivePath(): T {
    return DataClassHelpers.convertMapToDataClass(T::class, parameters)
}

inline fun <reified T : Any> HttpCall.receiveQuery(): T {
    // In query part of the request, keys can be repeated with different values, like this:
    // /path?foo=1&bar=2&foo=3
    // Parameter foo is repeated 2 times here and this is a completely legal HTTP request
    // For this reason, request.parameterMap is represented as Map<String, Array<String>>, not just Map<String, String>
    // However, it is absolutely unclear how to "fit" such a data structure into a data-class, so for such multi-parameters
    // we take only the very first one and that's it
    val queryParams = rawRequest.parameterMap.mapValues { it.value.first() }
    return DataClassHelpers.convertMapToDataClass(T::class, queryParams)
}
