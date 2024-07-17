package kotlet.mocks

import kotlet.Handler
import kotlet.HttpCall
import kotlet.Interceptor

object Interceptors {
    fun invokeInterceptor(interceptor: Interceptor, call: HttpCall, handler: Handler) {
        val newCall = interceptor.beforeCall(call)
        interceptor.aroundCall(newCall, handler)
        interceptor.afterCall(newCall)
    }
}
