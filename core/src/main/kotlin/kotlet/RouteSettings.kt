package kotlet

import kotlet.attributes.RouteAttribute
import kotlet.attributes.RouteAttributes
import kotlet.attributes.MutableRouteAttributes

/**
 * Settings for a route.
 *
 * @param interceptors Interceptors to be executed for the route.
 * @param attributes Attributes for the route.
 */
class RouteSettings(
    internal val interceptors: List<Interceptor>,
    internal val attributes: RouteAttributes,
) {
    @KotletDsl
    class RouteSettingsBuilder(
        interceptors: Collection<Interceptor>
    ) {
        private val interceptors = interceptors.toMutableList()
        private val attributes = MutableRouteAttributes()

        /**
         * Add an interceptor to the route.
         */
        fun withInterceptor(interceptor: Interceptor): RouteSettingsBuilder {
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
        fun <T: Any> withAttribute(key: RouteAttribute<T>, value: T) {
            if (attributes[key] != null) {
                error("Attribute $key already present in the route settings")
            }
            attributes[key] = value
        }

        internal fun build(): RouteSettings {
            return RouteSettings(
                interceptors = interceptors,
                attributes = attributes,
            )
        }
    }
}
