package kotlet

/**
 * HTTP request methods
 *
 * HTTP defines a set of request methods to indicate the desired action to be performed for a given resource.
 * Although they can also be nouns, these request methods are sometimes referred to as HTTP verbs.
 * Each of them implements a different semantic, but some common features are shared by a group of them:
 * e.g. a request method can be safe, idempotent, or cacheable.
 *
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
 */
enum class HttpMethod {
    /**
     * The GET method requests a representation of the specified resource. Requests using GET should only retrieve data.
     */
    GET,

    /**
     * The HEAD method asks for a response identical to a GET request, but without the response body.
     */
    HEAD,

    /**
     * The POST method submits an entity to the specified resource, often causing a change in state or side effects on the server.
     */
    POST,

    /**
     * The PUT method replaces all current representations of the target resource with the request payload.
     */
    PUT,

    /**
     * The DELETE method deletes the specified resource.
     */
    DELETE,

    /**
     * The CONNECT method establishes a tunnel to the server identified by the target resource.
     */
    CONNECT,

    /**
     * The OPTIONS method describes the communication options for the target resource.
     */
    OPTIONS,

    /**
     * The TRACE method performs a message loop-back test along the path to the target resource.
     */
    TRACE,

    /**
     * The PATCH method applies partial modifications to a resource.
     */
    PATCH;

    companion object {
        private val MAP = HttpMethod.entries.associateBy { it.name.uppercase() }

        fun parse(value: String): HttpMethod? {
            return MAP[value.uppercase()]
        }
    }
}
