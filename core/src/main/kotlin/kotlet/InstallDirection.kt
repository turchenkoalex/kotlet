package kotlet

/**
 * Interceptor installation direction
 */
enum class InstallDirection {
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
