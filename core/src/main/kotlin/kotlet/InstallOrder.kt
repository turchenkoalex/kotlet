package kotlet

/**
 * Interceptor installation order
 *
 * Interceptors can be installed at the beginning or end of the interceptor list.
 * The order in which interceptors are called is determined by the order in which they are installed
 *
 * @see Interceptor
 */
enum class InstallOrder {
    /**
     * Install interceptors at the beginning of the interceptor list
     * The first interceptor will be the first to be called on the request
     */
    FIRST,

    /**
     * Install interceptors at the end of the interceptor list
     * The last interceptor will be called after all other installed interceptors
     */
    LAST
}
