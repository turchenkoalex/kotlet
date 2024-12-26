package kotlet

import io.mockk.mockk
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class InterceptorUnitTest {
    @Test
    fun `interceptor call chain must be in valid order`() {
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

    @Test
    fun `interceptors afterCall must be invoked while exception occurred in handler`() {
        val step = mutableListOf<String>()

        val firstInterceptor = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                step.add("before 1")
                return call
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

            override fun afterCall(call: HttpCall): HttpCall {
                step.add("after 2")
                return call
            }
        }

        val interceptors = listOf(firstInterceptor, secondInterceptor)
        val handler: Handler = { _: HttpCall ->
            step.add("call")
            error("failure")
        }

        val callChain = Interceptor.createRecursiveHandler(interceptors, handler)

        assertThrows<IllegalStateException> {
            callChain(mockk())
        }


        assertEquals(
            listOf(
                "before 1",
                "before 2",
                "call",
                "after 2",
                "after 1"
            ), step
        )
    }

    @Test
    fun `interceptors afterCall must be invoked while exception occurred in next interceptor`() {
        val step = mutableListOf<String>()

        val firstInterceptor = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                step.add("before 1")
                return call
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
                error("interceptor failure")
            }

            override fun afterCall(call: HttpCall): HttpCall {
                step.add("after 2")
                return call
            }
        }

        val interceptors = listOf(firstInterceptor, secondInterceptor)
        val handler: Handler = { _: HttpCall -> step.add("call") }

        val callChain = Interceptor.createRecursiveHandler(interceptors, handler)

        assertThrows<IllegalStateException> {
            callChain(mockk())
        }

        assertEquals(
            listOf(
                "before 1",
                "before 2",
                "after 2",
                "after 1"
            ), step
        )
    }
}
