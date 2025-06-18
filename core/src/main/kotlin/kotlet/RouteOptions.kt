package kotlet

import kotlet.attributes.MutableRouteAttributes
import kotlet.attributes.RouteAttribute
import kotlet.attributes.RouteAttributes
import kotlet.attributes.emptyRouteAttributes

/**
 * Settings for a route.
 *
 * @param interceptors Interceptors to be executed for the route.
 * @param attributes Attributes for the route.
 */
@Suppress("unused")
class RouteOptions(
    internal val interceptors: List<Interceptor>,
    internal val attributes: RouteAttributes,
) {
    @KotletDsl
    class RouteOptionsBuilder {
        private val interceptors = mutableListOf<Interceptor>()
        private val attributes = MutableRouteAttributes()

        /**
         * Add an interceptor to the route.
         */
        fun withInterceptor(interceptor: Interceptor): RouteOptionsBuilder {
            interceptors.add(interceptor)
            return this
        }

        /**
         * Add an attribute to the route.
         *
         * @param key Attribute key.
         * @param value Attribute value.
         * @throws IllegalStateException If the attribute is already present.
         */
        fun <T : Any> withAttribute(key: RouteAttribute<T>, value: T) {
            if (attributes[key] != null) {
                error("Attribute $key already present in the route settings")
            }
            attributes[key] = value
        }

        internal fun build(): RouteOptions {
            return RouteOptions(
                interceptors = interceptors,
                attributes = attributes,
            )
        }
    }

    /**
     * Create a new [RouteOptions] with the predefined interceptors.
     */
    internal fun merge(predefinedInterceptors: Collection<Interceptor>): RouteOptions {
        if (predefinedInterceptors.isEmpty()) {
            return this
        }

        val mergedInterceptors = ArrayList<Interceptor>(this.interceptors.size + predefinedInterceptors.size)
        mergedInterceptors.addAll(predefinedInterceptors)
        mergedInterceptors.addAll(this.interceptors)

        return RouteOptions(
            interceptors = mergedInterceptors,
            attributes = this.attributes,
        )
    }

    companion object {
        val EMPTY_OPTIONS = RouteOptions(
            interceptors = emptyList(),
            attributes = emptyRouteAttributes(),
        )
    }
}

/**
 * Create a new [RouteOptions] with the specified interceptors.
 */
fun routeOptions(
    block: RouteOptions.RouteOptionsBuilder.() -> Unit
): RouteOptions {
    val builder = RouteOptions.RouteOptionsBuilder()
    builder.block()
    return builder.build()
}
