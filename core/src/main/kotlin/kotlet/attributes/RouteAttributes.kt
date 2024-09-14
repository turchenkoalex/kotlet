package kotlet.attributes

/**
 * Route attributes. Attributes are used to store additional information about a route.
 * This interface provides read-only access to the attributes.
 *
 * @see RouteAttribute
 * @see kotlet.RegisteredRoute
 */
interface RouteAttributes {

    /**
     * Checks if the specified key is present in the attributes.
     */
    fun <T : Any> containsKey(key: RouteAttribute<T>): Boolean

    /**
     * Gets the attribute value for the specified key.
     */
    operator fun <T : Any> get(key: RouteAttribute<T>): T?

}
