package kotlet

/**
 * Interceptor for HTTP calls.
 *
 * Interceptors can be used to modify the request or response of a call.
 *
 * The [beforeCall] method is called before the call is made, and can be used to modify the call.
 * The [aroundCall] method is the main method to override to modify the call, or to stop the call from being made.
 * The [afterCall] method can be used to clean up resources after the call is made.
 *
 * Interceptors can be nested, lining up handlers one after another.
 */
interface Interceptor {
    /**
     * Called before the call is made. This method can be used to modify the call before it is made.
     */
    fun beforeCall(call: HttpCall): HttpCall {
        return call
    }

    /**
     * Called around the call. This method is the main method to override to modify the call, or to stop the call from being made.
     * For stopping the call, the [next] handler should not be called.
     */
    fun aroundCall(call: HttpCall, next: Handler) {
        next(call)
    }

    /**
     * Called after the call is made, and after all interceptors have been called in the chain.
     * This method can be used to clean up resources after the call is made.
     */
    fun afterCall(call: HttpCall): HttpCall {
        return call
    }

    companion object {
        /**
         * Create a call chain with interceptors.
         */
        internal fun createRecursiveHandler(interceptors: List<Interceptor>, handler: Handler): Handler {
            if (interceptors.isEmpty()) {
                return handler
            }

            // Fold the interceptors into a single handler
            return interceptors.foldRight(handler) { interceptor: Interceptor, next: Handler ->
                { call ->
                    val interceptedCall = interceptor.beforeCall(call)
                    try {
                        interceptor.aroundCall(interceptedCall, next)
                    } catch (e: Exception) {
                        // Always call afterCall even if an exception is thrown
                        // This is to ensure that resources are cleaned up
                        interceptor.afterCall(interceptedCall)

                        // Re-throw the exception
                        throw e
                    }
                    interceptor.afterCall(interceptedCall)
                }
            }
        }
    }
}