package kotlet

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class InterceptorsRoutingUnitTest {
    @Test
    fun `install global interceptors last`() {
        val operations = mutableListOf<String>()

        val routing = Kotlet.routing {
            get("/", handler = {
                operations.add("handler")
            })
        }

        val global1 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("global1")
                return call
            }
        }

        val global2 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("global2")
                return call
            }
        }

        routing.install(global1)
        routing.install(global2)

        val route = routing.getAllRoutes().single()

        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.GET
        }
        route.handler(call)

        assertEquals(listOf("global1", "global2", "handler"), operations)
    }

    @Test
    fun `install global interceptors first`() {
        val operations = mutableListOf<String>()

        val routing = Kotlet.routing {
            get("/", handler = {
                operations.add("handler")
            })
        }

        val global1 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("global1")
                return call
            }
        }

        val global2 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("global2")
                return call
            }
        }

        val global3 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("global3")
                return call
            }
        }

        routing.install(global1)
        routing.install(global2, global3, order = InstallOrder.FIRST)

        val route = routing.getAllRoutes().single()

        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.GET
        }
        route.handler(call)

        assertEquals(listOf("global2", "global3", "global1", "handler"), operations)
    }

    @Test
    fun `install global interceptors before local`() {
        val operations = mutableListOf<String>()

        val global1 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("global1")
                return call
            }
        }

        val local2 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("local2")
                return call
            }
        }

        val routing = Kotlet.routing {
            use(local2) {
                get("/", handler = {
                    operations.add("handler")
                })
            }

            install(global1)
        }

        val route = routing.getAllRoutes().single()

        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.GET
        }
        route.handler(call)

        assertEquals(listOf("global1", "local2", "handler"), operations)
    }

    @Test
    fun `install local interceptors with nesting`() {
        val operations = mutableListOf<String>()

        val local1 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("local1")
                return call
            }
        }

        val local2 = object : Interceptor {
            override fun beforeCall(call: HttpCall): HttpCall {
                operations.add("local2")
                return call
            }
        }

        val routing = Kotlet.routing {
            use(local1) {
                use(local2) {
                    get("/", handler = {
                        operations.add("handler")
                    })
                }
            }
        }

        val route = routing.getAllRoutes().single()

        val call = mockk<HttpCall> {
            every { httpMethod } returns HttpMethod.GET
        }
        route.handler(call)

        assertEquals(listOf("local1", "local2", "handler"), operations)
    }
}
