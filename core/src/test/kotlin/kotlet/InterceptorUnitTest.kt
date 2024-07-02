package kotlet

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class InterceptorUnitTest {
    @Test
    fun createInterceptedCallChainTest() {
        val step = mutableListOf<String>()

        val firstInterceptor = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                step.add("before 1")
                return call
            }

            override fun aroundCall(call: HttpCall, next: Handler) {
                step.add("around 1")
                next(call)
            }

            override fun afterCall(call: HttpCall): HttpCall {
                step.add("after 1")
                return call
            }
        }

        val secondInterceptor = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                step.add("before 2")
                return call
            }

            override fun aroundCall(call: HttpCall, next: Handler) {
                step.add("around 2")
                next(call)
            }

            override fun afterCall(call: HttpCall): HttpCall {
                step.add("after 2")
                return call
            }
        }

        val interceptors = listOf(firstInterceptor, secondInterceptor)
        val handler: Handler = { _: HttpCall -> step.add("call") }

        val callChain = Interceptor.createRecursiveHandler(interceptors, handler)
        callChain(mockk())

        assertEquals(
            listOf(
                "before 1",
                "around 1",
                "before 2",
                "around 2",
                "call",
                "after 2",
                "after 1"
            ), step
        )
    }
}