package kotlet.attributes

/**
 * Route attribute that can be attached to a [kotlet.RegisteredRoute].
 * Attributes are used to store additional information about a route.
 * This is a generic class that can be used to store any type of attribute value.
 *
 * @param T the type of the attribute value.
 * @param name the name of the attribute.
 * @see kotlet.RegisteredRoute
 * @see RouteAttributes
 */
class RouteAttribute<T: Any> internal constructor(
    private val name: String
) {
    companion object {
        fun <T: Any> of(name: String): RouteAttribute<T> {
            return RouteAttribute(name)
        }
    }

    override fun toString(): String {
        return "RouteAttribute(name='$name')"
    }

}
