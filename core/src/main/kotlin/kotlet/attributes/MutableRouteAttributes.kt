package kotlet.attributes

/**
 * Mutable route attributes.
 *
 * This class is used to store route attributes.
 * It provides a mutable interface to set and read attributes.
 *
 * @see RouteAttributes
 */
internal class MutableRouteAttributes : RouteAttributes {
    private val map = mutableMapOf<RouteAttribute<*>, Any>()

    override fun <T : Any> containsKey(key: RouteAttribute<T>): Boolean {
        return map.containsKey(key)
    }

    override fun <T : Any> get(key: RouteAttribute<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[key] as? T
    }

    operator fun <T : Any> set(key: RouteAttribute<T>, value: Any) {
        map[key] = value
    }

    override fun toString(): String {
        val items = map.keys.joinToString(", ", prefix = "[", postfix = "]")
        return "RouteAttributes($items)"
    }
}
