package kotlet

import kotlet.attributes.MutableRouteAttributes
import kotlet.attributes.RouteAttribute
import kotlet.attributes.RouteAttributes

/**
 * Context for route configuration.
 * Holds interceptors and attributes for the route.
 * Used to extend the route configuration in a type-safe way.
 */
class RouteContext(
    interceptors: List<Interceptor>
) {
    private val initialInterceptors = interceptors.toList()
    private val additionalInterceptors = mutableListOf<Interceptor>()
    private val attributes = MutableRouteAttributes()

    /**
     * Add an interceptor to the route context
     * @param interceptor Interceptor to add.
     */
    internal fun addInterceptor(interceptor: Interceptor) {
        additionalInterceptors.add(interceptor)
    }

    /**
     * Add an attribute to the route context
     *
     * @param key Attribute key.
     * @param value Attribute value.
     * @throws IllegalStateException If the attribute is already present.
     */
    internal fun <T : Any> addAttribute(key: RouteAttribute<T>, value: T) {
        if (attributes[key] != null) {
            error("Attribute $key already present in the route settings")
        }
        attributes[key] = value
    }

    internal fun interceptors(): List<Interceptor> {
        return initialInterceptors + additionalInterceptors
    }

    internal fun attributes(): RouteAttributes {
        return attributes
    }

    class Mutator internal constructor(private val context: RouteContext) {
        /**
         * Add an interceptor to the route context
         * @param interceptor Interceptor to add.
         */
        fun withInterceptor(interceptor: Interceptor) {
            context.addInterceptor(interceptor)
        }

        /**
         * Add an attribute to the route context
         *
         * @param key Attribute key.
         * @param value Attribute value.
         * @throws IllegalStateException If the attribute is already present.
         */
        fun <T : Any> withAttribute(key: RouteAttribute<T>, value: T) {
            context.addAttribute(key, value)
        }
    }

}

/**
 * Configure the route context using a [block].
 *
 * Example:
 * ```
 * get {
 *     // Handler implementation
 * } configure {
 *     withAttribute(MyAttributeKey, myValue)
 *     withInterceptor(myInterceptor)
 * }
 * ```
 *
 * @param block Configuration block.
 * @return The same [RouteContext] instance.
 */
infix fun RouteContext.configure(block: RouteContext.Mutator.() -> Unit): RouteContext {
    val mutator = RouteContext.Mutator(this)
    mutator.block()
    return this
}
